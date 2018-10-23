package com.psliusar.layers;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
