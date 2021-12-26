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

/**
 * Manages stacks of Layers in view groups. Each [Layer] may or may not provide a view.
 *
 * Keeps stack of layers.
 * Manages lifecycle.
 * Provides different stacks.
 *
 * @param host the host, usually implemented by Activity.
 * @param containerId the ID of the container (view group) that will be used to stack Layers in it.
 * @param savedState the state that was saved during configuration change. Contains states for all
 * stacks controlled by this manager.
 *
 * @see saveState
 */
class Layers @VisibleForTesting constructor(
    val host: LayersHost,
    val containerId: Int,
    savedState: Bundle?
) {

    /**
     * Main constructor for [Layers]. Uses default container for the stack.
     *
     * @param host the host, usually implemented by Activity.
     * @param savedState the state that was saved during configuration change. Contains states for
     * all stacks controlled by this manager.
     */
    constructor(
        host: LayersHost,
        savedState: Bundle?
    ) : this(host, View.NO_ID, savedState)

    /**
     * Indicates whether there is an active transition happening.
     */
    val hasRunningTransition: Boolean
        get() = transitions.size != 0

    /**
     * Returns the size of the stack.
     */
    val stackSize: Int
        get() = layerStack.size

    /**
     * Indicates whether the view creation is paused or not.
     */
    var isViewPaused = false
        private set

    private val transitions = LinkedList<Transition<*>>()
    private val layerStack: ArrayList<StackEntry>
    private var container: ViewGroup? = null
    private var layerGroup: SparseArray<Layers>? = null

    init {
        isViewPaused = savedState != null
        layerStack = savedState?.getParcelableArrayList(STATE_STACK) ?: ArrayList()

        layerStack.forEachIndexed { i, entry ->
            entry.layerInstance = null
            entry.state = StackEntry.LAYER_STATE_EMPTY
            moveToState(entry, i, StackEntry.LAYER_STATE_CREATED, false)
        }

        // Initialize Layers at other containers (stacks)
        savedState?.getSparseParcelableArray<Bundle>(STATE_LAYERS)
            ?.forEach { key, state ->
                at(key, state)
            }
    }

    /**
     * Returns an existing manager or creates a new one for the given container.
     */
    fun at(@IdRes containerId: Int): Layers = when (containerId) {
        this.containerId -> this
        else -> at(containerId, null)
    }

    /**
     * Returns an existing manager or creates a new one for the given container.
     */
    @VisibleForTesting
    internal fun at(@IdRes containerId: Int, state: Bundle?): Layers {
        val group = layerGroup.or { SparseArray<Layers>().also { layerGroup = it } }
        return group.getOrPut(containerId) { Layers(host, containerId, state) }
    }

    /**
     * Un-pause View creation. View on the top of the stack will be created if it wasn't created
     * already.
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

    /**
     * Adds a Layer to the stack.
     *
     * @param layerClass the class of the Layer.
     * @param block the callback to configure transition.
     */
    fun <L : Layer> add(layerClass: Class<L>, block: (Transition<L>.() -> Unit)? = null) {
        val t = AddTransition(this, layerClass)
        block?.invoke(t)
        addTransition(t)
    }

    /**
     * Updates a Layer at the given index.
     *
     * @param index the index in stack at which a Layer should be updated.
     * @param arguments the arguments to pass to Layer.
     */
    fun update(index: Int, arguments: Bundle) {
        val entry = layerStack.getOrNull(index)
            ?: throw IndexOutOfBoundsException("Index: $index, Size: ${layerStack.size}")

        entry.arguments = arguments
        entry.layerInstance?.update(arguments)
    }

    /**
     * Replaces a Layer at the top of the stack with another one.
     *
     * @param layerClass the class of the new Layer.
     * @param block the callback to configure transition.
     */
    fun <L : Layer> replace(layerClass: Class<L>, block: (Transition<L>.() -> Unit)? = null) {
        val t = ReplaceTransition(this, layerClass)
        block?.invoke(t)
        addTransition(t)
    }

    /**
     * Removes a Layer at the given index from the stack.
     *
     * @param index the index in stack at which a Layer should be removed.
     * @param block the callback to configure transition.
     */
    fun <L : Layer> remove(index: Int, block: (Transition<L>.() -> Unit)? = null) {
        val t = RemoveTransition<L>(this, index)
        block?.invoke(t)
        addTransition(t)
    }

    /**
     * Removes a Layer from the stack.
     *
     * @param layer the instance of Layer to remove.
     * @param block the callback to configure transition.
     */
    fun <L : Layer> remove(layer: L, block: (Transition<L>.() -> Unit)? = null) {
        layerStack.indexOfLast { it.layerInstance === layer }
            .takeIf { it >= 0 }
            ?.let { remove(it, block) }
            ?: throw IllegalArgumentException("Layer not found: $layer")
    }

    /**
     * Pops a Layer out from the top of the stack.
     *
     * @return the Layer being removed or null if the stack is empty.
     */
    fun <L : Layer> pop(): L? = peek<L>()
        ?.also {
            val t = RemoveTransition<Layer>(this, layerStack.size - 1)
            addTransition(t)
        }

    /**
     * Pops Layers out of the stack until a Layer with the given [name].
     *
     * @param name the name of the Layer till which Layers should be popped out from the stack.
     * @param inclusive the flag indicates whether to remove the Layer with the given name or not.
     */
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

                @Suppress("UNCHECKED_CAST")
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

        @Suppress("UNCHECKED_CAST")
        return lastEntry?.layerInstance as L?
    }

    /**
     * Handles `Back` pressing event.
     *
     * @return true if the event has been handled, false if the caller should handle the event.
     */
    fun onBackPressed(): Boolean {
        return layerStack.lastOrNull {
            val layer = it.layerInstance
            it.valid && layer != null && layer.onBackPressed()
        } != null
    }

    /**
     * Removes all Layers from the stack one by one.
     */
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

    /**
     * Returns a Layer from the top of the stack, if there's any.
     */
    fun <L : Layer> peek(): L? = get(layerStack.size - 1)

    /**
     * Returns a Layer from the stack by the given index.
     */
    @Suppress("UNCHECKED_CAST")
    fun <L : Layer> get(index: Int): L? = layerStack.getOrNull(index)?.layerInstance as L?

    /**
     * Looks up the topmost Layer with the given name in the stack.
     */
    @Suppress("UNCHECKED_CAST")
    fun <L : Layer> find(name: String?): L? =
        layerStack.lastOrNull { it.name == name }?.layerInstance as L?

    /**
     * Returns an index of the topmost Layer in the stack with the given name.
     */
    fun indexOf(name: String?): Int = layerStack.indexOfLast { it.name == name }

    /**
     * Looks up the given Layer's index in the stack.
     */
    fun indexOf(layer: Layer): Int = layerStack.indexOfLast { it.layerInstance === layer }

    //endregion

    //region Lifecycle

    /**
     * Saves the state of this manager and all nested managers (at different containers).
     *
     * @see Layers
     */
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

        return outState.takeIf { it.size() > 0 }
    }

    /**
     * Initiates the end of life of the manager and all its nested managers.
     */
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

    /**
     * Moves the Layer to a particular state.
     *
     * @param entry the stack entry.
     * @param index the index in the stack.
     * @param toState the target state.
     * @param saveState the flag indicating whether the state should be saved or not in case the
     * Layer or its view gets destroyed.
     */
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

    /**
     * Creates an instance of the Layer and puts it into the given stack entry.
     */
    private fun createLayer(entry: StackEntry) {
        entry.instantiateLayer()
            .create(host, entry.arguments, entry.name, entry.layerState)
    }

    /**
     * Creates a view for the Layer in stack entry.
     */
    private fun createView(entry: StackEntry, index: Int) {
        val container = getContainer()
        val layer = entry.layerInstance ?: throw IllegalStateException("Layer instance must exist")
        layer.createView(
            entry.layerState,
            if (layer.isViewInLayout) container else null,
            entry.layoutResId
        )
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

    /**
     * Restores the view state that was saved before the view was destroyed due to lifecycle.
     */
    private fun restoreViewState(entry: StackEntry) {
        val savedState = entry.viewState
        entry.layerInstance?.restoreViewState(savedState)
    }

    /**
     * Saves the view state before the view is going to be destroyed.
     */
    private fun saveViewState(entry: StackEntry) {
        val viewState = SparseArray<Parcelable>()
        entry.layerInstance?.saveViewState(viewState)
        if (viewState.size() > 0) {
            entry.viewState = viewState
        }
    }

    /**
     * Destroys the view.
     */
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

    /**
     * Saves the state of the Layer. It contains both states for the view and custom state from
     * the Layer.
     * The state of the view is not accessible from the subclasses and is managed automatically.
     */
    private fun saveLayerState(entry: StackEntry) {
        val bundle = Bundle()
        entry.layerInstance?.saveLayerState(bundle)
        if (bundle.size() > 0) {
            entry.layerState = bundle
        }
    }

    /**
     * Destroys the instance of Layer.
     */
    private fun destroyLayer(entry: StackEntry, saveState: Boolean) {
        if (saveState) {
            saveLayerState(entry)
        }
        entry.layerInstance?.destroy(!saveState) // XXX
    }

    //endregion

    //region Transition

    /**
     * Adds a transition to the list of transition. Immediately starts the transition if there's
     * only one in the list.
     * Consequent transitions will be started at the end of previous transition.
     */
    internal fun addTransition(t: Transition<*>) {
        transitions.offer(t)
        if (transitions.size == 1) {
            transitions.peek().start()
        }
    }

    /**
     * Starts next transition in the list.
     */
    internal fun nextTransition() {
        transitions.poll()
        if (!transitions.isEmpty()) {
            transitions.peek().start()
        }
    }

    //endregion

    //region Internal tools

    /**
     * Adds an entry to the stack.
     */
    internal fun addStackEntry(entry: StackEntry, index: Int = -1) {
        if (index == -1) {
            layerStack.add(entry)
        } else {
            layerStack.add(index, entry)
        }
        ensureViews()
    }

    /**
     * Provides an entry by the index.
     *
     * @throws IndexOutOfBoundsException
     */
    internal fun getStackEntryAt(index: Int): StackEntry = layerStack[index]

    /**
     * Silently removes the Layer from the stack at the given index, with no animations.
     * Does not throw an exception if the index is incorrect.
     */
    internal fun <L : Layer> removeLayerAt(index: Int): L? {
        val entry = layerStack.getOrNull(index) ?: return null

        moveToState(entry, index, StackEntry.LAYER_STATE_DESTROYED, false)

        @Suppress("UNCHECKED_CAST")
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

    /**
     * Goes in stack from top to bottom and ensures the views are in correct state. Layers can be
     * transparent, which means underlying view must also be created. If a Layer does not have the
     * view, but it should be, the Layer will be moved into the corresponding state with the view.
     */
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

    /**
     * Returns the container where the views will be placed.
     */
    private fun getContainer(): ViewGroup = container.or {
        when (containerId) {
            View.NO_ID -> host.defaultContainer
            else -> host.getView(containerId)
        }.also {
            // We save state manually
            it.isSaveFromParentEnabled = false
            container = it
        }
    }

    //endregion

    //region Extensions

    /**
     * Reified extension to add a Layer.
     */
    inline fun <reified L : Layer> add(noinline block: (Transition<L>.() -> Unit)? = null) {
        add(L::class.java, block)
    }

    /**
     * Reified extension to replace a Layer.
     */
    inline fun <reified L : Layer> replace(noinline block: (Transition<L>.() -> Unit)? = null) {
        replace(L::class.java, block)
    }

    //endregion
}