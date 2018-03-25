package com.psliusar.layers.track;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.AsyncTaskCompat;

import java.util.concurrent.atomic.AtomicReference;

public class AsyncTaskWorkerTask<V, P> implements WorkerTask<V, P> {

    private final Task<V, P> task = new Task<>();

    @Override
    public void execute(@NonNull AsyncTrack<V, P> parent) {
        if (task.getStatus() == AsyncTask.Status.PENDING) {
            AsyncTaskCompat.executeParallel(task, parent);
        } else {
            // XXX
        }
    }

    @Override
    public void cancel() {
        setListener(null);
        task.cancel(false);
    }

    @Override
    public boolean isCancelled() {
        return task.isCancelled();
    }

    @Override
    public void setListener(@Nullable OnCompletionListener<V, P> listener) {
        task.setListener(listener);
    }

    private static class Task<V, P> extends AsyncTask<AsyncTrack<V, P>, P, V> {

        private final AtomicReference<Throwable> throwableRef = new AtomicReference<>();

        @Nullable
        private OnCompletionListener<V, P> listener;

        @Override
        protected V doInBackground(AsyncTrack<V, P>... params) {
            throwableRef.set(null);
            try {
                return params[0].doInBackground();
            } catch (Throwable t) {
                throwableRef.set(t);
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(P... values) {
            if (listener != null) {
                listener.onProgressUpdate(values[0]);
            }
        }

        @Override
        protected void onPostExecute(V value) {
            if (listener != null) {
                final Throwable throwable = throwableRef.get();
                if (throwable == null) {
                    listener.onWorkCompleted(value);
                } else {
                    listener.onError(throwable);
                }
            }
        }

        protected void setListener(@Nullable OnCompletionListener<V, P> listener) {
            this.listener = listener;
        }
    }
}