package com.psliusar.layers.binder;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

public abstract class ViewBindManager {

    @NonNull
    public View find(@NonNull View.OnClickListener listener, @NonNull View container, @IdRes int viewResId) {
        return ObjectBinder.find(container, viewResId);
    }
}
