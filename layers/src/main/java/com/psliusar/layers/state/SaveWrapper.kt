package com.psliusar.layers.state

import android.os.Parcel
import android.os.Parcelable

/**
 * A wrapper that behaves like real [Parcelable], but does not save the object to [Parcel].
 * This wrapper makes it possible to save custom object into Bundle for transferring between
 * components in the app. For example, when the Activity is being recreated because of configuration
 * change, a custom object could be delivered to the new instance in Bundle.
 *
 * This class is not supposed so serialize custom objects. Therefore, reading from parcel is not
 * implemented.
 */
internal class SaveWrapper(
    val value: Any?
) : Parcelable {

    // We're recreating from a serialized object. Let the object be empty, because we didn't save
    // anything.
    constructor(parcel: Parcel) : this(null)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // Don't write anything. We only need our object until Parcelable gets serialized.
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<SaveWrapper> {
            override fun createFromParcel(source: Parcel): SaveWrapper = SaveWrapper(source)
            override fun newArray(size: Int): Array<SaveWrapper?> = arrayOfNulls(size)
        }
    }
}