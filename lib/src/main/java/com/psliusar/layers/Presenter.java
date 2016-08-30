package com.psliusar.layers;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by slesar on 8/4/16.
 */
public abstract class Presenter<M extends Model, L extends Layer> {

    private LayersHost host;
    private M model;
    private L layer;

    void create(@NonNull LayersHost host, @NonNull L layer) {
        this.host = host;
        this.layer = layer;
    }

    void destroy() {
        model = null;
        layer = null;
    }

    public LayersHost getHost() {
        return host;
    }

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

    protected M onCreateModel() {
        return null;
    }
}
