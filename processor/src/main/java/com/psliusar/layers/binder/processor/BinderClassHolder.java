package com.psliusar.layers.binder.processor;

import android.support.annotation.NonNull;

import com.psliusar.layers.binder.LayerBinder;
import com.psliusar.layers.binder.processor.view.ViewField;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

public class BinderClassHolder {


    private final String packageName;
    private final String className;
    private final String parentClassName;

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

    public void addViewField(@NonNull String fieldName, @NonNull String fieldType, int resId, @NonNull Integer parentResId, boolean clickListener) {
        final ViewField desc = new ViewField(fieldName, fieldType, resId, parentResId, clickListener);
        viewFields.add(desc);
    }

    @NonNull
    public String getJavaClassFile() {
        return JavaFile.builder(packageName, getTypeSpec()).build().toString();
    }

    @NonNull
    private TypeSpec getTypeSpec() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(className + LayerBinder.BINDER_SUFFIX)
                .addModifiers(Modifier.PUBLIC);

        // TODO parametrized class
        builder.superclass(ClassName.bestGuess(parentClassName));

        if (!viewFields.isEmpty()) {
            builder.addMethod(ViewFieldProcessor.getBindMethod(packageName, className, viewFields));
            builder.addMethod(ViewFieldProcessor.getUnbindMethod(packageName, className, viewFields));
        }

        return builder.build();
    }

}
