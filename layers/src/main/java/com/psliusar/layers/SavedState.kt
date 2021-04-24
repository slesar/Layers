package com.psliusar.layers

import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class SavedState<N>(
    private val type: Class<N>
) : ReadWriteProperty<Layer, N> {

    override fun getValue(thisRef: Layer, property: KProperty<*>): N {
        val bundle = thisRef.stateOrArguments ?: Bundle()
        val key = property.name
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

    override fun setValue(thisRef: Layer, property: KProperty<*>, value: N) {
        val bundle = thisRef.state
        val key = property.name
        when (type) {
            Int::class.java, Integer.TYPE, java.lang.Integer::class.java -> bundle.putInt(property.name, (value as Number).toInt())
            Long::class.java, java.lang.Long.TYPE, java.lang.Long::class.java -> bundle.putLong(key, (value as Number).toLong())
            Float::class.java, java.lang.Float.TYPE, java.lang.Float::class.java -> bundle.putFloat(key, (value as Number).toFloat())
            Double::class.java, java.lang.Double.TYPE, java.lang.Double::class.java -> bundle.putDouble(key, (value as Number).toDouble())
            Boolean::class.java, java.lang.Boolean.TYPE, java.lang.Boolean::class.java -> bundle.putBoolean(key, value as Boolean)
            String::class.java -> bundle.putString(key, value as String?)
            CharSequence::class.java -> bundle.putCharSequence(key, value as CharSequence?)
            Serializable::class.java -> bundle.putSerializable(key, value as Serializable?)
            Parcelable::class.java -> bundle.putParcelable(key, value as Parcelable?)
            else -> throw UnsupportedOperationException("Unable to save value of type $type")
        }
    }
}