package com.psliusar.layers.track;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class SimpleTrackCallbacks<V, P> implements TrackCallbacks<V, P> {

    @Override
    public void onTrackStart(int trackId, @NonNull Track<V, P> track) {

    }

    @Override
    public void onTrackFinished(int trackId, @NonNull Track<V, P> track, @Nullable V value) {

    }

    @Override
    public void onTrackError(int trackId, @NonNull Track<V, P> track, @NonNull Throwable throwable) {

    }

    @Override
    public void onTrackRestart(int trackId, @NonNull Track<V, P> track) {

    }

    @Override
    public void onTrackProgress(int trackId, @NonNull Track<V, P> track, @Nullable P progress) {

    }
}
