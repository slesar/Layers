package com.psliusar.layers

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View

interface LayerDelegate {

    val isViewInLayout: Boolean

    val layoutInflater: LayoutInflater?

    fun onCreate(savedState: Bundle?)

    fun onAttach()

    fun onBindView(savedState: Bundle?, view: View)

    fun restoreViewState(inState: SparseArray<Parcelable>?)

    fun saveViewState(outState: SparseArray<Parcelable>)

    fun onDetach()

    fun onDestroyView()

    fun onDismiss()

    fun saveLayerState(outState: Bundle)
}
