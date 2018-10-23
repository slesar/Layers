package com.psliusar.layers.binder.processor;

import androidx.annotation.NonNull;

public class FieldHolder {

    private final String fieldName;
    private final String fieldType;

    public FieldHolder(
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
}
