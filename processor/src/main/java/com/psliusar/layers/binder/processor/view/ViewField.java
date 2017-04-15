package com.psliusar.layers.binder.processor.view;

import android.support.annotation.NonNull;

import com.psliusar.layers.binder.processor.FieldHolder;

public class ViewField extends FieldHolder {

    private final int resId;
    private final Integer parentContainer;
    private final boolean clickListener;

    public ViewField(
            @NonNull String fieldName,
            @NonNull String fieldType,
            int resId,
            @NonNull Integer parentContainer,
            boolean clickListener) {
        super(fieldName, fieldType);
        this.resId = resId;
        this.parentContainer = parentContainer;
        this.clickListener = clickListener;
    }

    public int getResId() {
        return resId;
    }

    @NonNull
    public Integer getParentContainer() {
        return parentContainer;
    }

    public boolean isClickListener() {
        return clickListener;
    }
}
