package com.psliusar.layers

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View

/**
 * A callback for most important methods of the [Layer].
 */
interface LayerDelegate {

    /**
     * Overrides the respective value in [Layer] - [Layer.isViewInLayout].
     */
    val isViewInLayout: Boolean

    /**
     * Could provide custom [LayoutInflater] when non-null value is provided.
     */
    val layoutInflater: LayoutInflater?

    /**
     * Gets called when the respective method is called - [Layer.onCreate].
     */
    fun onCreate(savedState: Bundle?)

    /**
     * Gets called when the respective method is called - [Layer.onAttach].
     */
    fun onAttach()

    /**
     * Gets called when the respective method is called - [Layer.onBindView].
     */
    fun onBindView(savedState: Bundle?, view: View)

    /**
     * Gets called when the respective method is called - [Layer.restoreViewState].
     */
    fun restoreViewState(inState: SparseArray<Parcelable>?)

    /**
     * Gets called when the respective method is called - [Layer.saveViewState].
     */
    fun saveViewState(outState: SparseArray<Parcelable>)

    /**
     * Gets called when the respective method is called - [Layer.onDetach].
     */
    fun onDetach()

    /**
     * Gets called when the respective method is called - [Layer.onDestroyView].
     */
    fun onDestroyView()

    /**
     * Gets called when the respective method is called - [Layer.dismiss].
     */
    fun onDismiss()

    /**
     * Gets called when the respective method is called - [Layer.saveLayerState].
     */
    fun saveLayerState(outState: Bundle)
}
