package com.psliusar.layers.binder.processor;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.binder.LayerBinder;
import com.psliusar.layers.binder.processor.state.SaveField;
import com.psliusar.layers.binder.processor.state.SaveFieldProcessor;
import com.psliusar.layers.binder.processor.view.ViewField;
import com.psliusar.layers.binder.processor.view.ViewFieldProcessor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

public class BinderClassHolder {

    private final String packageName;
    private final String className;
    private final String parentClassName;

    private final List<SaveField> saveFields = new ArrayList<>();

    private final List<ViewField> viewFields = new ArrayList<>();

    private boolean fileWritten;

    public BinderClassHolder(
            @NonNull String packageName,
            @NonNull String className,
            @NonNull String parentClassName) {
        this.packageName = packageName;
        this.className = className;
        this.parentClassName = parentClassName;
    }

    public void setFileWritten() {
        fileWritten = true;
    }

    public boolean isFileWritten() {
        return fileWritten;
    }

    public void addSaveField(
            @NonNull String fieldName,
            @NonNull String fieldType,
            @Nullable String manager,
            @NonNull String key,
            @NonNull String methodSuffix) {
        final SaveField field = new SaveField(fieldName, fieldType, manager, key, methodSuffix);
        saveFields.add(field);
    }

    public void addViewField(
            @NonNull String fieldName,
            @NonNull String fieldType,
            int resId,
            @NonNull Integer parentResId,
            boolean clickListener) {
        final ViewField field = new ViewField(fieldName, fieldType, resId, parentResId, clickListener);
        viewFields.add(field);
    }

    @NonNull
    public String getJavaClassFile() {
        return JavaFile.builder(packageName, getTypeSpec()).build().toString();
    }

    @NonNull
    private TypeSpec getTypeSpec() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(className + LayerBinder.BINDER_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                // TODO Really need to @Keep here?
                .addAnnotation(ClassName.get(Keep.class));

        // TODO parametrized class
        builder.superclass(ClassName.bestGuess(parentClassName));

        if (!saveFields.isEmpty()) {
            builder.addMethod(SaveFieldProcessor.getRestoreMethod(packageName, className, saveFields));
            builder.addMethod(SaveFieldProcessor.getSaveMethod(packageName, className, saveFields));
        }

        if (!viewFields.isEmpty()) {
            builder.addMethod(ViewFieldProcessor.getBindMethod(packageName, className, viewFields));
            builder.addMethod(ViewFieldProcessor.getUnbindMethod(packageName, className, viewFields));
        }

        return builder.build();
    }
}
