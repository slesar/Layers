package com.psliusar.layers

import android.os.Bundle
import com.psliusar.layers.binder.StateWrapper
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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