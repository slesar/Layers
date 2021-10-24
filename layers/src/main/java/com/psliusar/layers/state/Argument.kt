package com.psliusar.layers.state

import com.psliusar.layers.Layer
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Provides values from the arguments Bundle of the [Layer].
 */
internal class Argument<T>(
    type: Class<T>,
    private val key: String
) : StateWrapper<T>(type), ReadOnlyProperty<Layer, T> {

    override fun getValue(thisRef: Layer, property: KProperty<*>): T {
        val bundle = thisRef.arguments
        if (bundle == null || !bundle.containsKey(key)) {
            throw IllegalStateException("Argument $key of type $type is not provided")
        }
        return getValue(bundle, key) ?: throw IllegalStateException("Value of $key is null")
    }
}