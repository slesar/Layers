package com.psliusar.layers

import android.app.Activity
import android.view.View
import android.view.ViewGroup

import androidx.annotation.IdRes

interface LayersHost {

    val layers: Layers

    val defaultContainer: ViewGroup

    val activity: Activity

    val parentLayer: Layer?

    fun <T : View> getView(@IdRes viewId: Int): T
}
