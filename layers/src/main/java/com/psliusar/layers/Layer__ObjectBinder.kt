package com.psliusar.layers

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.psliusar.layers.binder.ObjectBinder

private const val VIEW_MODEL_KEY = "_LAYER::VIEW_MODEL_STORE"

/**
 * Base binder for [Layer] that manages lifecycle for [ViewModel]s.
 */
@Suppress("ClassName")
open class Layer__ObjectBinder : ObjectBinder() {

    override fun save(target: Any, state: Bundle) {
        super.save(target, state)
        /*(target as Layer<*>)._viewModel?.takeIf { it.isPersistent() }?.let { vm ->
            putViewModel(VIEW_MODEL_KEY, vm, state)
        }*/
        (target as Layer).viewModelStore?.let { vm ->
            putViewModelStore(VIEW_MODEL_KEY, vm, state)
        }
    }

    override fun restore(target: Any, state: Bundle) {
        super.restore(target, state)
        /*target as Layer<*>
        target._viewModel = getViewModel(VIEW_MODEL_KEY, state)*/
        target as Layer
        target.viewModelStore = getViewModelStore(VIEW_MODEL_KEY, state)
    }
}
