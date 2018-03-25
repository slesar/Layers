package com.psliusar.layers.track;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface WorkerTask<V, P> {

    interface OnCompletionListener<V, P> {

        void onWorkCompleted(@Nullable V value);

        void onProgressUpdate(@Nullable P progress);

        void onError(@NonNull Throwable t);
    }

    void execute(@NonNull AsyncTrack<V, P> parent);

    void cancel();

    boolean isCancelled();

    void setListener(@Nullable OnCompletionListener<V, P> listener);
}
