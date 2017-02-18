package com.psliusar.layers.track;

import android.support.annotation.WorkerThread;

public abstract class AsyncTrack<V> extends Track<V> {

    @Override
    protected void doBlocking() {

    }

    @WorkerThread
    protected abstract void doInBackground();
}
