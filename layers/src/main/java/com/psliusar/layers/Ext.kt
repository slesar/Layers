package com.psliusar.layers

import android.content.res.Resources
import android.util.SparseArray
import androidx.annotation.AnyRes

internal inline fun <T : Any> T?.or(creator: () -> T): T = this ?: creator()

internal inline fun <T> SparseArray<T>.forEach(action: (Int, T) -> Unit) {
    val size = size()
    for (i in 0 until size) {
        val key = keyAt(i)
        action(key, get(key))
    }
}

internal fun getResourceName(res: Resources, @AnyRes resId: Int): String? = try {
    res.getResourceName(resId)
} catch (e: Resources.NotFoundException) {
    "0x" + Integer.toHexString(resId)
}