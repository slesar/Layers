package com.psliusar.layers.callbacks;

import android.support.annotation.NonNull;

import java.util.LinkedList;

public class ManagedSubscriptions {

    private LinkedList<EventSubscription> subscriptions;

    public void manage(@NonNull EventSubscription subscription) {
        if (subscriptions == null) {
            subscriptions = new LinkedList<>();
        }
        subscriptions.add(subscription);
    }

    public void unSubscribeAll() {
        if (subscriptions == null) {
            return;
        }
        for (EventSubscription subscription : subscriptions) {
            if (subscription.isSubscribed()) {
                subscription.unSubscribe();
            }
        }
    }

    public boolean hasSubscriptions() {
        if (subscriptions == null || subscriptions.size() == 0) {
            return false;
        }

        for (EventSubscription subscription : subscriptions) {
            if (subscription.isSubscribed()) {
                return true;
            }
        }

        return false;
    }
}
