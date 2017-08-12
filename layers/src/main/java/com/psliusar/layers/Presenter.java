package com.psliusar.layers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class Presenter<M extends Model, L extends Layer> {

    private LayersHost host;
    private M model;
    private L layer;

    void create(@NonNull LayersHost host, @NonNull L layer) {
        this.host = host;
        this.layer = layer;
        onCreate();
    }

    protected void onCreate() {

    }

    void destroy() {
        onDestroy();
        model = null;
        layer = null;
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
