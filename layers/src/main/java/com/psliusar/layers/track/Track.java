package com.psliusar.layers.track;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class Track<V, P> {

    protected OnTrackListener<V, P> listener;

    /**
     * Value that was achieved during work
     */
    private V value;

    /**
     * When true, track will not be automatically saved. This flag means client doesn't need this track any more.
     */
    private boolean disposed = false;

    /**
     * Indicates that work reached its end
     */
    private boolean finished;

    /**
     * Indicates that work is started
     */
    private boolean started;

    public Track() {

    }

    public void subscribe(@Nullable OnTrackListener<V, P> listener) {
        this.listener = listener;
    }

    public void unsubscribe() {
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
        value = null;
        callOnRestart();
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
        unsubscribe();
    }

    public boolean isDisposed() {
        return disposed;
    }

    protected void postProgress(@Nullable P progress) {
        callOnProgress(progress);
    }

    protected void done(@Nullable V result) {
        value = result;
        finished = true;
        started = false;
        callOnFinished();
    }

    protected void callOnFinished() {
        if (listener != null) {
            listener.onTrackFinished(this, value);
        }
    }

    protected void callOnError(@NonNull Throwable t) {
        if (listener != null) {
            listener.onTrackError(this, t);
        }
    }

    protected void callOnRestart() {
        if (listener != null) {
            listener.onTrackRestart(this);
        }
    }

    protected void callOnProgress(@Nullable P progress) {
        if (listener != null) {
            listener.onTrackProgress(this, progress);
        }
    }

    @MainThread
    protected abstract void doBlocking();

    public interface OnTrackListener<V, P> {

        void onTrackFinished(@NonNull Track<V, P> track, @Nullable V value);

        void onTrackError(@NonNull Track<V, P> track, @NonNull Throwable throwable);

        void onTrackRestart(@NonNull Track<V, P> track);

        void onTrackProgress(@NonNull Track<V, P> track, @Nullable P progress);
    }
}
