package com.psliusar.layers.binder.processor.save;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.javapoet.FieldSpec;

public class SaveField {

    private final String fieldName;
    private final String fieldType;

    private final String manager;
    private final String key;
    private final String methodSuffix;
    private final boolean needsClassLoader;

    private FieldSpec managerField;

    public SaveField(
            @NonNull String fieldName,
            @NonNull String fieldType,
            @Nullable String manager,
            @NonNull String key,
            @NonNull String methodSuffix,
            boolean needsClassLoader) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.manager = manager;
        this.key = key;
        this.methodSuffix = methodSuffix;
        this.needsClassLoader = needsClassLoader;
    }

    @NonNull
    public String getFieldName() {
        return fieldName;
    }

    @NonNull
    public String getFieldType() {
        return fieldType;
    }

    @Nullable
    public String getManager() {
        return manager;
    }

    @NonNull
    public String getKey() {
        return key;
    }

    @NonNull
    public String getMethodSuffix() {
        return methodSuffix;
    }

    public boolean needsClassLoader() {
        return needsClassLoader;
    }

    public void setManagerField(@Nullable FieldSpec fieldSpec) {
        managerField = fieldSpec;
    }

    @Nullable
    public FieldSpec getManagerField() {
        return managerField;
    }
}
