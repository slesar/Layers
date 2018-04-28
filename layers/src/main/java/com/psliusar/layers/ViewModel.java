package com.psliusar.layers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.subscription.Subscription;
import com.psliusar.layers.subscription.Subscriptions;

public class ViewModel<M extends Model> {

    private Subscriptions subscriptions;

    private final M model;

    public ViewModel(@Nullable M model) {
        this.model = model;
    }

    protected void onUnSubscribe() {

    }

    protected void onDestroy() {
        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unSubscribeAll();
        }
    }

    public boolean isPersistent() {
        return true;
    }

    @Nullable
    public M getModel() {
        return model;
    }

    public void manage(@NonNull Subscription subscription) {
        if (subscriptions == null) {
            subscriptions = new Subscriptions();
        }
        subscriptions.manage(subscription);
    }
}
