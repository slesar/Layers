package com.psliusar.layers.track;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class WorkerTask<V, P> extends AsyncTask<AsyncTrack<V, P>, P, V> {

    @Nullable
    private OnCompletionListener<V, P> listener;

    private volatile Throwable throwable;


    @Override
    protected V doInBackground(AsyncTrack<V, P>... params) {
        throwable = null;
        try {
            return params[0].doInBackground();
        } catch (Throwable t) {
            throwable = t;
            return null;
        }
    }

    protected void postProgress(@Nullable P value) {
        publishProgress(value);
    }

    @Override
    protected void onPostExecute(V value) {
        if (listener != null) {
            if (throwable == null) {
                listener.onWorkCompleted(value);
            } else {
                listener.onError(throwable);
            }
        }
    }

    @Override
    protected void onProgressUpdate(P... values) {
        if (listener != null) {
            listener.onProgressUpdate(values[0]);
        }
    }

    protected void setListener(@Nullable OnCompletionListener<V, P> listener) {
        this.listener = listener;
    }

    interface OnCompletionListener<V, P> {

        void onWorkCompleted(@Nullable V value);

        void onProgressUpdate(@Nullable P progress);

        void onError(@NonNull Throwable t);
    }
}
