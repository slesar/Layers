package com.psliusar.layers;

import android.support.annotation.Nullable;

public abstract class Presenter<M extends Model> {

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
