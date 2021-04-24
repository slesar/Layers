package com.psliusar.layers

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup

private const val STATE_STRING_KEY = "STATE_STRING_KEY"
private const val STATE_STRING_VALUE = "STATE_STRING_VALUE"

private const val ARGS_KEY = "ARGS_KEY"
private const val ARGS_VALUE = "ARGS_VALUE"

class MockedLayer : Layer() {

    var createCalled = 0
        private set
    var onCreateCalled = 0
        private set
    var onCreateViewCalled = 0
        private set
    var onBindViewCalled = 0
        private set
    var restoreViewStateCalled = 0
        private set
    var onAttachCalled = 0
        private set
    var onDetachCalled = 0
        private set
    var saveViewStateCalled = 0
        private set
    var onDestroyViewCalled = 0
        private set
    var onDestroyCalled = 0
        private set
    var destroyCalled = 0
        private set

    var stateRestored = false
        private set
    var viewStateRestored = false
        private set
    var argumentsPassed = false
        private set

    override fun create(host: LayersHost, arguments: Bundle?, name: String?, savedState: Bundle?) {
        super.create(host, arguments, name, savedState)
        createCalled++
    }

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        onCreateCalled++
        stateRestored = savedState?.getString(STATE_STRING_KEY) == STATE_STRING_VALUE
        argumentsPassed = arguments?.getString(ARGS_KEY) == ARGS_VALUE
    }

    override fun onCreateView(savedState: Bundle?, parent: ViewGroup?): View? {
        onCreateViewCalled++
        return inflate(0, parent)
    }

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)
        onBindViewCalled++
    }

    override fun restoreViewState(inState: SparseArray<Parcelable>?) {
        super.restoreViewState(inState)
        restoreViewStateCalled++
        viewStateRestored = inState != null
    }

    override fun onAttach() {
        super.onAttach()
        onAttachCalled++
    }

    override fun onDetach() {
        super.onDetach()
        onDetachCalled++
    }

    override fun saveViewState(outState: SparseArray<Parcelable>) {
        super.saveViewState(outState)
        saveViewStateCalled++
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onDestroyViewCalled++
    }

    override fun saveLayerState(outState: Bundle) {
        super.saveLayerState(outState)
        // TODO counter
    }

    override fun onSaveLayerState(outState: Bundle) {
        super.onSaveLayerState(outState)
        // TODO counter
        outState.putString(STATE_STRING_KEY, STATE_STRING_VALUE)
    }

    override fun onDestroy() {
        super.onDestroy()
        onDestroyCalled++
    }

    override fun destroy(finish: Boolean) {
        super.destroy(finish)
        destroyCalled++
    }

    companion object {
        fun createArguments(): Bundle {
            val bundle = Bundle()
            bundle.putString(ARGS_KEY, ARGS_VALUE)
            return bundle
        }
    }
}