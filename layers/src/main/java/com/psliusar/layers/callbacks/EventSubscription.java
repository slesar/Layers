package com.psliusar.layers.callbacks;

import android.support.annotation.NonNull;

public class EventSubscription {

    private final ActivityCallbacks callbacks;
    final ActivityEvent event;
    final OnActivityListener listener;
    private boolean subscribed;

    public EventSubscription(
            @NonNull ActivityCallbacks callbacks,
            @NonNull ActivityEvent event,
            @NonNull OnActivityListener listener) {
        this.callbacks = callbacks;
        this.event = event;
        this.listener = listener;
        subscribed = true;
    }

    @NonNull
    public ActivityCallbacks getCallbacks() {
        return callbacks;
    }

    @NonNull
    public ActivityEvent getEvent() {
        return event;
    }

    @NonNull
    public OnActivityListener getListener() {
        return listener;
    }

    public void unSubscribe() {
        callbacks.remove(listener);
        subscribed = false;
    }

    public boolean isSubscribed() {
        return subscribed;
    }
}
