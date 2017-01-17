package com.psliusar.layers;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedHashSet;
import java.util.LinkedList;

public class ActivityCallbacks implements OnActivityListener {

    private final LinkedHashSet<OnActivityListener> listeners = new LinkedHashSet<>();

    @Override
    public void onCreate(@Nullable Bundle state) {
        for (OnActivityListener listener : listeners) {
            listener.onCreate(state);
        }
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle state) {
        for (OnActivityListener listener : listeners) {
            listener.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onRestart() {
        for (OnActivityListener listener : listeners) {
            listener.onRestart();
        }
    }

    @Override
    public void onStart() {
        for (OnActivityListener listener : listeners) {
            listener.onStart();
        }
    }

    @Override
    public void onPostCreate(@Nullable Bundle state) {
        for (OnActivityListener listener : listeners) {
            listener.onPostCreate(state);
        }
    }

    @Override
    public void onResume() {
        for (OnActivityListener listener : listeners) {
            listener.onResume();
        }
    }

    @Override
    public void onPostResume() {
        for (OnActivityListener listener : listeners) {
            listener.onPostResume();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        for (OnActivityListener listener : listeners) {
            listener.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onPause() {
        for (OnActivityListener listener : listeners) {
            listener.onPause();
        }
    }

    @Override
    public void onStop() {
        for (OnActivityListener listener : listeners) {
            listener.onStop();
        }
    }

    @Override
    public void onDestroy() {
        for (OnActivityListener listener : listeners) {
            listener.onDestroy();
        }
    }

    @Override
    public void onNewIntent(@NonNull Intent intent) {
        for (OnActivityListener listener : listeners) {
            listener.onNewIntent(intent);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        for (OnActivityListener listener : listeners) {
            listener.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        for (OnActivityListener listener : listeners) {
            listener.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (OnActivityListener listener : listeners) {
            listener.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onTrimMemory(int level) {
        for (OnActivityListener listener : listeners) {
            listener.onTrimMemory(level);
        }
    }

    @Override
    public void onLowMemory() {
        for (OnActivityListener listener : listeners) {
            listener.onLowMemory();
        }
    }

    @NonNull
    public EventSubscription add(@NonNull OnActivityListener listener) {
        final boolean result = listeners.add(listener);
        return new EventSubscription(this, listener, result);
    }

    public boolean remove(@NonNull OnActivityListener listener) {
        return listeners.remove(listener);
    }

    public abstract static class BaseActivityListener implements OnActivityListener {

        @Override public void onCreate(@Nullable Bundle state) {}
        @Override public void onRestoreInstanceState(@NonNull Bundle state) {}
        @Override public void onRestart() {}
        @Override public void onStart() {}
        @Override public void onPostCreate(@Nullable Bundle state) {}
        @Override public void onResume() {}
        @Override public void onPostResume() {}
        @Override public void onSaveInstanceState(@NonNull Bundle outState) {}
        @Override public void onPause() {}
        @Override public void onStop() {}
        @Override public void onDestroy() {}

        @Override public void onNewIntent(@NonNull Intent intent) {}

        @Override public void onConfigurationChanged(Configuration newConfig) {}

        @Override public void onActivityResult(int requestCode, int resultCode, Intent intent) {}
        @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {}

        @Override public void onTrimMemory(int level) {}
        @Override public void onLowMemory() {}
    }

    public static class ManagedSubscriptions {

        private LinkedList<ActivityCallbacks.EventSubscription> subscriptions;

        public void manage(ActivityCallbacks.EventSubscription subscription) {
            if (subscriptions == null) {
                subscriptions = new LinkedList<>();
            }
            subscriptions.add(subscription);
        }

        public void unsubscribeAll() {
            if (subscriptions == null) {
                return;
            }
            for (ActivityCallbacks.EventSubscription subscription : subscriptions) {
                if (subscription.isSubscribed()) {
                    subscription.unsubscribe();
                }
            }
        }
    }

    public static class EventSubscription {

        private final ActivityCallbacks callbacks;
        private final OnActivityListener listener;
        private boolean subscribed;

        public EventSubscription(ActivityCallbacks callbacks, OnActivityListener listener, boolean subscribed) {
            this.callbacks = callbacks;
            this.listener = listener;
            this.subscribed = subscribed;
        }

        public void unsubscribe() {
            callbacks.remove(listener);
            subscribed = false;
        }

        public boolean isSubscribed() {
            return subscribed;
        }

        public void manageWith(ManagedSubscriptions manager) {
            manager.manage(this);
        }
    }
}
