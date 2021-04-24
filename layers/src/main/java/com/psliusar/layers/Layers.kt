package com.psliusar.layers

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.VisibleForTesting
import java.util.LinkedList
import kotlin.collections.ArrayList

private const val STATE_STACK = "LAYERS.STATE_STACK"
private const val STATE_LAYERS = "LAYERS.STATE_LAYERS"

class Layers @VisibleForTesting constructor(
    val host: LayersHost,
    val containerId: Int,
    savedState: Bundle?
) {

    constructor(
        host: LayersHost,
        savedState: Bundle?
    ) : this(host, View.NO_ID, savedState)

    val hasRunningTransition: Boolean
        get() = transitions.size != 0

    val isInSavedState: Boolean
        get() = stateSaved

    val stackSize: Int
        get() = layerStack.size

    var isViewPaused = false
        private set

    private val transitions = LinkedList<Transition<*>>()
    private val layerStack: ArrayList<StackEntry>
    private var container: ViewGroup? = null
    private var layerGroup: SparseArray<Layers>? = null
    private var stateSaved = false

    init {
        isViewPaused = savedState != null
        layerStack = savedState?.getParcelableArrayList(STATE_STACK) ?: ArrayList()

        layerStack.forEachIndexed { i, entry ->
            entry.layerInstance = null
            entry.state = StackEntry.LAYER_STATE_EMPTY
            moveToState(entry, i, StackEntry.LAYER_STATE_CREATED, false)
        }

        // Initialize Layers at other containers (groups)
        savedState?.getSparseParcelableArray<Bundle>(STATE_LAYERS)
            ?.forEach { key, state ->
                at(key, state)
            }
    }

    fun at(@IdRes containerId: Int): Layers = when (containerId) {
        this.containerId -> this
        else -> at(containerId, null)
    }

    @VisibleForTesting
    internal fun at(@IdRes containerId: Int, state: Bundle?): Layers {
        val group = layerGroup.or { SparseArray<Layers>().also { layerGroup = it } }
        return group.get(containerId).or { Layers(host, containerId, state).also { group.put(containerId, it) } }
    }

    /**
     * Un-pause View creation. View on the top of the stack will be created if wasn't created already.
     */
    fun resumeView() {
        if (!isViewPaused) {
            return
        }
        isViewPaused = false

        ensureViews()

        layerGroup?.forEach { _, layers ->
            layers.resumeView()
        }
    }

    /**
     * Set View creation to pause. Views will not be created until [Layers.resumeView] is called.
     */
    fun pauseView() {
        isViewPaused = true
    }

    //region Public layer operations

    inline fun <reified L : Layer> add(noinline block: (Transition<L>.() -> Unit)? = null) {
        add(L::class.java, block)
    }

    fun <L : Layer> add(layerClass: Class<L>, block: (Transition<L>.() -> Unit)? = null) {
        val t = AddTransition(this, layerClass)
        block?.invoke(t)
        t.commit()
    }

    fun update(index: Int, arguments: Bundle) {
        val entry = layerStack.getOrNull(index)
            ?: throw IndexOutOfBoundsException("Index: $index, Size: ${layerStack.size}")

        entry.arguments = arguments
        entry.layerInstance?.update(arguments)
    }

    inline fun <reified L : Layer> replace(noinline block: (Transition<L>.() -> Unit)? = null) {
        replace(L::class.java, block)
    }

    fun <L : Layer> replace(layerClass: Class<L>, block: (Transition<L>.() -> Unit)? = null) {
        val t = ReplaceTransition(this, layerClass)
        block?.invoke(t)
        t.commit()
    }

    fun <L : Layer> remove(index: Int, block: (Transition<L>.() -> Unit)? = null) {
        val t = RemoveTransition<L>(this, index)
        block?.invoke(t)
        t.commit()
    }

    fun <L : Layer> remove(layer: L, block: (Transition<L>.() -> Unit)? = null) {
        layerStack.indexOfLast { it.layerInstance === layer }
            .takeIf { it >= 0 }
            ?.let { remove(it, block) }
            ?: throw IllegalArgumentException("Layer not found: $layer")
    }

    fun <L : Layer> pop(): L? = peek<L>()
        ?.also { RemoveTransition<Layer>(this, layerStack.size - 1).commit() }

    fun <L : Layer> popLayersTo(name: String?, inclusive: Boolean): L? {
        val size = layerStack.size
        if (size == 0) {
            return null
        }

        // If name is not defined - any layer matches
        if (name == null) {
            // Remove all
            if (inclusive) {
                val entry = layerStack[0]
                clear()
                return entry.layerInstance as L?
            }
        } else if (layerStack.none { it.name == name }) {
            // Nothing found, do not touch the stack
            return null
        }

        pauseView()

        var lastEntry: StackEntry? = null
        for (i in size - 1 downTo 0) {
            val entry = layerStack[i]
            if ((name == null && i > 0) || (name != null && name != entry.name || inclusive)) {
                moveToState(entry, i, StackEntry.LAYER_STATE_DESTROYED, false)
                layerStack.removeAt(i)
                lastEntry = entry
                if (name == entry.name) {
                    break
                }
            } else {
                break
            }
        }

        resumeView()

        return lastEntry?.layerInstance as L?
    }

    fun onBackPressed(): Boolean {
        return layerStack.lastOrNull {
            val layer = it.layerInstance
            it.valid && layer != null && layer.onBackPressed()
        } != null
    }

    fun clear(): Int {
        val size = layerStack.size
        if (size == 0) {
            return 0
        }

        pauseView()

        for (i in size - 1 downTo 0) {
            moveToState(layerStack[i], i, StackEntry.LAYER_STATE_DESTROYED, false)
        }

        layerStack.clear()

        resumeView()

        return size
    }

    //endregion

    //region Layer getters

    fun <L : Layer> peek(): L? = get(layerStack.size - 1)

    fun <L : Layer> get(index: Int): L? = layerStack.getOrNull(index)?.layerInstance as L?

    fun <L : Layer> find(name: String?): L? = layerStack.lastOrNull { it.name == name }?.layerInstance as L?

    fun indexOf(name: String?): Int = layerStack.indexOfLast { it.name == name }

    //endregion

    //region Lifecycle

    fun saveState(): Bundle? {
        val outState = Bundle()

        transitions.forEach {
            it.fastForward(layerStack)
        }

        layerStack.filterTo(ArrayList()) { it.valid }.takeIf { it.size > 0 }?.let { stack ->
            for (i in stack.size - 1 downTo 0) {
                val entry = stack[i]
                if (entry.layerInstance?.view != null) {
                    saveViewState(entry)
                }
                saveLayerState(entry)
            }
            outState.putParcelableArrayList(STATE_STACK, stack)
        }

        // Save state of layers which are at other containers
        layerGroup?.takeIf { it.size() > 0 }?.let { groups ->
            val layersArray = SparseArray<Bundle>()
            groups.forEach { key, layers ->
                layers.saveState()?.let {
                    layersArray.put(key, it)
                }
            }
            outState.putSparseParcelableArray(STATE_LAYERS, layersArray)
        }

        stateSaved = true
        return outState.takeIf { it.size() > 0 }
    }

    fun destroy() {
        val size = layerStack.size
        for (i in size - 1 downTo 0) {
            val entry = layerStack[i]
            moveToState(entry, i, StackEntry.LAYER_STATE_DESTROYED, false)
        }

        // Layers at other containers
        layerGroup?.forEach { _, layers ->
            layers.destroy()
        }
    }

    //endregion

    //region Layer management

    private fun moveToState(entry: StackEntry, index: Int, toState: Int, saveState: Boolean) {
        var targetState = toState
        var state = entry.state
        if (targetState <= StackEntry.LAYER_STATE_VIEW_CREATED) {
            while (state < targetState) {
                when (state) {
                    StackEntry.LAYER_STATE_EMPTY -> {
                        createLayer(entry)
                        entry.state = StackEntry.LAYER_STATE_CREATED
                    }
                    StackEntry.LAYER_STATE_CREATED -> if (!isViewPaused) {
                        createView(entry, index)
                        entry.state = StackEntry.LAYER_STATE_VIEW_CREATED
                    }
                    StackEntry.LAYER_STATE_VIEW_CREATED -> {
                        // nothing to do
                    }
                    else -> return
                }
                state++
            }
        } else {
            when (targetState) {
                StackEntry.LAYER_STATE_VIEW_DESTROYED -> targetState = StackEntry.LAYER_STATE_CREATED
                StackEntry.LAYER_STATE_DESTROYED -> targetState = StackEntry.LAYER_STATE_EMPTY
            }
            while (state > targetState) {
                when (state) {
                    StackEntry.LAYER_STATE_EMPTY ->
                        // nothing to do
                        return
                    StackEntry.LAYER_STATE_CREATED -> {
                        destroyLayer(entry, saveState)
                        entry.state = StackEntry.LAYER_STATE_EMPTY
                    }
                    StackEntry.LAYER_STATE_VIEW_CREATED -> {
                        destroyView(entry, saveState)
                        entry.state = StackEntry.LAYER_STATE_CREATED
                    }
                    else -> return
                }
                state--
            }
        }
    }

    private fun createLayer(entry: StackEntry) {
        entry.instantiateLayer().create(host, entry.arguments, entry.name, entry.layerState)
    }

    private fun createView(entry: StackEntry, index: Int) {
        val container = getContainer()
        val layer = entry.layerInstance ?: throw IllegalStateException("Layer instance must exist")
        layer.createView(entry.layerState, if (layer.isViewInLayout) container else null, entry.layoutResId)
        val layerView = layer.view
        if (layerView != null && layer.isViewInLayout) {
            var fromEnd = 0
            for (i in layerStack.size - 1 downTo index + 1) {
                if (layerStack[i].layerInstance?.view != null) {
                    fromEnd++
                }
            }

            val position = container.childCount - fromEnd
            container.addView(layerView, position)
        }

        layer.isAttached = true

        layer.onAttach()

        layerView?.let {
            layer.onBindView(entry.layerState, it)
            restoreViewState(entry)
        }
    }

    private fun restoreViewState(entry: StackEntry) {
        val savedState = entry.viewState
        entry.layerInstance?.restoreViewState(savedState)
    }

    private fun saveViewState(entry: StackEntry) {
        val viewState = SparseArray<Parcelable>()
        entry.layerInstance?.saveViewState(viewState)
        if (viewState.size() > 0) {
            entry.viewState = viewState
        }
    }

    private fun destroyView(entry: StackEntry, saveState: Boolean) {
        val layer = entry.layerInstance ?: throw IllegalStateException("Layer instance must exist")

        if (saveState && layer.view != null) {
            saveViewState(entry)
        }

        layer.onDetach()

        layer.isAttached = false

        val layerView = layer.view
        layer.destroyView()
        if (layer.isViewInLayout && layerView != null) {
            getContainer().removeView(layerView)
        }
    }

    private fun saveLayerState(entry: StackEntry) {
        val bundle = Bundle()
        entry.layerInstance?.saveLayerState(bundle)
        if (bundle.size() > 0) {
            entry.layerState = bundle
        }
    }

    private fun destroyLayer(entry: StackEntry, saveState: Boolean) {
        if (saveState) {
            saveLayerState(entry)
        }
        entry.layerInstance?.destroy(!saveState) // XXX
    }

    //endregion

    //region Transition

    internal fun addTransition(t: Transition<*>) {
        transitions.offer(t)
        if (transitions.size == 1) {
            transitions.peek().apply()
        }
    }

    internal fun nextTransition() {
        transitions.poll()
        if (!transitions.isEmpty()) {
            transitions.peek().apply()
        }
    }

    //endregion

    //region Internal tools

    internal fun commitStackEntry(entry: StackEntry) {
        layerStack.add(entry)
        ensureViews()
    }

    internal fun getStackEntryAt(index: Int): StackEntry = layerStack[index]

    /**
     * TODO
     *
     * @param index
     * @param <L>
     * @return
    </L> */
    internal fun <L : Layer> removeLayerAt(index: Int): L? {
        val entry = layerStack.getOrNull(index) ?: return null

        moveToState(entry, index, StackEntry.LAYER_STATE_DESTROYED, false)

        val layer = layerStack.removeAt(index).layerInstance as L?

        ensureViews()

        return layer
    }

    /**
     * All "transparent" from top till ground or "opaque", including it.
     *
     * @return the lowest visible layer index or -1 if stack is empty
     */
    internal fun getLowestVisibleLayer(): Int {
        val size = layerStack.size
        var lowest = size - 1
        // from end to beginning, search for opaque layer
        for (i in lowest downTo 0) {
            lowest = i
            val entry = layerStack[i]
            if (!entry.inTransition && entry.layerType == StackEntry.TYPE_OPAQUE) {
                break
            }
        }
        return lowest
    }

    //endregion

    //region Views

    internal fun ensureViews() {
        val size = layerStack.size
        if (isViewPaused || size == 0) {
            return
        }

        val lowest = getLowestVisibleLayer()
        for (i in 0 until size) {
            val entry = layerStack[i]
            if (i < lowest) {
                moveToState(entry, i, StackEntry.LAYER_STATE_VIEW_DESTROYED, true)
            } else {
                moveToState(entry, i, StackEntry.LAYER_STATE_VIEW_CREATED, false)
            }
        }
    }

    private fun getContainer(): ViewGroup = container.or {
        when (containerId) {
            View.NO_ID -> host.defaultContainer
            else -> host.getView(containerId)
        }.also {
            it.isSaveFromParentEnabled = false
            container = it
        }
    }

    //endregion
}