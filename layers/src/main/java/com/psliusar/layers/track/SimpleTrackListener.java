package com.psliusar.layers.track;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SimpleTrackListener<V, P> implements OnTrackListener<V, P> {

    @Override
    public void onTrackFinished(@NonNull Track<V, P> track, @Nullable V value) {

    }

    @Override
    public void onTrackError(@NonNull Track<V, P> track, @NonNull Throwable throwable) {

    }

    @Override
    public void onTrackRestart(@NonNull Track<V, P> track) {

    }

    @Override
    public void onTrackProgress(@NonNull Track<V, P> track, @Nullable P progress) {

    }
}
