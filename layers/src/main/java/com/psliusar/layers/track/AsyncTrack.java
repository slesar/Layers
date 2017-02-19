package com.psliusar.layers.track;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.os.AsyncTaskCompat;

public abstract class AsyncTrack<V, P> extends Track<V, P> implements WorkerTask.OnCompletionListener<V, P> {

    @Nullable
    private WorkerTask<V, P> task;

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
    public void stop() {
        reset();
        super.stop();
    }

    @Override
    protected void doBlocking() {
        if (task != null && task.isCancelled()) {
            task.setListener(null);
            task = null;
        }
        if (task == null) {
            task = new WorkerTask<>();
        }
        task.setListener(this);
        if (task.getStatus() == AsyncTask.Status.PENDING) {
            AsyncTaskCompat.executeParallel(task, this);
        }
    }

    @Override
    protected void postProgress(@Nullable P progress) {
        task.postProgress(progress);
    }

    @WorkerThread
    protected abstract V doInBackground();

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

    protected void reset() {
        if (task != null) {
            task.setListener(null);
            task.cancel(false);
            task = null;
        }
    }
}
