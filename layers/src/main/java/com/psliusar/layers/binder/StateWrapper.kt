package com.psliusar.layers.binder

import android.os.Bundle

abstract class StateWrapper<T>(
    protected val type: Class<T>
) {

    @Suppress("UNCHECKED_CAST")
    fun getValue(bundle: Bundle, key: String): T = StateWrapperHelper.restoreValue(bundle, type, key) as T

    fun saveValue(bundle: Bundle, key: String, value: T?) = StateWrapperHelper.saveValue(bundle, type, key, value)
}