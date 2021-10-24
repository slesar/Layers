package com.psliusar.layers.state

import android.os.Bundle

/**
 * Base class for saving and restoring values from [Bundle].
 */
internal abstract class StateWrapper<T>(
    protected val type: Class<T>
) {

    /**
     * Attempts to read a value by the given key from the given [bundle].
     */
    @Suppress("UNCHECKED_CAST")
    fun getValue(bundle: Bundle, key: String): T? =
        StateWrapperHelper.restoreValue(bundle, type, key)

    /**
     * Writes the value by the given key into the given [bundle].
     */
    fun saveValue(bundle: Bundle, key: String, value: T?) =
        StateWrapperHelper.saveValue(bundle, type, key, value)
}