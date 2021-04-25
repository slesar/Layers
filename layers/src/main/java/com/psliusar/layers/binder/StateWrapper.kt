package com.psliusar.layers.binder

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.ViewModelStore
import java.io.Serializable
import java.util.Arrays

abstract class StateWrapper<T>(
    protected val type: Class<T>
) {

    @Suppress("UNCHECKED_CAST")
    fun getValue(bundle: Bundle, key: String): T = when (type) {
        Int::class.java, Integer.TYPE, java.lang.Integer::class.java -> bundle.getInt(key) as T
        Long::class.java, java.lang.Long.TYPE, java.lang.Long::class.java -> bundle.getLong(key) as T
        Float::class.java, java.lang.Float.TYPE, java.lang.Float::class.java -> bundle.getFloat(key) as T
        Double::class.java, java.lang.Double.TYPE, java.lang.Double::class.java -> bundle.getDouble(key) as T
        Boolean::class.java, java.lang.Boolean.TYPE, java.lang.Boolean::class.java -> bundle.getBoolean(key) as T
        String::class.java -> bundle.getString(key) as T
        CharSequence::class.java -> bundle.getCharSequence(key) as T
        Serializable::class.java -> bundle.getSerializable(key) as T
        Parcelable::class.java -> bundle.getParcelable<Parcelable>(key) as T
        ViewModelStore::class.java -> bundle.getParcelable<SaveWrapper>(key)?.value as T
        else -> StateWrapperHelper.getValue(bundle, type, key) as T
    }

    fun saveValue(bundle: Bundle, key: String, value: Any?) = when (type) {
        Int::class.java, Integer.TYPE, java.lang.Integer::class.java -> bundle.putInt(key, (value as Number).toInt())
        Long::class.java, java.lang.Long.TYPE, java.lang.Long::class.java -> bundle.putLong(key, (value as Number).toLong())
        Float::class.java, java.lang.Float.TYPE, java.lang.Float::class.java -> bundle.putFloat(key, (value as Number).toFloat())
        Double::class.java, java.lang.Double.TYPE, java.lang.Double::class.java -> bundle.putDouble(key, (value as Number).toDouble())
        Boolean::class.java, java.lang.Boolean.TYPE, java.lang.Boolean::class.java -> bundle.putBoolean(key, value as Boolean)
        String::class.java -> bundle.putString(key, value as String?)
        CharSequence::class.java -> bundle.putCharSequence(key, value as CharSequence?)
        Serializable::class.java -> bundle.putSerializable(key, value as Serializable?)
        Parcelable::class.java -> bundle.putParcelable(key, value as Parcelable?)
        ViewModelStore::class.java -> (value as ViewModelStore?)?.let { bundle.putParcelable(key, SaveWrapper(it)) }
        else -> StateWrapperHelper.saveValue(bundle, type, key, value as T?)
    }

    fun initClassLoader(state: Bundle, target: Any) {
        state.classLoader = target.javaClass.classLoader
    }

    fun <T> copyParcelableArray(array: Array<Parcelable?>?, targetClass: Class<out Array<T>?>): Array<T>? {
        return if (array == null) null else Arrays.copyOf(array, array.size, targetClass)
    }

    fun <T> copySerializableArray(array: Array<Serializable?>?, targetClass: Class<out Array<T>?>): Array<T>? {
        return if (array == null) null else Arrays.copyOf(array, array.size, targetClass)
    }
}