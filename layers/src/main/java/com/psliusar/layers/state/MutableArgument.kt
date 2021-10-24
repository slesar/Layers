package com.psliusar.layers.state

import android.os.Bundle
import com.psliusar.layers.Layer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Reads and writes values into arguments bundle of the [Layer].
 */
internal class MutableArgument<T>(
    type: Class<T>,
    private val key: String
) : StateWrapper<T>(type), ReadWriteProperty<Layer, T> {

    override fun getValue(thisRef: Layer, property: KProperty<*>): T {
        val bundle = thisRef.stateOrArguments ?: Bundle.EMPTY
        return getValue(bundle, key) ?: throw IllegalStateException("Value of $key is null")
    }

    override fun setValue(thisRef: Layer, property: KProperty<*>, value: T) {
        val bundle = thisRef.state
        saveValue(bundle, key, value)
    }
}