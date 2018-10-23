package com.psliusar.layers;

import android.os.Bundle;

import com.psliusar.layers.binder.ObjectBinder;
import com.psliusar.layers.track.TrackManager;

import androidx.annotation.NonNull;

/**
 * Base binder for {@link Layer} that manages lifecycle for
 * {@link com.psliusar.layers.track.TrackManager} and {@link ViewModel}.
 */
public class Layer$$ObjectBinder extends ObjectBinder {

    private static final String TRACK_MANAGER_KEY = "_LAYER$$TRACK_MANAGER";
    private static final String VIEW_MODEL_KEY = "_LAYER$$VIEW_MODEL";

    @Override
    protected void save(@NonNull Object object, @NonNull Bundle state) {
        super.save(object, state);
        final Layer target = (Layer) object;
        final ViewModel vm = target.viewModel;
        if (vm != null) {
            if (vm.isPersistent()) {
                putViewModel(VIEW_MODEL_KEY, vm, state);
            } else {
                putTrackManager(TRACK_MANAGER_KEY, vm.trackManager, state);
            }
        }
    }

    @Override
    protected void restore(@NonNull Object object, @NonNull Bundle state) {
        super.restore(object, state);
        final Layer target = (Layer) object;
        target.viewModel = getViewModel(VIEW_MODEL_KEY, state);
        final TrackManager manager = getTrackManager(TRACK_MANAGER_KEY, state);
        if (manager != null) {
            // Create view model if it doesn't exist
            final ViewModel vm = target.getViewModel();
            if (vm.trackManager == null) {
                vm.trackManager = manager;
            }
        }
    }

    @Override
    protected void unbindTrackManagers(@NonNull Object object) {
        super.unbindTrackManagers(object);
        final Layer target = (Layer) object;
        final ViewModel vm = target.viewModel;
        if (vm != null && vm.trackManager != null) {
            vm.trackManager.dropCallbacks();
        }
    }
}
