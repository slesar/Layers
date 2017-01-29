package com.psliusar.layers.binder.processor;

import android.support.annotation.NonNull;
import android.view.View;

import com.psliusar.layers.binder.ViewBinder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.lang.model.element.Modifier;

public class BinderClassHolder {

    private static final String PARENT_NAME_FORMAT = "parent_%03d";
    private static final String METHOD_PARAM_LISTENER = "listener";
    private static final String METHOD_PARAM_VIEW = "view";
    private static final String METHOD_VAR_TARGET = "target";

    private final String packageName;
    private final String className;
    private final String parentClassName;

    private final List<FieldDescription> fields = new ArrayList<>();
    private final Map<Integer, ParentDescription> parents = new HashMap<>();

    private boolean fileWritten;

    public BinderClassHolder(
            @NonNull String packageName,
            @NonNull String className,
            @NonNull String parentClassName) {
        this.packageName = packageName;
        this.className = className;
        this.parentClassName = parentClassName;
    }

    public void addField(@NonNull String fieldName, @NonNull String fieldType, int resId, @NonNull Integer parentResId, boolean clickListener) {
        final FieldDescription desc = new FieldDescription(fieldName, fieldType, resId, parentResId, clickListener);
        getParentName(parentResId, METHOD_PARAM_VIEW);
        fields.add(desc);
    }

    @NonNull
    public String getParentName(@NonNull Integer resId, @NonNull String fallback) {
        if (resId == View.NO_ID) {
            return fallback;
        }

        ParentDescription parent = parents.get(resId);
        if (parent == null) {
            final String name = String.format(Locale.US, PARENT_NAME_FORMAT, parents.size() + 1);
            parent = new ParentDescription(resId, name);
            parents.put(resId, parent);
        }

        return parent.getVarName();
    }

    public void setFileWritten() {
        fileWritten = true;
    }

    public boolean isFileWritten() {
        return fileWritten;
    }

    @NonNull
    public String getJavaClassFile() {
        return JavaFile.builder(packageName, getTypeSpec()).build().toString();
    }

    @NonNull
    private TypeSpec getTypeSpec() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(className + ViewBinder.BINDER_SUFFIX)
                .addModifiers(Modifier.PUBLIC);

        // TODO parametrized class
        builder.superclass(ClassName.bestGuess(parentClassName));

        builder.addMethod(getBindMethod());
        builder.addMethod(getUnbindMethod());

        return builder.build();
    }

    @NonNull
    private MethodSpec getBindMethod() {
        final ClassName viewClass = ClassName.get("android.view", "View");
        final ClassName targetClass = ClassName.get(packageName, className);

        final MethodSpec.Builder builder = MethodSpec.methodBuilder("bind")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterSpec
                        .builder(viewClass.nestedClass("OnClickListener"), METHOD_PARAM_LISTENER)
                        .addAnnotation(ClassName.get(NonNull.class))
                        .build())
                .addParameter(ParameterSpec
                        .builder(viewClass, METHOD_PARAM_VIEW)
                        .addAnnotation(NonNull.class)
                        .build());

        builder.addStatement(
                "super.bind($L, $L)",
                METHOD_PARAM_LISTENER,
                METHOD_PARAM_VIEW
        );

        // -> final NextLayer target = (NextLayer) listener;
        builder.addStatement(
                "final $T $L = ($T) $L",
                targetClass,
                METHOD_VAR_TARGET,
                targetClass,
                METHOD_PARAM_LISTENER
        );

        for (Map.Entry<Integer, ParentDescription> entry : parents.entrySet()) {
            // -> final View parent_001 = find(View.class, view, R.id.container);
            builder.addStatement(
                    "final $T $L = find($L, $L)",
                    viewClass,
                    entry.getValue().getVarName(),
                    METHOD_PARAM_VIEW,
                    entry.getValue().getResId()
            );
        }

        for (FieldDescription field : fields) {
            final ClassName fieldTypeClass = ClassName.bestGuess(field.getFieldType());
            final String parent = getParentName(field.getParentContainer(), METHOD_PARAM_VIEW);

            // -> target.button = (Button) find(Button.class, parent_001, R.id.button);
            builder.addStatement(
                    "$L.$L = ($T) find($L, $L)",
                    METHOD_VAR_TARGET,
                    field.getFieldName(),
                    fieldTypeClass,
                    parent,
                    field.getResId()
            );

            // -> target.button.setOnClickListener(target);
            if (field.isClickListener()) {
                builder.addStatement(
                        "$L.$L.setOnClickListener($L)",
                        METHOD_VAR_TARGET,
                        field.getFieldName(),
                        METHOD_VAR_TARGET
                );
            }
        }

        return builder.build();
    }

    @NonNull
    private MethodSpec getUnbindMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("unbind")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterSpec
                        .builder(ClassName.get("android.view", "View", "OnClickListener"), METHOD_PARAM_LISTENER)
                        .addAnnotation(NonNull.class)
                        .build());

        builder.addStatement(
                "super.unbind($L)",
                METHOD_PARAM_LISTENER
        );

        final ClassName targetClass = ClassName.get(packageName, className);
        // -> final NextLayer target = (NextLayer) listener");
        builder.addStatement(
                "final $T $L = ($T) $L",
                targetClass,
                METHOD_VAR_TARGET,
                targetClass,
                METHOD_PARAM_LISTENER
        );

        for (FieldDescription field : fields) {
            // -> target.button = null;
            builder.addStatement(
                    "$L.$L = null",
                    METHOD_VAR_TARGET,
                    field.getFieldName()
            );
        }

        return builder.build();
    }

    public static class FieldDescription {

        private final String fieldName;
        private final String fieldType;

        private final int resId;
        private final Integer parentContainer;
        private final boolean clickListener;

        public FieldDescription(@NonNull String fieldName, @NonNull String fieldType, int resId, @NonNull Integer parentContainer, boolean clickListener) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
            this.resId = resId;
            this.parentContainer = parentContainer;
            this.clickListener = clickListener;
        }

        @NonNull
        public String getFieldName() {
            return fieldName;
        }

        @NonNull
        public String getFieldType() {
            return fieldType;
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
    }

    private static class ParentDescription {

        private final Integer resId;
        private final String varName;

        public ParentDescription(@NonNull Integer resId, @NonNull String varName) {
            this.resId = resId;
            this.varName = varName;
        }

        @NonNull
        public Integer getResId() {
            return resId;
        }

        @NonNull
        public String getVarName() {
            return varName;
        }
    }
}
