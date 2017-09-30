package com.psliusar.layers.track;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface OnTrackListener<V, P> {

    void onTrackFinished(@NonNull Track<V, P> track, @Nullable V value);

    void onTrackError(@NonNull Track<V, P> track, @NonNull Throwable throwable);

    void onTrackRestart(@NonNull Track<V, P> track);

    void onTrackProgress(@NonNull Track<V, P> track, @Nullable P progress);
}
