package com.psliusar.layers.state

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import androidx.lifecycle.ViewModelStore
import java.io.Serializable
import java.util.ArrayList
import java.util.Arrays

/**
 * Helper object to save values into [Bundle] and read values from it.
 */
internal object StateWrapperHelper {

    /**
     * Restores a value of the given [type] by the [key] from the given [bundle].
     * Not all types are supported. Refer to [isSupported] for all supported types.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> restoreValue(bundle: Bundle, type: Class<T>, key: String): T? {
        val desc = getTypeDescription(type, null)

        if (!isSupported(desc)) {
            throw UnsupportedOperationException("Unable to restore value of type $type")
        }

        // Custom classes have to be deserialized with proper class loader.
        if (desc.customType) {
            bundle.classLoader = javaClass.classLoader
        }

        return if (desc.isArray) {
            val restored = bundle.get(key) as T?
            when (desc.type) {
                Serializable::class.java -> restored
                Parcelable::class.java -> {
                    // Parcelable array needs to be copied to an array of proper type
                    val innerType = java.lang.reflect.Array.newInstance(desc.innerType, 0)::class.java
                    copyParcelableArray(restored as Array<Parcelable?>?, innerType as Class<out Array<Parcelable>?>) as T?
                }
                else -> restored
            }
        } else if (!desc.isArrayList && !desc.isArray) {
            when (desc.type) {
                SparseArray::class.java -> bundle.getSparseParcelableArray<Parcelable>(key) as T?
                // ViewModelStore is being stored in Bundle just to be able to handle it over to
                // a new instance of Activity
                ViewModelStore::class.java -> bundle.getParcelable<SaveWrapper>(key)?.value as T?
                else -> bundle.get(key) as T?
            }
        } else {
            bundle.get(key) as T?
        }
    }

    /**
     * Saves a value of the given [type] by the [key] in the given [bundle].
     * Not all types are supported. Refer to [isSupported] for all supported types.
     */
    fun <T> saveValue(bundle: Bundle, type: Class<T>, key: String, value: T?) {
        value ?: return
        val desc = getTypeDescription(type, value)

        if (desc.isArrayList) {
            when (desc.type) {
                Int::class.java -> bundle.putIntegerArrayList(key, value as ArrayList<Int>)
                String::class.java -> bundle.putStringArrayList(key, value as ArrayList<String>)
                CharSequence::class.java -> bundle.putCharSequenceArrayList(key, value as ArrayList<CharSequence>)
                Parcelable::class.java -> bundle.putParcelableArrayList(key, value as ArrayList<out Parcelable>)
                else -> throw UnsupportedOperationException("Unable to save value of type $type")
            }
        } else if (desc.isArray) {
            when (desc.type) {
                Boolean::class.java -> bundle.putBooleanArray(key, value as BooleanArray)
                Int::class.java -> bundle.putIntArray(key, value as IntArray)
                Long::class.java -> bundle.putLongArray(key, value as LongArray)
                Float::class.java -> bundle.putFloatArray(key, value as FloatArray)
                Double::class.java -> bundle.putDoubleArray(key, value as DoubleArray)
                Byte::class.java -> bundle.putByteArray(key, value as ByteArray)
                Short::class.java -> bundle.putShortArray(key, value as ShortArray)
                Char::class.java -> bundle.putCharArray(key, value as CharArray)
                String::class.java -> bundle.putStringArray(key, value as Array<String>)
                CharSequence::class.java -> bundle.putCharSequenceArray(key, value as Array<CharSequence>)
                Serializable::class.java -> bundle.putSerializable(key, value as Array<Serializable>)
                Parcelable::class.java -> bundle.putParcelableArray(key, value as Array<Parcelable>)
                else -> throw UnsupportedOperationException("Unable to save value of type $type")
            } as T?
        } else {
            when (desc.type) {
                Boolean::class.java, java.lang.Boolean.TYPE, java.lang.Boolean::class.java -> bundle.putBoolean(key, value as Boolean)
                Int::class.java, Integer.TYPE, java.lang.Integer::class.java -> bundle.putInt(key, value as Int)
                Long::class.java, java.lang.Long.TYPE, java.lang.Long::class.java -> bundle.putLong(key, value as Long)
                Float::class.java, java.lang.Float.TYPE, java.lang.Float::class.java -> bundle.putFloat(key, value as Float)
                Double::class.java, java.lang.Double.TYPE, java.lang.Double::class.java -> bundle.putDouble(key, value as Double)
                Byte::class.java -> bundle.putByte(key, value as Byte)
                Short::class.java -> bundle.putShort(key, value as Short)
                Char::class.java -> bundle.putChar(key, value as Char)
                String::class.java -> bundle.putString(key, value as String)
                CharSequence::class.java -> bundle.putCharSequence(key, value as CharSequence)
                Serializable::class.java -> bundle.putSerializable(key, value as Serializable)
                Parcelable::class.java -> bundle.putParcelable(key, value as Parcelable)
                Bundle::class.java -> bundle.putBundle(key, value as Bundle)
                SparseArray::class.java -> bundle.putSparseParcelableArray(key, value as SparseArray<out Parcelable>)
                ViewModelStore::class.java -> bundle.putParcelable(key, SaveWrapper(value))
                else -> throw UnsupportedOperationException("Unable to save value of type $type")
            } as T?
        }
    }

