package com.psliusar.layers;

import com.psliusar.layers.subscription.Subscription;
import com.psliusar.layers.subscription.Subscriptions;
import com.psliusar.layers.track.TrackManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ViewModel<M extends Model> {

    @Nullable
    TrackManager trackManager;

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

    @NonNull
    public TrackManager getTrackManager() {
        if (trackManager == null) {
            trackManager = new TrackManager();
        }
        return trackManager;
    }
}
