package com.psliusar.layers.binder.processor;

import com.psliusar.layers.binder.processor.builder.ClassBuilder;
import com.psliusar.layers.binder.processor.save.SaveField;
import com.psliusar.layers.binder.processor.save.SaveStatementsBuilder;
import com.psliusar.layers.binder.processor.view.ViewField;
import com.psliusar.layers.binder.processor.view.ViewStatementsBuilder;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

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

    @NonNull
    public SaveField addSaveField(
            @NonNull String fieldName,
            @NonNull String fieldType) {
        final SaveField field = new SaveField(fieldName, fieldType);
        saveFields.add(field);
        return field;
    }

    @NonNull
    public ViewField addViewField(
            @NonNull String fieldName,
            @NonNull String fieldType,
            int resId,
            @NonNull Integer parentResId,
            boolean clickListener) {
        final ViewField field = new ViewField(fieldName, fieldType, resId, parentResId, clickListener);
        viewFields.add(field);
        return field;
    }

    @NonNull
    public String getJavaClassFile() {
        return JavaFile.builder(packageName, getTypeSpec()).build().toString();
    }

    @NonNull
    private TypeSpec getTypeSpec() {
        final ClassBuilder builder = new ClassBuilder(className, parentClassName);

        final String innerClassName = className.replace('$', '.');

        if (!saveFields.isEmpty()) {
            builder.addStatementsBuilder(new SaveStatementsBuilder(packageName, innerClassName, saveFields));
        }
        if (!viewFields.isEmpty()) {
            builder.addStatementsBuilder(new ViewStatementsBuilder(packageName, innerClassName, viewFields));
        }

        return builder.build();
    }
}