    private fun <T> copyParcelableArray(
        array: Array<Parcelable?>?,
        targetClass: Class<out Array<T>?>
    ): Array<T>? = array?.let { Arrays.copyOf(it, it.size, targetClass) }

    /**
     * Checks if the type could be read and written to [Bundle]. Basically, most of the types that
     * [Bundle] can handle is supported.
     */
    private fun isSupported(desc: TypeDescription): Boolean = when {
        desc.isArrayList -> {
            desc.type == null ||
                desc.type == Int::class.java ||
                desc.type == String::class.java ||
                desc.type == CharSequence::class.java ||
                desc.type == Parcelable::class.java
        }
        desc.isArray -> {
            desc.type == null ||
                desc.type == Boolean::class.java ||
                desc.type == Int::class.java ||
                desc.type == Long::class.java ||
                desc.type == Float::class.java ||
                desc.type == Double::class.java ||
                desc.type == Byte::class.java ||
                desc.type == Short::class.java ||
                desc.type == Char::class.java ||
                desc.type == String::class.java ||
                desc.type == CharSequence::class.java ||
                desc.type == Serializable::class.java ||
                desc.type == Parcelable::class.java
        }
        else -> {
            desc.type == Boolean::class.java ||
                desc.type == java.lang.Boolean.TYPE ||
                desc.type == java.lang.Boolean::class.java ||
                desc.type == Int::class.java ||
                desc.type == Integer.TYPE ||
                desc.type == java.lang.Integer::class.java ||
                desc.type == Long::class.java ||
                desc.type == java.lang.Long.TYPE ||
                desc.type == java.lang.Long::class.java ||
                desc.type == Float::class.java ||
                desc.type == java.lang.Float.TYPE ||
                desc.type == java.lang.Float::class.java ||
                desc.type == Double::class.java ||
                desc.type == java.lang.Double.TYPE ||
                desc.type == java.lang.Double::class.java ||
                desc.type == Byte::class.java ||
                desc.type == Short::class.java ||
                desc.type == Char::class.java ||
                desc.type == String::class.java ||
                desc.type == CharSequence::class.java ||
                desc.type == Serializable::class.java ||
                desc.type == Parcelable::class.java ||
                desc.type == Bundle::class.java ||
                desc.type == SparseArray::class.java ||
                desc.type == ViewModelStore::class.java
        }
    }

    private fun <T> getTypeDescription(type: Class<*>, value: T?): TypeDescription {
        val isSparseArray = type == SparseArray::class.java
        val isArrayList = !isSparseArray && type == ArrayList::class.java
        val isArray = !isSparseArray && !isArrayList && type.isArray

        val innerType: Class<*>?
        val declaredType: Class<*>? = when {
            isArrayList -> {
                innerType = (value as ArrayList<*>?)?.firstOrNull()?.javaClass ?: Any::class.java

                when {
                    innerType == String::class.java -> String::class.java
                    innerType == Int::class.java -> Int::class.java
                    CharSequence::class.java.isAssignableFrom(innerType) -> CharSequence::class.java
                    Parcelable::class.java.isAssignableFrom(innerType) -> Parcelable::class.java
                    else -> null
                }
            }
            isArray -> {
                innerType = type.componentType
                    ?: ((value as Array<*>?)?.firstOrNull()?.javaClass ?: Any::class.java)

                when {
                    innerType == String::class.java -> String::class.java
                    CharSequence::class.java.isAssignableFrom(innerType) -> CharSequence::class.java
                    Parcelable::class.java.isAssignableFrom(innerType) -> Parcelable::class.java
                    Serializable::class.java.isAssignableFrom(innerType) -> Serializable::class.java
                    else -> null
                }
            }
            else -> {
                innerType = null

                when {
                    type == String::class.java -> String::class.java
                    CharSequence::class.java.isAssignableFrom(type) -> CharSequence::class.java
                    Parcelable::class.java.isAssignableFrom(type) -> Parcelable::class.java
                    Serializable::class.java.isAssignableFrom(type) -> Serializable::class.java
                    else -> type
                }
            }
        }

        return TypeDescription(
            isArrayList,
            isArray,
            declaredType == Serializable::class.java ||
                declaredType == Parcelable::class.java ||
                declaredType == Bundle::class.java,
            declaredType,
            innerType
        )
    }

    private data class TypeDescription(
        val isArrayList: Boolean,
        val isArray: Boolean,
        val customType: Boolean,
        val type: Class<*>?,
        val innerType: Class<*>?
    )
}