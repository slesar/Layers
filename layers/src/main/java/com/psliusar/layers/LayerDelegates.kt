package com.psliusar.layers

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View

internal class LayerDelegates : LayerDelegate {

    private val delegates = mutableListOf<LayerDelegate>()

    fun addDelegate(delegate: LayerDelegate) {
        delegates.add(delegate)
    }

    fun removeDelegate(delegate: LayerDelegate) {
        delegates.remove(delegate)
    }

    override val isViewInLayout: Boolean
        get() = delegates.any { it.isViewInLayout }

    override val layoutInflater: LayoutInflater?
        get() {
            delegates.forEach { delegate ->
                delegate.layoutInflater?.let {
                    return it
                }
            }
            return null
        }

    override fun onCreate(savedState: Bundle?) {
        delegates.forEach {
            it.onCreate(savedState)
        }
    }

    override fun onAttach() {
        delegates.forEach {
            it.onAttach()
        }
    }

    override fun onBindView(savedState: Bundle?, view: View) {
        delegates.forEach {
            it.onBindView(savedState, view)
        }
    }

    override fun restoreViewState(inState: SparseArray<Parcelable>?) {
        delegates.forEach {
            it.restoreViewState(inState)
        }
    }

    override fun saveViewState(outState: SparseArray<Parcelable>) {
        delegates.forEach {
            it.saveViewState(outState)
        }
    }

    override fun onDetach() {
        delegates.forEach {
            it.onDetach()
        }
    }

    override fun onDestroyView() {
        delegates.forEach {
            it.onDestroyView()
        }
    }

    override fun onDismiss() {
        delegates.forEach {
            it.onDismiss()
        }
    }

    override fun saveLayerState(outState: Bundle) {
        delegates.forEach {
            it.saveLayerState(outState)
        }
    }
}