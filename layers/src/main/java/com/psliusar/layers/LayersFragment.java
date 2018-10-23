package com.psliusar.layers;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class LayersFragment extends Fragment {

    private Layers layers;

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        final Activity activity = getActivity();
        /*final LayersHost layersHost = new LayersHost() {
            @NonNull
            @Override
            public Layers getLayers() {
                return layers;
            }

            @NonNull
            @Override
            public <T extends View> T getView(@IdRes int viewId) {
                return (T) LayersFragment.this.getView().findViewById(viewId);
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
        layers = new Layers(layersHost, getDefaultContainerId(), state);*/
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        //layers.resumeView();
    }

    protected abstract int getDefaultContainerId();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //layers.saveState(outState);
    }
}
