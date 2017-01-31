package com.psliusar.layers.binder;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

public abstract class LayerBinder {

    public static final String BINDER_SUFFIX = "$$LayerBinder";

    @NonNull
    protected static View find(@NonNull View container, @IdRes int viewResId) {
        final View view = container.findViewById(viewResId);
        if (view == null) {
            final String viewResName = resolveResourceName(container.getResources(), viewResId);
            throw new IllegalArgumentException("View with ID 0x" + Integer.toHexString(viewResId)
                    + (viewResName != null ? " (" + viewResName + ")" : "") + " not found!");
        }
        return view;
    }

    @Nullable
    private static String resolveResourceName(@NonNull Resources res, int resId) {
        try {
            return res.getResourceName(resId);
        } catch (Resources.NotFoundException ex) {
            return null;
        }
    }

    protected void restore(@NonNull Object target, @NonNull Bundle state) {

    }

    protected void save(@NonNull Object target, @NonNull Bundle state) {

    }

    protected void bind(@NonNull View.OnClickListener listener, @NonNull View view) {

    }

    protected void unbind(@NonNull View.OnClickListener listener) {

    }
}
