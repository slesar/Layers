package com.psliusar.layers.state

import com.psliusar.layers.Layer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Reads and writes value into the state bundle of the [Layer]. These values will be preserved when
 * the [Layer]'s instance is recreated.
 */
internal class SavedState<T>(
    type: Class<T>
) : StateWrapper<T>(type), ReadWriteProperty<Layer, T?> {

    override fun getValue(thisRef: Layer, property: KProperty<*>): T? {
        val bundle = thisRef.state
        return getValue(bundle, property.name)
    }

    override fun setValue(thisRef: Layer, property: KProperty<*>, value: T?) {
        val bundle = thisRef.state
        saveValue(bundle, property.name, value)
    }
}