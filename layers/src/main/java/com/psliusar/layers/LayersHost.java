package com.psliusar.layers;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

public interface LayersHost {

    @NonNull
    Layers getLayers();

    @NonNull
    <T extends View> T getView(@IdRes int viewId);

    @NonNull
    ViewGroup getDefaultContainer();

    @NonNull
    Activity getActivity();

    @Nullable
    Layer<?> getParentLayer();
}
