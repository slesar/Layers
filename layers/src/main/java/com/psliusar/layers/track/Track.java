package com.psliusar.layers.track;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class Track<V> {

    public static final int POLICY_KEEP = 1;
    public static final int POLICY_RESTART = 2;

    protected int policy = POLICY_KEEP;

    protected OnTrackListener<V> listener;

    private boolean disposed = false;

    private V value;

    public Track() {

    }

    public void setPolicy(int value) {
        policy = value;
    }

    public int getPolicy() {
        return policy;
    }

    public void subscribe(@Nullable OnTrackListener<V> listener) {
        this.listener = listener;
    }

    public void unsubscribe() {

    }

    public void start() {

    }

    public void restart() {

    }

    public void cancel() {

    }

    public void stop() {

    }

    /**
     * Calling this method means you've got what you wanted. This method will mark object as "disposed" and will prevent saving its state.
     */
    public void dispose() {
        disposed = true;
        unsubscribe();
    }

    public boolean isDisposed() {
        return disposed;
    }

    protected void callOnFinished() {
        if (listener != null) {
            listener.onTrackFinished(this, value);
        }
    }

    protected void callOnRestart() {
        if (listener != null) {
            listener.onTrackRestart(this);
        }
    }

    @MainThread
    protected abstract void doBlocking();

    public interface OnTrackListener<V> {

        void onTrackFinished(@NonNull Track<V> track, @Nullable V value);

        void onTrackRestart(@NonNull Track<V> track);
    }
}
