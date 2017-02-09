package com.psliusar.layers.binder.processor.save;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.javapoet.FieldSpec;

public class SaveField {

    private final String fieldName;
    private final String fieldType;

    private String manager;
    private String key;
    private String methodSuffix;
    private boolean needsClassLoader;
    private boolean needsParcelableWrapper;
    private boolean needsSerializableWrapper;

    private FieldSpec managerField;

    public SaveField(
            @NonNull String fieldName,
            @NonNull String fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
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

    public void setManager(@Nullable String managerClass) {
        manager = managerClass;
    }

    @NonNull
    public String getKey() {
        return key;
    }

    public void setKey(@NonNull String key) {
        this.key = key;
    }

    @NonNull
    public String getMethodSuffix() {
        return methodSuffix;
    }

    public void setMethodSuffix(@NonNull String suffix) {
        methodSuffix = suffix;
    }

    public boolean needsClassLoader() {
        return needsClassLoader;
    }

    public void setNeedsClassLoader(boolean value) {
        needsClassLoader = value;
    }

    public boolean needsParcelableWrapper() {
        return needsParcelableWrapper;
    }

    public void setNeedsParcelableWrapper(boolean value) {
        needsParcelableWrapper = value;
    }

    public boolean needsSerializableWrapper() {
        return needsSerializableWrapper;
    }

    public void setNeedsSerializableWrapper(boolean value) {
        needsSerializableWrapper = value;
    }

    @Nullable
    public FieldSpec getManagerField() {
        return managerField;
    }

    public void setManagerField(@Nullable FieldSpec fieldSpec) {
        managerField = fieldSpec;
    }
}
