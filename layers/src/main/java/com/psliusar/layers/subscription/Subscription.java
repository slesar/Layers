package com.psliusar.layers.subscription;

import android.support.annotation.NonNull;

public class Subscription {

    private boolean subscribed;

    private Subscriptions subscriptions = null;

    void subscribe(@NonNull Subscriptions subscriptions) {
        this.subscriptions = subscriptions;
        subscribed = true;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void unSubscribe() {
        if (subscriptions != null) {
            subscriptions.remove(this);
        }
        subscribed = false;
    }

    @NonNull
    public Subscription getOriginal() {
        return this;
    }
}
