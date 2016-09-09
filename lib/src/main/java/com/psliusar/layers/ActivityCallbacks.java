package com.psliusar.layers;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedHashSet;
import java.util.LinkedList;

public class ActivityCallbacks {

    public static final int EVENT_ON_CREATE = 1;
    public static final int EVENT_ON_RESTORE_STATE = 1 << 1;
    public static final int EVENT_ON_RESTART = 1 << 2;
    public static final int EVENT_ON_START = 1 << 3;
    public static final int EVENT_ON_POST_CREATE = 1 << 4;
    public static final int EVENT_ON_RESUME = 1 << 5;
    public static final int EVENT_ON_POST_RESUME = 1 << 6;
    public static final int EVENT_ON_SAVE_STATE = 1 << 7;
    public static final int EVENT_ON_PAUSE = 1 << 8;
    public static final int EVENT_ON_STOP = 1 << 9;
    public static final int EVENT_ON_DESTROY = 1 << 10;
    public static final int EVENT_ON_NEW_INTENT = 1 << 11;
    public static final int EVENT_ON_CONFIGURATION_CHANGED = 1 << 12;
    public static final int EVENT_ON_ACTIVITY_RESULT = 1 << 13;
    public static final int EVENT_ON_REQUEST_PERMISSIONS_RESULT = 1 << 14;
    public static final int EVENT_ON_TRIM_MEMORY = 1 << 15;
    public static final int EVENT_ON_LOW_MEMORY = 1 << 16;

    private final LinkedHashSet<OnActivityListener> listeners = new LinkedHashSet<>();

    public void fireEvent(int eventType, Object... args) {
        for (OnActivityListener listener : listeners) {
            if ((listener.getEventTypes() & eventType) == 0) {
                continue;
            }
            switch (eventType) {
            case EVENT_ON_CREATE: listener.onCreate((Bundle) args[0]); break;
            case EVENT_ON_RESTORE_STATE: listener.onRestoreInstanceState((Bundle) args[0]); break;
            case EVENT_ON_RESTART: listener.onRestart(); break;
            case EVENT_ON_START: listener.onStart(); break;
            case EVENT_ON_POST_CREATE: listener.onPostCreate((Bundle) args[0]); break;
            case EVENT_ON_RESUME: listener.onResume(); break;
            case EVENT_ON_POST_RESUME: listener.onPostResume(); break;
            case EVENT_ON_SAVE_STATE: listener.onSaveInstanceState((Bundle) args[0]); break;
            case EVENT_ON_PAUSE: listener.onPause(); break;
            case EVENT_ON_STOP: listener.onStop(); break;
            case EVENT_ON_DESTROY: listener.onDestroy(); break;
            case EVENT_ON_NEW_INTENT: listener.onNewIntent((Intent) args[0]); break;
            case EVENT_ON_CONFIGURATION_CHANGED: listener.onConfigurationChanged((Configuration) args[0]); break;
            case EVENT_ON_ACTIVITY_RESULT: listener.onActivityResult((Integer) args[0], (Integer) args[1], (Intent) args[2]); break;
            case EVENT_ON_REQUEST_PERMISSIONS_RESULT: listener.onRequestPermissionsResult((Integer) args[0], (String[]) args[1], (int[]) args[2]); break;
            case EVENT_ON_TRIM_MEMORY: listener.onTrimMemory((Integer) args[0]); break;
            case EVENT_ON_LOW_MEMORY: listener.onLowMemory(); break;
            }
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

    public interface OnActivityListener {

        void onCreate(@Nullable Bundle state);
        void onRestoreInstanceState(@NonNull Bundle state);
        void onRestart();
        void onStart();
        void onPostCreate(@Nullable Bundle state);
        void onResume();
        void onPostResume();
        void onSaveInstanceState(@NonNull Bundle outState);
        void onPause();
        void onStop();
        void onDestroy();

        void onNewIntent(@NonNull Intent intent);

        void onConfigurationChanged(Configuration newConfig);

        void onActivityResult(int requestCode, int resultCode, Intent intent);
        void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

        void onTrimMemory(int level);
        void onLowMemory();

        int getEventTypes();
    }

    public abstract static class BaseActivityListener implements OnActivityListener {

        private final int eventTypes;

        public BaseActivityListener(int eventTypes) {
            this.eventTypes = eventTypes;
        }

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

        @Override
        public int getEventTypes() {
            return eventTypes;
        }
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
            subscribed = !callbacks.remove(listener);
        }

        public boolean isSubscribed() {
            return subscribed;
        }

        public void manageWith(ManagedSubscriptions manager) {
            manager.manage(this);
        }
    }
}
