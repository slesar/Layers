package com.psliusar.layers.binder

import android.os.Parcel
import android.os.Parcelable

class SaveWrapper(
    val value: Any?
) : Parcelable {

    // We're recreating from a serialized object. Let object be empty, because we didn't save anything.
    constructor(parcel: Parcel) : this(null)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        // Don't write anything. We only need our object until Parcelable gets serialized.
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<SaveWrapper> {
            override fun createFromParcel(`in`: Parcel): SaveWrapper = SaveWrapper(`in`)
            override fun newArray(size: Int): Array<SaveWrapper?> = arrayOfNulls(size)
        }
    }
}