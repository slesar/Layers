package com.psliusar.layers

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

private const val SAVED_STATE_LAYERS = "LAYERS.SAVED_STATE_LAYERS"

/**
 * A [Fragment] implementing [LayersHost]. May be used as an isolated container to host Layers.
 * Subclasses must specify an ID of default ViewGroup that will host the default stack of layers.
 */
abstract class LayersFragment(
    @LayoutRes contentLayoutId: Int = 0
) : Fragment(contentLayoutId), LayersHost {

    private var _layers: Layers? = null
    override val layers: Layers
        get() {
            ensureLayerViews()
            return _layers ?: throw IllegalStateException("Fragment is not created")
        }

    override val defaultContainer: ViewGroup
        get() = requireView().findViewById(defaultContainerId)

    override val activity: Activity
        get() = requireActivity()

    override val parentLayer: Layer? = null

    @get:IdRes
    abstract val defaultContainerId: Int

    private var layersStateRestored = false

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        layersStateRestored = state != null
        _layers = Layers(this, state?.getBundle(SAVED_STATE_LAYERS))
    }

    override fun onStart() {
        super.onStart()
        ensureLayerViews()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        layers.saveState()?.let {
            outState.putBundle(SAVED_STATE_LAYERS, it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        layers.destroy()
    }

    /**
     * A useful method to pop the stack of layers.
     *
     * @return true if the back press was handled. False if the stack is empty.
     */
    @CallSuper
    open fun onBackPressed(): Boolean =
        _layers?.onBackPressed() == true || _layers?.pop<Layer>() != null

    override fun <T : View> getView(viewId: Int): T = requireView().findViewById(viewId)

    private fun ensureLayerViews() {
        if (layersStateRestored) {
            _layers?.resumeView()
            layersStateRestored = false
        }
    }
}
