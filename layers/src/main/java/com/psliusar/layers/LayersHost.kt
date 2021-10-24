package com.psliusar.layers

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes

/**
 * Layers manager works at the special host that must meet some requirements. This interface
 * declares these requirements.
 */
interface LayersHost {

    /**
     * Provides an instance of Layers manager which operates on this host.
     */
    val layers: Layers

    /**
     * Default container for default stack of layers. There could be several stacks of layers at the
     * same host. Please refer to [Layers.at] for details.
     */
    val defaultContainer: ViewGroup

    /**
     * The [Activity].
     * Important: the Activity should implement LifecycleOwner interface.
     */
    val activity: Activity

    /**
     * If the current host is a [Layer], it will be returned. If the host is not a [Layer], then
     * `null` will be returned. Layers can use this property to communicate with layers up in
     * hierarchy.
     */
    val parentLayer: Layer?

    /**
     * Returns a [View] by its ID.
     * Host must be able to provide its views by ID. In particular, when layers are stacked in a
     * non-default container, this method will provide the container View by its ID.
     */
    fun <T : View> getView(@IdRes viewId: Int): T
}
