package com.psliusar.layers

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray

private const val VIEW_STATE = "STACK_ENTRY.VIEW_STATE"

/**
 * An entry that contains metadata for a [Layer].
 */
internal class StackEntry private constructor(
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

    /** The class of the [Layer] */
    private var layerClass: Class<out Layer>? = null

    /** Optional name of the entry in the stack */
    internal var name: String? = null

    /** Optional arguments for the [Layer] */
    internal var arguments: Bundle? = null

    /** Retained state of the [Layer] */
    internal var layerState: Bundle? = null

    /** Retained state of the [android.view.View] of the [Layer] */
    internal var viewState: SparseArray<Parcelable>? = null

    /** Transparency type of the layer. One of [TYPE_TRANSPARENT], [TYPE_OPAQUE]. */
    internal var layerType = TYPE_OPAQUE

    /** Optional layout resource for the [Layer] */
    internal var layoutResId: Int = 0

    /** Optional animations for transitions */
    internal var animations: IntArray? = null

    //endregion

    /** The instance of the [Layer] */
    internal var layerInstance: Layer? = null

    /** The current state of the [Layer] */
    internal var state = LAYER_STATE_EMPTY

    /**
     * Indicates whether the entry is still valid.
     *
     * If the [Layer] is being removed from the stack and is currently animating, the entry can be
     * considered as invalid, which will prevent it from saving to Activity's state.
     */
    internal var valid = true

    /** Marks the entry as in transition state */
    internal var inTransition = false

    /**
     * Returns an instance of the [Layer]. Will create an instance at first call.
     */
    fun instantiateLayer(): Layer = layerInstance.or {
        try {
            val instance = getLayerClass().newInstance()
            layerInstance = instance
            instance
        } catch (e: InstantiationException) {
            throw RuntimeException(
                "Unable to instantiate layer $className: make sure class exists, is public " +
                    "and has an empty constructor", e
            )
        } catch (e: IllegalAccessException) {
            throw RuntimeException(
                "Unable to instantiate layer $className: make sure class has an empty " +
                    "constructor that is public", e
            )
        }
    }

    private fun getLayerClass(): Class<out Layer> = layerClass.or {
        try {
            @Suppress("UNCHECKED_CAST")
            val lc = Layers::class.java.classLoader!!.loadClass(className) as Class<Layer>
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

        @Suppress("unused")
        @JvmField
        val CREATOR = object : Parcelable.Creator<StackEntry> {
            override fun createFromParcel(source: Parcel): StackEntry =
                StackEntry(source, Layers::class.java.classLoader!!)

            override fun newArray(size: Int): Array<StackEntry?> = arrayOfNulls(size)
        }
    }
}