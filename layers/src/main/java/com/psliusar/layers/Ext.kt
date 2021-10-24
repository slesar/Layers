package com.psliusar.layers

import android.content.res.Resources
import android.util.SparseArray
import androidx.annotation.AnyRes

/**
 * An extension that executes the callback if the original value is `null`.
 */
internal inline fun <T : Any> T?.or(creator: () -> T): T = this ?: creator()

/**
 * Retrieves a value by the given key from the [SparseArray]. If the value does not exist, the one
 * will be created by calling [creator]. The value will be stored by [key] and returned from the
 * method.
 */
internal inline fun <T : Any> SparseArray<T>.getOrPut(key: Int, creator: () -> T): T {
    return get(key) ?: creator().also { put(key, it) }
}

/**
 * For-each implementation for [SparseArray].
 */
internal inline fun <T> SparseArray<T>.forEach(action: (Int, T) -> Unit) {
    val size = size()
    for (i in 0 until size) {
        val key = keyAt(i)
        action(key, get(key))
    }
}

/**
 * Attempts to look up the resource name by the resource ID.
 */
internal fun getResourceName(res: Resources, @AnyRes resId: Int): String? = try {
    res.getResourceName(resId)
} catch (e: Resources.NotFoundException) {
    "0x" + Integer.toHexString(resId)
}