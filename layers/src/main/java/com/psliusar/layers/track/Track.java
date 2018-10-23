package com.psliusar.layers.track;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    /**
     * Makes track run only once. It will unsubscribe once work is done.
     */
    private boolean singleShot = false;

    void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setSingleShot(boolean value) {
        singleShot = value;
    }

    public boolean isSingleShot() {
        return singleShot;
    }

    public void subscribe(@Nullable TrackCallbacks<V, P> listener) {
        this.listener = listener;
        if (started && listener != null) {
            listener.onTrackStart(id, this);
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
            if (listener != null) {
                listener.onTrackStart(id, this);
            }
            doBlocking();
        } catch (Throwable t) {
            value = null;
            disposed = false;
            finished = false;
            started = false;
            callOnError(t);
        }
    }

    @CallSuper
    protected void done(@Nullable V result) {
        value = result;
        finished = true;
        started = false;
        callOnFinished();
        if (singleShot) {
            dispose();
        }
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

    public boolean isStarted() {
        return started;
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
        if (singleShot) {
            dispose();
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
