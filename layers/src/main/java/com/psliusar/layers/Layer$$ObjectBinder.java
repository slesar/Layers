package com.psliusar.layers;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.psliusar.layers.binder.ObjectBinder;

/**
 * Base binder for {@link Layer} that manages {@link com.psliusar.layers.track.TrackManager} lifecycle.
 */
public class Layer$$ObjectBinder extends ObjectBinder {

    private static final String TRACK_MANAGER_KEY = "_LAYER$$TRACK_MANAGER";

    @Override
    protected void save(@NonNull Object object, @NonNull Bundle state) {
        super.save(object, state);
        final Layer target = (Layer) object;
        putTrackManager(TRACK_MANAGER_KEY, target.trackManager, state);
    }

    @Override
    protected void restore(@NonNull Object object, @NonNull Bundle state) {
        super.restore(object, state);
        final Layer target = (Layer) object;
        target.trackManager = getTrackManager(TRACK_MANAGER_KEY, state);
    }

    @Override
    protected void unbindTrackManagers(@NonNull Object object) {
        super.unbindTrackManagers(object);
        final Layer target = (Layer) object;
        if (target.trackManager != null) target.trackManager.dropCallbacks();
    }
}
