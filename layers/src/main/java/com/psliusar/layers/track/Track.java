package com.psliusar.layers.track;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class Track<V, P> {

    /**
     * Listener to report events to.
     */
    protected TrackCallbacks<V, P> listener;

    /**
     * An ID that is used in {@link TrackManager}.
     */
    private int id;

    /**
     * Value that was achieved during work.
     */
    private V value;

    /**
     * When true, track will not be automatically saved. This flag means client doesn't need this track any more.
     */
    private boolean disposed = false;

    /**
     * Indicates that work reached its end.
     */
    private boolean finished;

    /**
     * Indicates that work is started.
     */
    private boolean started;

    void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void subscribe(@Nullable TrackCallbacks<V, P> listener) {
        this.listener = listener;
        if (finished) {
            done(value);
        }
    }

    public void unSubscribe() {
        listener = null;
    }

    public void start() {
        if (finished) {
            done(value);
        } else if (!started) {
            restart();
        }
    }

    public void restart() {
        if (finished) {
            callOnRestart();
        }
        value = null;
        disposed = false;
        finished = false;
        started = true;
        try {
            doBlocking();
        } catch (Throwable t) {
            value = null;
            disposed = false;
            finished = false;
            started = false;
            callOnError(t);
        }
    }

    protected void done(@Nullable V result) {
        value = result;
        finished = true;
        started = false;
        callOnFinished();
    }

    public void cancel() {
        disposed = true;
        started = false;
    }

    public void stop() {
        cancel();
        callOnFinished();
    }

    /**
     * Calling this method means you've got what you wanted. This method will mark object as "disposed" and will prevent saving its state.
     */
    public void dispose() {
        value = null;
        disposed = true;
        cancel();
        unSubscribe();
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isDisposed() {
        return disposed;
    }

    @Nullable
    public V getValue() {
        return value;
    }

    protected void postProgress(@Nullable P progress) {
        callOnProgress(progress);
    }

    protected void callOnFinished() {
        if (listener != null) {
            listener.onTrackFinished(id, this, value);
        }
    }

    protected void callOnError(@NonNull Throwable t) {
        if (listener != null) {
            listener.onTrackError(id, this, t);
        }
    }

    protected void callOnRestart() {
        if (listener != null) {
            listener.onTrackRestart(id, this);
        }
    }

    protected void callOnProgress(@Nullable P progress) {
        if (listener != null) {
            listener.onTrackProgress(id, this, progress);
        }
    }

    @MainThread
    protected abstract void doBlocking();
}
