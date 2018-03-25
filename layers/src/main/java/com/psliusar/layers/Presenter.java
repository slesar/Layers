package com.psliusar.layers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class Presenter<M extends Model, L extends Layer> {

    private final LayersHost host;
    private final L layer;

    public Presenter(@NonNull L layer) {
        this.host = layer.getHost();
        this.layer = layer;
    }

    private M model;

    protected void onCreate() {

    }

    protected void onStart() {

    }

    protected void onStop() {

    }

    void destroy() {
        onDestroy();
        model = null;
    }

    protected void onDestroy() {

    }

    @NonNull
    public LayersHost getHost() {
        return host;
    }

    @NonNull
    public Activity getActivity() {
        return host.getActivity();
    }

    @NonNull
    public Context getContext() {
        return host.getActivity().getApplicationContext();
    }

    protected final L getLayer() {
        return layer;
    }

    public final M getModel() {
        if (model == null) {
            model = onCreateModel();
        }
        return model;
    }

    @Nullable
    protected M onCreateModel() {
        return null;
    }
}
