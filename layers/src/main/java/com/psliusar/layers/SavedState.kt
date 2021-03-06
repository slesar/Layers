package com.psliusar.layers

import android.os.Bundle
import com.psliusar.layers.binder.StateWrapper
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class SavedState<T>(
    type: Class<T>
) : StateWrapper<T>(type), ReadWriteProperty<Layer, T> {

    override fun getValue(thisRef: Layer, property: KProperty<*>): T {
        val bundle = thisRef.stateOrArguments ?: Bundle.EMPTY
        return getValue(bundle, property.name)
    }

    override fun setValue(thisRef: Layer, property: KProperty<*>, value: T) {
        val bundle = thisRef.state
        saveValue(bundle, property.name, value)
    }
}