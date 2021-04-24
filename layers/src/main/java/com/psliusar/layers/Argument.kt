package com.psliusar.layers

import android.os.Parcelable
import java.io.Serializable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal class Argument<N>(
    private val type: Class<N>,
    private val key: String
) : ReadOnlyProperty<Layer, N> {

    override fun getValue(thisRef: Layer, property: KProperty<*>): N {
        val bundle = thisRef.arguments
        if (bundle == null || !bundle.containsKey(key)) {
            throw IllegalStateException("Argument $key of type $type is not provided")
        }
        return when (type) {
            Int::class.java, Integer.TYPE, java.lang.Integer::class.java -> bundle.getInt(key) as N
            Long::class.java, java.lang.Long.TYPE, java.lang.Long::class.java -> bundle.getLong(key) as N
            Float::class.java, java.lang.Float.TYPE, java.lang.Float::class.java -> bundle.getFloat(key) as N
            Double::class.java, java.lang.Double.TYPE, java.lang.Double::class.java -> bundle.getDouble(key) as N
            Boolean::class.java, java.lang.Boolean.TYPE, java.lang.Boolean::class.java -> bundle.getBoolean(key) as N
            String::class.java -> bundle.getString(key) as N
            CharSequence::class.java -> bundle.getCharSequence(key) as N
            Serializable::class.java -> bundle.getSerializable(key) as N
            Parcelable::class.java -> bundle.getParcelable<Parcelable>(key) as N
            else -> throw UnsupportedOperationException("Unable to retrieve value of type $type")
        }
    }
}