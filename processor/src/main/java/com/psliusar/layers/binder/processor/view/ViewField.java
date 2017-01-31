package com.psliusar.layers.binder.processor.view;

import android.support.annotation.NonNull;

public class ViewField {

    private final String fieldName;
    private final String fieldType;

    private final int resId;
    private final Integer parentContainer;
    private final boolean clickListener;

    public ViewField(@NonNull String fieldName, @NonNull String fieldType, int resId, @NonNull Integer parentContainer, boolean clickListener) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.resId = resId;
        this.parentContainer = parentContainer;
        this.clickListener = clickListener;
    }

    @NonNull
    public String getFieldName() {
        return fieldName;
    }

    @NonNull
    public String getFieldType() {
        return fieldType;
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
