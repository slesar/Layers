package com.psliusar.layers.track;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class TrackWrapper implements Parcelable {

    public static final Creator<TrackWrapper> CREATOR = new Creator<TrackWrapper>() {
        public TrackWrapper createFromParcel(Parcel in) {
            return new TrackWrapper(in);
        }

        public TrackWrapper[] newArray(int size) {
            return new TrackWrapper[size];
        }
    };

    private final Track track;

    TrackWrapper(@NonNull Parcel in) {
        // We're recreating from serialized object. Let track be empty, because we didn't save anything.
        track = null;
    }

    public TrackWrapper(@NonNull Track c) {
        track = c;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        // Don't write anything. We only need our object until Parcelable gets serialized.
    }

    @Nullable
    public Track getTrack() {
        return track;
    }
}
