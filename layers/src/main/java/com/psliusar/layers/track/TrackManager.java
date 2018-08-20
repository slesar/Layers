package com.psliusar.layers.track;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

public class TrackManager implements Parcelable {

    private final SparseArrayCompat<Track<?, ?>> tracks = new SparseArrayCompat<>();
    private final SparseArrayCompat<TrackCallbacks<?, ?>> callbacks = new SparseArrayCompat<>();

    public TrackManager() {
        // Keep default public constructor
    }

    public void dropCallbacks() {
        callbacks.clear();
        for (int i = 0; i < tracks.size(); i++) {
            tracks.valueAt(i).unSubscribe();
        }
    }

    @NonNull
    public <V, P> TrackStarter registerTrackCallbacks(int trackId, @NonNull TrackCallbacks<V, P> callback) {
        callbacks.put(trackId, callback);
        final Track<V, P> track = (Track<V, P>) tracks.get(trackId);
        if (track != null) {
            track.subscribe(callback);
        }
        return new TrackStarter(this, trackId);
    }

    public void removeTrackCallbacks(int trackId) {
        callbacks.remove(trackId);
        final Track track = tracks.get(trackId);
        if (track != null) {
            track.unSubscribe();
        }
    }

    @NonNull
    public <V, P> Track<V, P> getTrack(int trackId) {
        Track<V, P> track = (Track<V, P>) tracks.get(trackId);
        if (track == null) {
            final TrackCallbacks<V, P> callback = (TrackCallbacks<V, P>) callbacks.get(trackId);
            if (callback == null) {
                throw new IllegalArgumentException("No track callbacks registered for ID: " + trackId);
            }
            track = callback.createTrack(trackId);
            track.setId(trackId);
            track.subscribe(callback);
            tracks.put(trackId, track);
        }
        return track;
    }

    public boolean hasTrack(int trackId) {
        return tracks.get(trackId) != null;
    }

    public static final Creator<TrackManager> CREATOR = new Creator<TrackManager>() {
        public TrackManager createFromParcel(Parcel in) {
            return new TrackManager(in);
        }

        public TrackManager[] newArray(int size) {
            return new TrackManager[size];
        }
    };

    TrackManager(@NonNull Parcel in) {
        // We're recreating from serialized object. Let everything be empty, because we didn't save anything.
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Don't write anything. We only need our objects until Parcelable gets serialized.
    }

    public static class TrackStarter {

        private final TrackManager trackManager;
        private final int trackId;

        public TrackStarter(@NonNull TrackManager trackManager, int trackId) {
            this.trackManager = trackManager;
            this.trackId = trackId;
        }

        public void start() {
            trackManager.getTrack(trackId).start();
        }

        public void restart() {
            trackManager.getTrack(trackId).restart();
        }
    }
}
