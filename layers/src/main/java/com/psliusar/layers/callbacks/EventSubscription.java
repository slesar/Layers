package com.psliusar.layers.callbacks;

import com.psliusar.layers.subscription.Subscription;

import androidx.annotation.NonNull;

public class EventSubscription extends Subscription {

    private final ActivityEvent event;
    private final OnActivityEventListener listener;

    public EventSubscription(
            @NonNull ActivityEvent event,
            @NonNull OnActivityEventListener listener) {
        this.event = event;
        this.listener = listener;
    }

    @NonNull
    public ActivityEvent getEvent() {
        return event;
    }

    @NonNull
    public OnActivityEventListener getListener() {
        return listener;
    }
}
