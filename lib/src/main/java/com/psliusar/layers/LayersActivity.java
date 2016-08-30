package com.psliusar.layers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

public abstract class LayersActivity extends AppCompatActivity implements LayersHost {

    private Layers layers;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        layers = new Layers(this, state);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (layers != null) {
            layers.saveState(isFinishing() ? null : outState);
        }
    }

    @NonNull
    @Override
    public Layers getLayers() {
        return layers;
    }

    @NonNull
    @Override
    public <T extends View> T getView(@IdRes int viewId) {
        final View view = findViewById(viewId);
        if (view == null) {
            throw new IllegalArgumentException("View not found");
        }
        //noinspection unchecked
        return (T) view;
    }

    @NonNull
    @Override
    public ViewGroup getDefaultContainer() {
        return getView(android.R.id.content);
    }

    @NonNull
    @Override
    public Activity getActivity() {
        return this;
    }

    @Nullable
    @Override
    public Layer<?> getParentLayer() {
        return null;
    }

    @Override
    public void onBackPressed() {
        if (layers != null && layers.getStackSize() > 1) {
            final Layer<?> topLayer = layers.peek();
            if (topLayer != null && !topLayer.onBackPressed()) {
                layers.pop();
                return;
            }
        }
        super.onBackPressed();
    }
}
