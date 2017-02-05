package com.psliusar.layers.binder.processor.save;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SaveField {

    private final String fieldName;
    private final String fieldType;

    private final String manager;
    private final String key;
    private final String methodSuffix;

    public SaveField(
            @NonNull String fieldName,
            @NonNull String fieldType,
            @Nullable String manager,
            @NonNull String key,
            @NonNull String methodSuffix) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.manager = manager;
        this.key = key;
        this.methodSuffix = methodSuffix;
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
}
