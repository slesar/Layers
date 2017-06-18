package com.psliusar.layers.binder.processor;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.psliusar.layers.binder.BinderConstants;
import com.psliusar.layers.binder.processor.save.SaveField;
import com.psliusar.layers.binder.processor.save.SaveFieldProcessor;
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

    public SaveField addSaveField(
            @NonNull String fieldName,
            @NonNull String fieldType) {
        final SaveField field = new SaveField(fieldName, fieldType);
        saveFields.add(field);
        return field;
    }

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
        TypeSpec.Builder builder = TypeSpec.classBuilder(className + BinderConstants.BINDER_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(
                        "Generated class. Do not modify.\n" +
                                "Generator: $T.\n" +
                                "Details: $L\n",
                        LayersAnnotationProcessor.class,
                        "https://github.com/slesar/Layers")
                // TODO Really need to @Keep here?
                .addAnnotation(Keep.class);

        // TODO parametrized class
        builder.superclass(ClassName.bestGuess(parentClassName));

        final String innerClassName = className.replace('$', '.');
        if (!saveFields.isEmpty()) {
            builder.addFields(SaveFieldProcessor.getFields(saveFields));
            builder.addMethod(SaveFieldProcessor.getRestoreMethod(packageName, innerClassName, saveFields));
            builder.addMethod(SaveFieldProcessor.getSaveMethod(packageName, innerClassName, saveFields));
        }

        if (!viewFields.isEmpty()) {
            builder.addMethod(ViewFieldProcessor.getBindMethod(packageName, innerClassName, viewFields));
            builder.addMethod(ViewFieldProcessor.getUnbindMethod(packageName, innerClassName, viewFields));
        }

        return builder.build();
    }
}
