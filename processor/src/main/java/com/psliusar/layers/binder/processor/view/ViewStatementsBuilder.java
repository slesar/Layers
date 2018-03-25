package com.psliusar.layers.binder.processor.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.psliusar.layers.binder.processor.Processor;
import com.psliusar.layers.binder.processor.builder.StatementsBuilder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.lang.model.element.Modifier;

public class ViewStatementsBuilder extends StatementsBuilder<ViewField> {

    private static final String PARENT_NAME_FORMAT = "parent_%03d";
    private static final String METHOD_PARAM_LISTENER = "listener";
    private static final String METHOD_PARAM_VIEW = "view";
    private static final String METHOD_VAR_TARGET = "target";

    public ViewStatementsBuilder(@NonNull String packageName, @NonNull String className, @NonNull List<ViewField> fields) {
        super(packageName, className, fields);
    }

    @Nullable
    @Override
    public List<FieldSpec> createClassFields() {
        final ArrayList<FieldSpec> specs = new ArrayList<>();
        final HashMap<String, FieldSpec> managers = new HashMap<>();
        for (ViewField field : getFields()) {
            final String manager = field.getManager();
            if (manager == null) {
                continue;
            }
            FieldSpec managerField = managers.get(manager);
            if (managerField == null) {
                final String managerFieldName = Processor.typeNameToFieldName(manager);
                final ClassName managerClassName = ClassName.bestGuess(manager);

                // -> protected final ManagerType managerType = new ManagerType();
                managerField = FieldSpec
                        .builder(managerClassName, managerFieldName, Modifier.PROTECTED, Modifier.FINAL)
                        .initializer("new $T()", managerClassName)
                        .build();

                managers.put(manager, managerField);
                specs.add(managerField);
            }
            field.setManagerField(managerField);
        }
        return specs;
    }

    @Nullable
    @Override
    public Iterable<MethodSpec> createMethods() {
        final ArrayList<MethodSpec> methods = new ArrayList<>();
        methods.add(getBindMethod());
        methods.add(getUnbindMethod());
        return methods;
    }

    @NonNull
    public MethodSpec getBindMethod() {
        final ClassName viewClass = ClassName.get("android.view", "View");
        final ClassName targetClass = ClassName.get(getPackageName(), getClassName());

        final MethodSpec.Builder builder = MethodSpec.methodBuilder("bind")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterSpec
                        .builder(viewClass.nestedClass("OnClickListener"), METHOD_PARAM_LISTENER)
                        .addAnnotation(NonNull.class)
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

        // -> final NextObject target = (NextObject) listener;
        builder.addStatement(
                "final $T $L = ($T) $L",
                targetClass,
                METHOD_VAR_TARGET,
                targetClass,
                METHOD_PARAM_LISTENER
        );

        final Map<Integer, ParentView> parents = new HashMap<>();
        for (ViewField field : getFields()) {
            getParentView(parents, field.getParentContainer());
        }

        for (ParentView parent : parents.values()) {
            // -> final View parent_001 = find(View.class, view, R.id.container);
            builder.addStatement(
                    "final $T $L = find($L, $L)",
                    viewClass,
                    parent.getVarName(),
                    METHOD_PARAM_VIEW,
                    parent.getResId()
            );
        }

        for (ViewField field : getFields()) {
            final ClassName fieldTypeClass = ClassName.bestGuess(field.getFieldType());
            final ParentView parentView = getParentView(parents, field.getParentContainer());
            final String parent = parentView == null ? METHOD_PARAM_VIEW : parentView.getVarName();

            final FieldSpec manager = field.getManagerField();
            if (manager == null) {
                // -> target.button = (Button) find(parent_001, R.id.button);
                builder.addStatement(
                        "$L.$L = ($T) find($L, $L)",
                        METHOD_VAR_TARGET,
                        field.getFieldName(),
                        fieldTypeClass,
                        parent,
                        field.getResId()
                );
            } else {
                // Bind with manager
                // -> target.button = (Button) managerField.find(target, parent_001, R.id.button);
                builder.addStatement(
                        //"$N.put($L + $S, $L.$L, $L)",
                        "$L.$L = ($T) $N.find($L, $L, $L)",
                        METHOD_VAR_TARGET,
                        field.getFieldName(),
                        fieldTypeClass,
                        manager,
                        METHOD_VAR_TARGET,
                        parent,
                        field.getResId()
                );
            }

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
    public MethodSpec getUnbindMethod() {
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

        final ClassName targetClass = ClassName.get(getPackageName(), getClassName());
        // -> final NextObject target = (NextObject) listener");
        builder.addStatement(
                "final $T $L = ($T) $L",
                targetClass,
                METHOD_VAR_TARGET,
                targetClass,
                METHOD_PARAM_LISTENER
        );

        for (ViewField field : getFields()) {
            // -> target.button = null;
            builder.addStatement(
                    "$L.$L = null",
                    METHOD_VAR_TARGET,
                    field.getFieldName()
            );
        }

        return builder.build();
    }

    @Nullable
    private static ParentView getParentView(
            @NonNull Map<Integer, ParentView> parents,
            @NonNull Integer resId) {
        if (resId == View.NO_ID) {
            return null;
        }

        ParentView parent = parents.get(resId);
        if (parent == null) {
            final String name = String.format(Locale.US, PARENT_NAME_FORMAT, parents.size() + 1);
            parent = new ParentView(resId, name);
            parents.put(resId, parent);
        }

        return parent;
    }
}
