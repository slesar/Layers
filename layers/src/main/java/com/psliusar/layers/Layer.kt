package com.psliusar.layers

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.psliusar.layers.state.Argument
import com.psliusar.layers.state.MutableArgument
import com.psliusar.layers.state.SavedState
import com.psliusar.layers.state.ViewModelStoreState
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty

private const val SAVED_STATE_CHILD_LAYERS = "LAYER.SAVED_STATE_CHILD_LAYERS"
private const val SAVED_STATE_INTERNAL = "LAYER.SAVED_STATE_INTERNAL"
private const val SAVED_STATE_CUSTOM = "LAYER.SAVED_STATE_CUSTOM"

/**
 * A unit that controls the view in stack. Has its own [Lifecycle] and a [Lifecycle] for its view.
 *
 * Layer can exist without View. It will still be placed in stack and will live according to its
 * lifecycle.
 *
 * This class also serves as [LayersHost], which makes it possible to work with nested layers.
 *
 * Retains its state during configuration changes. Keeps separate states for the Layer itself and
 * its View.
 *
 * Supports [ViewModel] by providing [ViewModelStore].
 */
abstract class Layer(
    @LayoutRes private val layoutId: Int = 0
) : LayersHost, LifecycleOwner {

    private var _layers: Layers? = null
    private var _host: LayersHost? = null
    private var _state: Bundle? = null
    private var _viewLifecycleOwner: LayerViewLifecycleOwner? = null
    private var _vmStore: ViewModelStore? by ViewModelStoreState()
    private var delegate: LayerDelegate? = null
    private val lifecycleRegistry = LifecycleRegistry(this)

    override val layers: Layers
        get() = _layers.or { Layers(this, null).also { _layers = it } }

    override val defaultContainer: ViewGroup
        get() = view as ViewGroup? ?: throw IllegalStateException("Layer does not have View")

    override val activity: Activity
        get() = host.activity

    override val activityLifecycle: Lifecycle
        get() = host.activityLifecycle

    override val parentLayer: Layer?
        get() = host as? Layer

    /**
     * A set of arguments provided to the Layer from the outside.
     *
     * @see [StackEntry.arguments]
     */
    var arguments: Bundle? = null
        private set

    /**
     * Optional name of the Layer, that comes from stack entry.
     *
     * @see [StackEntry.name]
     */
    var name: String? = null
        private set

    /**
     * Indicates that the Layer was recreated and its saved state was applied.
     */
    var isFromSavedState = false
        private set

    /**
     * The View of the Layer. Could be empty.
     */
    var view: View? = null
        private set

    /**
     * Indicates that the Layer is attached to its container at the point where it could display a
     * View. Gets updated even if the View is `null`.
     */
    var isAttached: Boolean = false
        internal set

    /**
     * Indicates that the Layer reaches its end of life. This means the state and view model will
     * not be retained.
     */
    var isFinishing: Boolean = false
        private set

    /**
     * Says whether the [Layer] wants to display its view in the View hierarchy. For example, a
     * Layer could display a dialog which is not part of the current View hierarchy.
     */
    val isViewInLayout: Boolean
        get() = delegate?.isViewInLayout ?: true

    /**
     * Provides [Lifecycle] of the view.
     */
    val viewLifecycleOwner: LifecycleOwner
        get() = _viewLifecycleOwner ?: throw IllegalStateException("View is not initialized")

    /**
     * The state that could be populated with custom values. Will be retained across Layer
     * instances during configuration change.
     */
    internal val state: Bundle
        get() = _state.or { Bundle().also { _state = it } }

    /**
     * Provides either state or arguments Bundle.
     */
    internal val stateOrArguments: Bundle?
        get() = _state ?: arguments

    /**
     * Returns application context
     */
    protected val context: Context
        get() = host.activity.applicationContext

    /**
     * Provides [LayersHost] which this Layer operates on.
     */
    protected val host: LayersHost
        get() = _host ?: throw IllegalStateException("Layer is not attached to LayersHost")

    /**
     * Provides [LayoutInflater] to inflate views.
     */
    protected val layoutInflater: LayoutInflater
        get() = delegate?.layoutInflater ?: host.activity.layoutInflater

    private val activityLifecycleObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> onActivityStart()
            Lifecycle.Event.ON_RESUME -> onActivityResume()
            Lifecycle.Event.ON_PAUSE -> onActivityPause()
            Lifecycle.Event.ON_STOP -> onActivityStop()
            else -> {
                // NO-OP
            }
        }
    }

    private val vmStore: ViewModelStore
        get() = _vmStore ?: ViewModelStore().also { _vmStore = it }

    init {
        // Init Lifecycle
        if (Build.VERSION.SDK_INT >= 19) {
            lifecycleRegistry.addObserver(LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    view?.cancelPendingInputEvents()
                }
            })
        }
    }

    /**
     * Initializes the instance of [Layer].
     */
    internal fun create(host: LayersHost, arguments: Bundle?, name: String?, savedState: Bundle?) {
        _host = host
        this.arguments = arguments
        this.name = name
        isFromSavedState = savedState != null

        savedState?.let { state ->
            state.getBundle(SAVED_STATE_CHILD_LAYERS)?.let {
                _layers = Layers(this, it)
            }
            state.getBundle(SAVED_STATE_INTERNAL)?.let {
                _state = it
            }
        }

        onCreate(savedState?.getBundle(SAVED_STATE_CUSTOM))
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        activityLifecycle.addObserver(activityLifecycleObserver)
    }

    /**
     * Callback called right after the Layer is initialized. This is the very first step in
     * lifecycle.
     * Overriding methods should be calling this method.
     */
    @CallSuper
    open fun onCreate(savedState: Bundle?) {
        delegate?.onCreate(savedState)
    }

    /**
     * This is called when new arguments arrive to the Layer.
     *
     * @see onUpdate
     */
    internal fun update(arguments: Bundle) {
        onUpdate(arguments)
    }

    /**
     * This is called when new arguments arrive to the Layer.
     * When the Layer is placed in stack for the first time, its arguments will be available since
     * Layer's creation. When new arguments are passed to the Layer in stack, this method will be
     * called notifying of new arguments. If the Layer gets re-created (from saved state or due to
     * configuration change), the arguments will be available since Layer's creation.
     */
    open fun onUpdate(arguments: Bundle) {

    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    /**
     * Returns an existing ViewModel or creates a new one.
     */
    protected fun <VM : ViewModel> getViewModel(modelClass: Class<VM>): VM {
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(vmStore, factory).get(modelClass)
    }

    /**
     * Creates the view for the Layer. There are three ways how the view can be created:
     * - Layout ID is specified in the construction of the Layer.
     * - Layout ID is taken from the stack entry (could be specified when adding Layer to stack).
     * - By the callback [onCreateView].
     * Note that the Layer could still exist in stack even without a view.
     */
    internal fun createView(savedState: Bundle?, parent: ViewGroup?, @LayoutRes layoutResId: Int) {
        view = when {
            layoutResId != 0 -> inflate(layoutResId, parent)
            layoutId != 0 -> inflate(layoutId, parent)
            else -> onCreateView(savedState, parent)
        }
        if (view != null) {
            _viewLifecycleOwner = LayerViewLifecycleOwner()
            _viewLifecycleOwner?.event(Lifecycle.Event.ON_CREATE)
        }
    }

    /**
     * Callback to create a view.
     * Note that the Layer could still exist in stack even without a view.
     * Whether the view will be added to the container is controlled by the flag [isViewInLayout].
     */
    open fun onCreateView(savedState: Bundle?, parent: ViewGroup?): View? = null

    /**
     * Called after the moment the view has to be added to a container. Will still be called even
     * if the view is empty or was not attached.
     */
    @CallSuper
    open fun onAttach() {
        delegate?.onAttach()
    }

    /**
     * Called right after the view is created. Useful to created bindings and setup views.
     */
    @CallSuper
    open fun onBindView(savedState: Bundle?, view: View) {
        delegate?.onBindView(savedState, view)
    }

    /**
     * Restores the state of views from the saved state. Called after the view is bound. This brings
     * user's input over any initial state set in [onBindView].
     *
     * @see saveViewState
     */
    internal open fun restoreViewState(inState: SparseArray<Parcelable>?) {
        inState?.let {
            view?.restoreHierarchyState(it)
        }
        _layers?.resumeView()
        delegate?.restoreViewState(inState)
        val lifecycle = activityLifecycle
        if (lifecycle.currentState >= Lifecycle.State.STARTED) {
            onActivityStart()
        }
        if (lifecycle.currentState >= Lifecycle.State.RESUMED) {
            onActivityResume()
        }
    }

    /**
     * Called when the Activity is started.
     */
    @CallSuper
    open fun onActivityStart() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        _viewLifecycleOwner?.event(Lifecycle.Event.ON_START)
    }

    /**
     * Called when the Activity is resumed.
     */
    @CallSuper
    open fun onActivityResume() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        _viewLifecycleOwner?.event(Lifecycle.Event.ON_RESUME)
    }

    /**
     * Called when the Activity is paused.
     */
    @CallSuper
    open fun onActivityPause() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        _viewLifecycleOwner?.event(Lifecycle.Event.ON_PAUSE)
    }

    /**
     * Called when the Activity is stopped.
     */
    @CallSuper
    open fun onActivityStop() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        _viewLifecycleOwner?.event(Lifecycle.Event.ON_STOP)
    }

    /**
     * Called before the moment the view is detached from container.
     */
    @CallSuper
    open fun onDetach() {
        delegate?.onDetach()
    }

    /**
     * Saves the state of the view.
     *
     * @see restoreViewState
     */
    internal open fun saveViewState(outState: SparseArray<Parcelable>) {
        view?.saveHierarchyState(outState)
        _layers?.pauseView()
        delegate?.saveViewState(outState)
    }

    /**
     * Called right before the view is destroyed. Calls any nested Layers to be destroyed.
     */
    internal fun destroyView() {
        _viewLifecycleOwner?.event(Lifecycle.Event.ON_DESTROY)
        _viewLifecycleOwner = null
        onDestroyView()
        _layers?.destroy()
        delegate?.onDestroyView()
        view = null
    }

    /**
     * Called right before the view is destroyed.
     */
    @CallSuper
    open fun onDestroyView() {

    }

    /**
     * Saves the custom state of the Layer.
     *
     * @see create
     */
    internal open fun saveLayerState(outState: Bundle) {
        _layers?.saveState()?.let { outState.putBundle(SAVED_STATE_CHILD_LAYERS, it) }

        _state?.takeIf { it.size() > 0 }?.let {
            outState.putBundle(SAVED_STATE_INTERNAL, it)
        }

        val customState = Bundle()
        onSaveLayerState(customState)
        if (customState.size() > 0) {
            outState.putBundle(SAVED_STATE_CUSTOM, customState)
        }
        delegate?.saveLayerState(outState)
    }

    /**
     * Saves the custom state of the Layer.
     */
    @CallSuper
    open fun onSaveLayerState(outState: Bundle) {

    }

    /**
     * Called when the Layer is about to be destroyed.
     *
     * @param finish indicates whether the Layer has reached its end of life (true) or could be
     * re-created later (false).
     */
    internal open fun destroy(finish: Boolean) {
        isFinishing = finish
        onDestroy()
        if (finish) {
            _vmStore?.clear()
            _vmStore = null
        }
        activityLifecycle.removeObserver(activityLifecycleObserver)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    /**
     * Called when the Layer is about to be destroyed.
     */
    @CallSuper
    open fun onDestroy() {

    }

    override fun <T : View> getView(@IdRes viewId: Int): T {
        return findView(viewId)
            ?: throw IllegalArgumentException(
                "Failed to find View with ID " + getResourceName(activity.resources, viewId)
            )
    }

    /**
     * Attempts to find a view by ID.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : View> findView(@IdRes id: Int): T? = view?.findViewById<View>(id) as T?

    /**
     * Reaction on pressing 'Back'.
     *
     * @return true if the Layer has handled the event by itself and further handling of the event
     * is not required. False if the event should be processed by the caller.
     */
    @CallSuper
    open fun onBackPressed(): Boolean = _layers?.onBackPressed() ?: false

    /**
     * Removes Layer from the stack.
     */
    fun dismiss() {
        if (isAttached) {
            delegate?.onDismiss()
            host.layers.remove(this, null)
        }
    }

    /**
     * Inflates the view from the XML resource.
     */
    fun <V : View> inflate(@LayoutRes layoutRes: Int, parent: ViewGroup?): V {
        @Suppress("UNCHECKED_CAST")
        return layoutInflater.inflate(layoutRes, parent, false) as V
    }

    /**
     * Attempts to lookup the given interface in the hierarchy of parent hosts or Activity.
     *
     * @return the instance of the given interface or `null` if no such instance could be found
     * @see getParent
     */
    fun <T> optParent(parentClass: Class<T>): T? {
        val parent = parentLayer
        return when {
            parent == null -> host.activity.takeIf { parentClass.isInstance(it) }
                ?.let { parentClass.cast(it) }
            parentClass.isInstance(parent) -> parentClass.cast(parent)
            else -> parent.optParent(parentClass)
        }
    }

    /**
     * Finds the interface up in the hierarchy.
     *
     * @return the instance of the given interface or throws [NullPointerException]
     * @see optParent
     */
    fun <T> getParent(parentClass: Class<T>): T {
        return optParent(parentClass)
            ?: throw NullPointerException("A parent implementing $parentClass not found")
    }

    /**
     * Returns a custom animation for the Layer for the given [AnimationType].
     */
    open fun getAnimation(animationType: AnimationType): Animator? = null

    /**
     * Called when the Layer animation is started.
     */
    open fun onAnimationStart(type: AnimationType) {

    }

    /**
     * Called when the Layer animation is finished.
     */
    open fun onAnimationFinish(type: AnimationType) {

    }

    /**
     * Adds [LayerDelegate] to the Layer.
     */
    fun addDelegate(delegate: LayerDelegate) {
        when (val d = this.delegate) {
            null -> this.delegate = delegate
            is LayerDelegates -> d.addDelegate(delegate)
            else -> {
                val delegates = LayerDelegates()
                delegates.addDelegate(d)
                delegates.addDelegate(delegate)
                this.delegate = delegates
            }
        }
    }

    /**
     * Removes [LayerDelegate] from the Layer.
     */
    fun removeDelegate(delegate: LayerDelegate) {
        when (val d = this.delegate) {
            is LayerDelegates -> d.removeDelegate(delegate)
            delegate -> this.delegate = null
        }
    }

    /**
     * Creates read-write property that is retained in saved state of the Layer.
     */
    protected fun <T> savedState(type: Class<T>): ReadWriteProperty<Layer, T?> =
        SavedState(type)

    /**
     * Creates read-only property that reads the value from Layer's arguments.
     */
    protected fun <T> argument(type: Class<T>, key: String): ReadOnlyProperty<Layer, T> =
        Argument(type, key)

    /**
     * Creates read-write property that initially reads the value from arguments and writes to a
     * saved state of the Layer.
     */
    protected fun <T> mutableArgument(type: Class<T>, key: String): ReadWriteProperty<Layer, T> =
        MutableArgument(type, key)

    override fun toString(): String {
        return "${this::class.simpleName}{" +
            "name=$name" +
            ", arguments=$arguments" +
            ", attached=$isAttached" +
            ", fromSavedState=$isFromSavedState" +
            ", finishing=$isFinishing" +
            "}"
    }

    //region Extensions

    /**
     * A reified extension to [getParent].
     */
    inline fun <reified T> getParent(): T = getParent(T::class.java)

    /**
     * A reified extension to [getViewModel].
     */
    protected inline fun <reified VM : ViewModel> getViewModel(): VM = getViewModel(VM::class.java)

    /**
     * A delegate to read-write property that is retained in saved state of the Layer.
     */
    protected inline fun <reified T> savedState(): ReadWriteProperty<Layer, T?> =
        savedState(T::class.java)

    /**
     * A delegate to read-only property that reads values from arguments.
     */
    protected inline fun <reified T> argument(key: String): ReadOnlyProperty<Layer, T> =
        argument(T::class.java, key)

    /**
     * A delegate to read-write property that reads from arguments and writes to saved state.
     */
    protected inline fun <reified T> mutableArgument(key: String): ReadWriteProperty<Layer, T> =
        mutableArgument(T::class.java, key)

    //endregion

    /**
     * Helper class to store and provide [Lifecycle] to listeners.
     */
    private class LayerViewLifecycleOwner : LifecycleOwner {

        private val lifecycleRegistry = LifecycleRegistry(this)

        override fun getLifecycle(): Lifecycle = lifecycleRegistry

        fun event(event: Lifecycle.Event) = lifecycleRegistry.handleLifecycleEvent(event)
    }
}
