package com.psliusar.layers.callbacks;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.subscription.Subscription;
import com.psliusar.layers.subscription.Subscriptions;

import java.util.ArrayList;

public class ActivityCallbacks implements OnActivityListener {

    private final Subscriptions subscriptions = new Subscriptions();

    @Override
    public void onCreate(@Nullable Bundle state) {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onCreate(state);
        }
        unSubscribe(ActivityEvent.CREATE);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle state) {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onRestoreInstanceState(state);
        }
        unSubscribe(ActivityEvent.RESTORE_INSTANCE_STATE);
    }

    @Override
    public void onRestart() {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onRestart();
        }
        unSubscribe(ActivityEvent.RESTART);
    }

    @Override
    public void onStart() {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onStart();
        }
        unSubscribe(ActivityEvent.START);
    }

    @Override
    public void onPostCreate(@Nullable Bundle state) {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onPostCreate(state);
        }
        unSubscribe(ActivityEvent.POST_CREATE);
    }

    @Override
    public void onResume() {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onResume();
        }
        unSubscribe(ActivityEvent.RESUME);
    }

    @Override
    public void onPostResume() {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onPostResume();
        }
        unSubscribe(ActivityEvent.POST_RESUME);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onSaveInstanceState(outState);
        }
        unSubscribe(ActivityEvent.SAVE_INSTANCE_STATE);
    }

    @Override
    public void onPause() {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onPause();
        }
        unSubscribe(ActivityEvent.PAUSE);
    }

    @Override
    public void onStop() {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onStop();
        }
        unSubscribe(ActivityEvent.STOP);
    }

    @Override
    public void onDestroy() {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onDestroy();
        }
        unSubscribe(ActivityEvent.DESTROY);
    }

    @Override
    public void onNewIntent(@NonNull Intent intent) {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onNewIntent(intent);
        }
        unSubscribe(ActivityEvent.NEW_INTENT);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onConfigurationChanged(newConfig);
        }
        unSubscribe(ActivityEvent.CONFIGURATION_CHANGED);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onActivityResult(requestCode, resultCode, intent);
        }
        unSubscribe(ActivityEvent.ACTIVITY_RESULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        unSubscribe(ActivityEvent.REQUEST_PERMISSIONS_RESULT);
    }

    @Override
    public void onTrimMemory(int level) {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onTrimMemory(level);
        }
        unSubscribe(ActivityEvent.TRIM_MEMORY);
    }

    @Override
    public void onLowMemory() {
        for (Subscription s : subscriptions) {
            unwrap(s).getListener().onLowMemory();
        }
        unSubscribe(ActivityEvent.LOW_MEMORY);
    }

    @NonNull
    public EventSubscription add(@NonNull OnActivityListener listener) {
        return add(listener, ActivityEvent.DESTROY);
    }

    @NonNull
    public EventSubscription add(@NonNull OnActivityListener listener, @NonNull ActivityEvent event) {
        final EventSubscription subscription = new EventSubscription(event, listener);
        subscriptions.manage(subscription);
        return subscription;
    }

    @NonNull
    private EventSubscription unwrap(@NonNull Subscription s) {
        return (EventSubscription) s.getOriginal();
    }

    private void unSubscribe(@NonNull ActivityEvent event) {
        ArrayList<Subscription> subjects = null;

        for (Subscription s : subscriptions) {
            if (unwrap(s).getEvent() == event) {
                if (subjects == null) {
                    subjects = new ArrayList<>(subscriptions.size());
                }
                subjects.add(s);
            }
        }

        if (subjects != null) {
            for (Subscription s : subjects) {
                s.unSubscribe();
            }
        }
    }
}
