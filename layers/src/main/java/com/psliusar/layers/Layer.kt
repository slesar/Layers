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
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty

private const val SAVED_STATE_CHILD_LAYERS = "LAYER.SAVED_STATE_CHILD_LAYERS"
private const val SAVED_STATE_INTERNAL = "LAYER.SAVED_STATE_INTERNAL"
private const val SAVED_STATE_CUSTOM = "LAYER.SAVED_STATE_CUSTOM"

abstract class Layer(
    @LayoutRes private val layoutId: Int = 0
) : LayersHost, LifecycleOwner, ViewModelStoreOwner {

    private var _layers: Layers? = null
    override val layers: Layers
        get() = _layers.or { Layers(this, null).also { _layers = it } }

    override val defaultContainer: ViewGroup
        get() = view as ViewGroup? ?: throw IllegalStateException("Layer does not have View")

    override val activity: Activity
        get() = host.activity

    override val parentLayer: Layer?
        get() = host as? Layer

    protected val context: Context
        get() = host.activity.applicationContext

    private var _host: LayersHost? = null

    /** */
    protected val host: LayersHost
        get() = _host ?: throw IllegalStateException("Layer is not attached to Activity")

    var arguments: Bundle? = null
        private set

    var name: String? = null
        private set

    var isFromSavedState = false
        private set

    var view: View? = null
        private set

    var isAttached: Boolean = false
        internal set

    var isFinishing: Boolean = false
        private set

    val isViewInLayout: Boolean
        get() = delegate?.isViewInLayout ?: true

    protected val layoutInflater: LayoutInflater
        get() = delegate?.layoutInflater ?: host.activity.layoutInflater

    internal var viewModelStore: ViewModelStore? by ViewModelStoreState()

    private var _state: Bundle? = null
    internal val state: Bundle
        get() = _state.or { Bundle().also { _state = it } }

    internal val stateOrArguments: Bundle?
        get() = _state ?: arguments

    private var delegate: LayerDelegate? = null

    private val activityLifecycle: Lifecycle
        get() = (host.activity as LifecycleOwner).lifecycle

    private val activityLifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(value = Lifecycle.Event.ON_START)
        fun onStart() = onActivityStart()

        @OnLifecycleEvent(value = Lifecycle.Event.ON_RESUME)
        fun onResume() = onActivityResume()

        @OnLifecycleEvent(value = Lifecycle.Event.ON_PAUSE)
        fun onPause() = onActivityPause()

        @OnLifecycleEvent(value = Lifecycle.Event.ON_STOP)
        fun onStop() = onActivityStop()
    }

    private val lifecycleRegistry = LifecycleRegistry(this)

    private var _viewLifecycleOwner: LayerViewLifecycleOwner? = null
    val viewLifecycleOwner: LifecycleOwner
        get() = _viewLifecycleOwner ?: throw IllegalStateException("View is not initialized")

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

    @CallSuper
    open fun onCreate(savedState: Bundle?) {
        delegate?.onCreate(savedState)
    }

    internal fun update(arguments: Bundle) {
        onUpdate(arguments)
    }

    open fun onUpdate(arguments: Bundle) {

    }

    override fun getViewModelStore(): ViewModelStore {
        return viewModelStore ?: ViewModelStore().also { viewModelStore = it }
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    protected fun <VM : ViewModel> getViewModel(modelClass: Class<VM>): VM {
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(this, factory).get(modelClass)
    }

    protected inline fun <reified VM : ViewModel> getViewModel(): VM = getViewModel(VM::class.java)

    internal fun createView(savedState: Bundle?, parent: ViewGroup?, @LayoutRes layoutResId: Int) {
        view = when {
            layoutResId != 0 -> inflate(layoutResId, parent)
            layoutId != 0 -> inflate(layoutId, parent)
            else -> onCreateView(savedState, parent)
        }
        if (view != null) {
            _viewLifecycleOwner = LayerViewLifecycleOwner()
            _viewLifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }
    }

    open fun onCreateView(savedState: Bundle?, parent: ViewGroup?): View? = null

    @CallSuper
    open fun onBindView(savedState: Bundle?, view: View) {
        delegate?.onBindView(savedState, view)
    }

    internal open fun restoreViewState(inState: SparseArray<Parcelable>?) {
        inState?.let {
            view?.restoreHierarchyState(it)
        }
        _layers?.resumeView()
        if (delegate != null) {
            delegate!!.restoreViewState(inState)
        }
        val lifecycle = activityLifecycle
        if (lifecycle.currentState >= Lifecycle.State.STARTED) {
            onActivityStart()
        }
        if (lifecycle.currentState >= Lifecycle.State.RESUMED) {
            onActivityResume()
        }
    }

    /**
     * After attach
     */
    @CallSuper
    open fun onAttach() {
        delegate?.onAttach()
    }

    @CallSuper
    open fun onActivityStart() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        _viewLifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    @CallSuper
    open fun onActivityResume() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        _viewLifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    @CallSuper
    open fun onActivityPause() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        _viewLifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    @CallSuper
    open fun onActivityStop() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        _viewLifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    /**
     * Before detach
     */
    @CallSuper
    open fun onDetach() {
        delegate?.onDetach()
    }

    internal open fun saveViewState(outState: SparseArray<Parcelable>) {
        view!!.saveHierarchyState(outState)
        _layers?.pauseView()
        delegate?.saveViewState(outState)
    }

    internal fun destroyView() {
        _viewLifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        _viewLifecycleOwner = null
        onDestroyView()
        _layers?.destroy()
        delegate?.onDestroyView()
        view = null
    }

    @CallSuper
    open fun onDestroyView() {

    }

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

    @CallSuper
    open fun onSaveLayerState(outState: Bundle) {

    }

    internal open fun destroy(finish: Boolean) {
        isFinishing = finish
        onDestroy()
        if (finish) {
            viewModelStore?.clear()
            viewModelStore = null
        }
        activityLifecycle.removeObserver(activityLifecycleObserver)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    @CallSuper
    open fun onDestroy() {

    }

    override fun <T : View> getView(@IdRes viewId: Int): T {
        return findView(viewId)
            ?: throw IllegalArgumentException("Failed to find View with ID " + getResourceName(activity.resources, viewId))
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : View> findView(@IdRes id: Int): T? = view?.findViewById<View>(id) as T?

    fun onBackPressed(): Boolean = _layers?.onBackPressed() ?: false

    /**
     * Removes Layer from the stack.
     */
    fun dismiss() {
        if (isAttached) {
            delegate?.onDismiss().or {
                host.layers.remove(this, null)
            }
        }
    }

    fun <V : View> inflate(@LayoutRes layoutRes: Int, parent: ViewGroup?): V {
        @Suppress("UNCHECKED_CAST")
        return layoutInflater.inflate(layoutRes, parent, false) as V
    }

    fun <T> optParent(parentClass: Class<T>): T? {
        val parent = parentLayer
        return when {
            parent == null -> host.activity.takeIf { parentClass.isInstance(it) }?.let { parentClass.cast(it) }
            parentClass.isInstance(parent) -> parentClass.cast(parent)
            else -> parent.optParent(parentClass)
        }
    }

    fun <T> getParent(parentClass: Class<T>): T {
        return optParent(parentClass) ?: throw NullPointerException("A parent implementing $parentClass not found")
    }

    inline fun <reified T> getParent(): T = getParent(T::class.java)

    open fun getAnimation(animationType: AnimationType): Animator? = null

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

    fun removeDelegate(delegate: LayerDelegate) {
        when (val d = this.delegate) {
            is LayerDelegates -> d.removeDelegate(delegate)
            delegate -> this.delegate = null
        }
    }

    protected inline fun <reified T> savedState(): ReadWriteProperty<Layer, T> = savedState(T::class.java)

    protected fun <T> savedState(type: Class<T>): ReadWriteProperty<Layer, T> = SavedState(type)

    protected inline fun <reified T> argument(key: String): ReadOnlyProperty<Layer, T> = argument(T::class.java, key)

    protected fun <T> argument(type: Class<T>, key: String): ReadOnlyProperty<Layer, T> = Argument(type, key)

    protected inline fun <reified T> mutableArgument(key: String): ReadWriteProperty<Layer, T> = mutableArgument(T::class.java, key)

    protected fun <T> mutableArgument(type: Class<T>, key: String): ReadWriteProperty<Layer, T> = MutableArgument(type, key)

    override fun toString(): String {
        return "${this::class.java.simpleName}{" +
            "name=$name" +
            ", arguments=$arguments" +
            ", attached=$isAttached" +
            ", fromSavedState=$isFromSavedState" +
            ", finishing=$isFinishing" +
            "}"
    }

    private class LayerViewLifecycleOwner : LifecycleOwner {

        private val lifecycleRegistry = LifecycleRegistry(this)

        override fun getLifecycle(): Lifecycle = lifecycleRegistry

        fun handleLifecycleEvent(event: Lifecycle.Event) = lifecycleRegistry.handleLifecycleEvent(event)
    }
}
