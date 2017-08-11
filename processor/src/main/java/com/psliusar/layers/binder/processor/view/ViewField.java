package com.psliusar.layers.binder.processor.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.binder.processor.FieldHolder;
import com.squareup.javapoet.FieldSpec;

public class ViewField extends FieldHolder {

    private final int resId;
    private final Integer parentContainer;
    private final boolean clickListener;
    private String manager;

    private FieldSpec managerField;

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

    @Nullable
    public String getManager() {
        return manager;
    }

    public void setManager(@Nullable String managerClass) {
        manager = managerClass;
    }

    @Nullable
    public FieldSpec getManagerField() {
        return managerField;
    }

    public void setManagerField(@Nullable FieldSpec fieldSpec) {
        managerField = fieldSpec;
    }
}
