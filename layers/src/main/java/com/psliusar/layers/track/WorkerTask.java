package com.psliusar.layers.track;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

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

    @WorkerThread
    void postProgress(@Nullable P progress);
}
