package com.psliusar.layers.binder;

import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

public abstract class ViewBinder {

    public static final String BINDER_SUFFIX = "$$ViewBinder";

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

    protected void bind(@NonNull View.OnClickListener listener, @NonNull View view) {

    }

    protected void unbind(@NonNull View.OnClickListener listener) {

    }
}
