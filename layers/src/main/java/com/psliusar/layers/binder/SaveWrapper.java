package com.psliusar.layers.binder;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SaveWrapper implements Parcelable {

    public static final Creator<SaveWrapper> CREATOR = new Creator<SaveWrapper>() {
        public SaveWrapper createFromParcel(Parcel in) {
            return new SaveWrapper(in);
        }

        public SaveWrapper[] newArray(int size) {
            return new SaveWrapper[size];
        }
    };

    private final Object object;

    SaveWrapper(@NonNull Parcel in) {
        // We're recreating from serialized object. Let object be empty, because we didn't save anything.
        object = null;
    }

    public SaveWrapper(@NonNull Object obj) {
        object = obj;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        // Don't write anything. We only need our object until Parcelable gets serialized.
    }

    @Nullable
    public Object getObject() {
        return object;
    }
}
