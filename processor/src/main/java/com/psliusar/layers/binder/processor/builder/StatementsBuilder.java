package com.psliusar.layers.binder.processor.builder;

import com.psliusar.layers.binder.processor.FieldHolder;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class StatementsBuilder<F extends FieldHolder> {

    private final List<F> fields;
    private final String packageName;
    private final String className;

    public StatementsBuilder(@NonNull String packageName, @NonNull String className, @NonNull List<F> fields) {
        this.packageName = packageName;
        this.className = className;
        this.fields = fields;
    }

    @Nullable
    public abstract List<FieldSpec> createClassFields();

    @Nullable
    public abstract Iterable<MethodSpec> createMethods();

    @NonNull
    public String getPackageName() {
        return packageName;
    }

    @NonNull
    public String getClassName() {
        return className;
    }

    @NonNull
    protected List<F> getFields() {
        return fields;
    }

}
