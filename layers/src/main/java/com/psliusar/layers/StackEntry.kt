package com.psliusar.layers

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray

private const val VIEW_STATE = "STACK_ENTRY.VIEW_STATE"

class StackEntry private constructor(
    private val className: String
) : Parcelable {

    constructor(layerClass: Class<out Layer>) : this(layerClass.name) {
        this.layerClass = layerClass
    }

    internal constructor(input: Parcel, classLoader: ClassLoader) : this(input.readString()!!) {
        name = input.readString()
        arguments = input.readBundle(classLoader)
        layerState = input.readBundle(classLoader)
        viewState = input.readBundle(classLoader)?.getSparseParcelableArray(VIEW_STATE)
        layerType = input.readInt()
        layoutResId = input.readInt()
        if (input.readInt() > 0) {
            val array = IntArray(4)
            input.readIntArray(array)
            animations = array
        }
    }

    //region Retained properties

    private var layerClass: Class<out Layer>? = null
    internal var name: String? = null
    internal var arguments: Bundle? = null
    internal var layerState: Bundle? = null
    internal var viewState: SparseArray<Parcelable>? = null
    internal var layerType = TYPE_OPAQUE
    internal var layoutResId: Int = 0
    internal var animations: IntArray? = null

    //endregion

    internal var layerInstance: Layer? = null
    internal var state = LAYER_STATE_EMPTY
    internal var valid = true
    internal var inTransition = false

    fun instantiateLayer(): Layer = layerInstance.or {
        try {
            val instance = getLayerClass().newInstance()
            layerInstance = instance
            instance
        } catch (e: InstantiationException) {
            throw RuntimeException("Unable to instantiate layer $className: make sure class exists, is public, and has an empty constructor", e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Unable to instantiate layer $className: make sure class has an empty constructor that is public", e)
        }
    }

    private fun getLayerClass(): Class<out Layer> = layerClass.or {
        try {
            val lc = Layers::class.java.classLoader!!.loadClass(className) as Class<out Layer>
            layerClass = lc
            lc
        } catch (e: ClassNotFoundException) {
            throw RuntimeException("Failed to load class $className")
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(className)
        dest.writeString(name)
        dest.writeBundle(arguments)
        dest.writeBundle(layerState)
        val viewBundle = Bundle()
        viewBundle.putSparseParcelableArray(VIEW_STATE, viewState)
        dest.writeBundle(viewBundle)
        dest.writeInt(layerType)
        dest.writeInt(layoutResId)
        if (animations == null) {
            dest.writeInt(0)
        } else {
            dest.writeInt(1)
            dest.writeIntArray(animations)
        }
    }

    companion object {
        internal const val LAYER_STATE_EMPTY = 0
        internal const val LAYER_STATE_CREATED = 1
        internal const val LAYER_STATE_VIEW_CREATED = 2
        internal const val LAYER_STATE_VIEW_DESTROYED = 3
        internal const val LAYER_STATE_DESTROYED = 4

        internal const val TYPE_TRANSPARENT = 0
        internal const val TYPE_OPAQUE = 1

        @JvmField
        val CREATOR: Parcelable.Creator<StackEntry> = object : Parcelable.Creator<StackEntry> {
            override fun createFromParcel(input: Parcel): StackEntry = StackEntry(input, Layers::class.java.classLoader!!)
            override fun newArray(size: Int): Array<StackEntry?> = arrayOfNulls(size)
        }
    }
}