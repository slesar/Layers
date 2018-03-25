package com.psliusar.layers.callbacks;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;

public class ActivityCallbacks implements OnActivityListener {

    private final LinkedList<EventSubscription> subscriptions = new LinkedList<>();

    @Override
    public void onCreate(@Nullable Bundle state) {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onCreate(state);
        }
        unSubscribe(ActivityEvent.CREATE);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle state) {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onRestoreInstanceState(state);
        }
        unSubscribe(ActivityEvent.RESTORE_INSTANCE_STATE);
    }

    @Override
    public void onRestart() {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onRestart();
        }
        unSubscribe(ActivityEvent.RESTART);
    }

    @Override
    public void onStart() {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onStart();
        }
        unSubscribe(ActivityEvent.START);
    }

    @Override
    public void onPostCreate(@Nullable Bundle state) {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onPostCreate(state);
        }
        unSubscribe(ActivityEvent.POST_CREATE);
    }

    @Override
    public void onResume() {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onResume();
        }
        unSubscribe(ActivityEvent.RESUME);
    }

    @Override
    public void onPostResume() {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onPostResume();
        }
        unSubscribe(ActivityEvent.POST_RESUME);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onSaveInstanceState(outState);
        }
        unSubscribe(ActivityEvent.SAVE_INSTANCE_STATE);
    }

    @Override
    public void onPause() {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onPause();
        }
        unSubscribe(ActivityEvent.PAUSE);
    }

    @Override
    public void onStop() {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onStop();
        }
        unSubscribe(ActivityEvent.STOP);
    }

    @Override
    public void onDestroy() {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onDestroy();
        }
        unSubscribe(ActivityEvent.DESTROY);
    }

    @Override
    public void onNewIntent(@NonNull Intent intent) {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onNewIntent(intent);
        }
        unSubscribe(ActivityEvent.NEW_INTENT);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onConfigurationChanged(newConfig);
        }
        unSubscribe(ActivityEvent.CONFIGURATION_CHANGED);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onActivityResult(requestCode, resultCode, intent);
        }
        unSubscribe(ActivityEvent.ACTIVITY_RESULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        unSubscribe(ActivityEvent.REQUEST_PERMISSIONS_RESULT);
    }

    @Override
    public void onTrimMemory(int level) {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onTrimMemory(level);
        }
        unSubscribe(ActivityEvent.TRIM_MEMORY);
    }

    @Override
    public void onLowMemory() {
        for (EventSubscription subscription : subscriptions) {
            subscription.listener.onLowMemory();
        }
        unSubscribe(ActivityEvent.LOW_MEMORY);
    }

    @NonNull
    public EventSubscription add(@NonNull OnActivityListener listener) {
        return add(listener, ActivityEvent.DESTROY);
    }

    @NonNull
    public EventSubscription add(@NonNull OnActivityListener listener, @NonNull ActivityEvent event) {
        final EventSubscription subscription = new EventSubscription(this, event, listener);
        subscriptions.add(subscription);
        return subscription;
    }

    public boolean remove(@NonNull OnActivityListener listener) {
        for (int i = subscriptions.size() - 1; i >= 0; i--) {
            if (subscriptions.get(i).listener == listener) {
                subscriptions.remove(i);
                return true;
            }
        }
        return false;
    }

    private void unSubscribe(@NonNull ActivityEvent event) {
        for (EventSubscription subscription : subscriptions) {
            if (subscription.event == event) {
                subscription.unSubscribe();
            }
        }
    }
}
