package com.psliusar.layers.track;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

public abstract class AsyncTrack<V, P> extends Track<V, P> {

    @Nullable
    private WorkerTask<V, P> task;

    private final WorkerTask.OnCompletionListener<V, P> taskListener = new WorkerTask.OnCompletionListener<V, P>() {
        @Override
        public void onWorkCompleted(@Nullable V value) {
            done(value);
        }

        @Override
        public void onProgressUpdate(@Nullable P progress) {
            callOnProgress(progress);
        }

        @Override
        public void onError(@NonNull Throwable t) {
            callOnError(t);
        }
    };

    @Override
    public void restart() {
        reset();
        super.restart();
    }

    @Override
    public void cancel() {
        reset();
        super.cancel();
    }

    @Override
    protected void doBlocking() {
        super.doBlocking();
        if (task != null && task.isCancelled()) {
            task.setListener(null);
            task = null;
        }
        if (task == null) {
            task = createWorkerTask();
        }
        task.setListener(taskListener);
        task.execute(this);
    }

    @WorkerThread
    protected abstract V doInBackground() throws Throwable;

    protected WorkerTask<V, P> createWorkerTask() {
        return new AsyncTaskWorkerTask<>();
    }

    protected void reset() {
        if (task != null) {
            task.setListener(null);
            task.cancel();
            task = null;
        }
    }
}
