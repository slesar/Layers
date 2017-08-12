package com.psliusar.layers.binder;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;

public abstract class ViewBindManager {

    @NonNull
    public View find(@NonNull View.OnClickListener listener, @NonNull View container, @IdRes int viewResId) {
        return ObjectBinder.find(container, viewResId);
    }
}
