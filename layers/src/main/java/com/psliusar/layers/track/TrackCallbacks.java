package com.psliusar.layers.track;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface TrackCallbacks<V, P> {

    @NonNull
    Track<V, P> createTrack(int trackId);

    void onTrackFinished(int trackId, @NonNull Track<V, P> track, @Nullable V value);

    void onTrackError(int trackId, @NonNull Track<V, P> track, @NonNull Throwable throwable);

    void onTrackRestart(int trackId, @NonNull Track<V, P> track);

    void onTrackProgress(int trackId, @NonNull Track<V, P> track, @Nullable P progress);
}
