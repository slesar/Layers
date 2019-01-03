package com.psliusar.layers;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class LayersFragment extends Fragment {

    private static final String SAVED_STATE_LAYERS = "LAYERS.SAVED_STATE_LAYERS";

    private Layers layers;
    private boolean layersStateRestored = false;

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        layersStateRestored = state != null;

        final Activity activity = requireActivity();

        final LayersHost host = new LayersHost() {
            @NonNull
            @Override
            public Layers getLayers() {
                return layers;
            }

            @NonNull
            @Override
            public <T extends View> T getView(int viewId) {
                return LayersFragment.this.getView().findViewById(viewId);
            }

            @NonNull
            @Override
            public ViewGroup getDefaultContainer() {
                return LayersFragment.this.getView().findViewById(getDefaultContainerId());
            }

            @NonNull
            @Override
            public Activity getActivity() {
                return activity;
            }

            @Nullable
            @Override
            public Layer<?> getParentLayer() {
                return null;
            }
        };

        layers = new Layers(host, state != null ? state.getBundle(SAVED_STATE_LAYERS) : null);
    }

    @Override
    public void onStart() {
        super.onStart();
        ensureLayerViews();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        final Bundle layersState = layers.saveState();
        if (layersState != null) {
            outState.putBundle(SAVED_STATE_LAYERS, layersState);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        layers.destroy();
    }

    public boolean onBackPressed() {
        if (layers.onBackPressed()) {
            return true;
        }
        if (layers.getStackSize() > 1) {
            layers.pop();
            return true;
        }
        return false;
    }

    @NonNull
    public Layers getLayers() {
        ensureLayerViews();
        return layers;
    }

    @IdRes
    protected abstract int getDefaultContainerId();

    private void ensureLayerViews() {
        if (layersStateRestored) {
            layers.resumeView();
            layersStateRestored = false;
        }
    }
}
