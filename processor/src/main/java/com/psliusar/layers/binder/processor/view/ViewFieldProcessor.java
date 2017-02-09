package com.psliusar.layers.binder.processor.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.binder.processor.BinderClassHolder;
import com.psliusar.layers.binder.processor.FieldProcessor;
import com.psliusar.layers.binder.processor.LayersAnnotationProcessor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

public class ViewFieldProcessor extends FieldProcessor {

    private static final String PARENT_NAME_FORMAT = "parent_%03d";
    private static final String METHOD_PARAM_LISTENER = "listener";
    private static final String METHOD_PARAM_VIEW = "view";
    private static final String METHOD_VAR_TARGET = "target";

    public ViewFieldProcessor(@NonNull LayersAnnotationProcessor proc) {
        super(proc);
    }

    @NonNull
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return Bind.class;
    }

    @Nullable
    @Override
    protected String getRootClassName() {
        return "android.view.View";
    }

    @Override
    protected void process(@NonNull RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(Bind.class)) {
            try {
                processField(element);
            } catch (Exception e) {
                getAnnotationProcessor()
                        .logError("Unable to generate view binder for %s.")
                        .arguments(element.toString())
                        .throwable(e)
                        .element(element)
                        .print();
            }
        }
    }

    private void processField(@NonNull Element element) {
        final BinderClassHolder holder = getClassHolderForField(element);

        final Bind annotation = element.getAnnotation(Bind.class);
        final String fieldName = element.getSimpleName().toString();
        final String fieldType = element.asType().toString();

        holder.addViewField(
                fieldName,
                fieldType,
                annotation.value(),
                annotation.parent(),
                annotation.clicks()
        );
    }

    @NonNull
    public static MethodSpec getBindMethod(
            @NonNull String packageName,
            @NonNull String className,
            @NonNull List<ViewField> fields) {
        final ClassName viewClass = ClassName.get("android.view", "View");
        final ClassName targetClass = ClassName.get(packageName, className);

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

        // -> final NextLayer target = (NextLayer) listener;
        builder.addStatement(
                "final $T $L = ($T) $L",
                targetClass,
                METHOD_VAR_TARGET,
                targetClass,
                METHOD_PARAM_LISTENER
        );

        final Map<Integer, ParentView> parents = new HashMap<>();
        for (ViewField field : fields) {
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

        for (ViewField field : fields) {
            final ClassName fieldTypeClass = ClassName.bestGuess(field.getFieldType());
            final ParentView parentView = getParentView(parents, field.getParentContainer());
            final String parent = parentView == null ? METHOD_PARAM_VIEW : parentView.getVarName();

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
    public static MethodSpec getUnbindMethod(
            @NonNull String packageName,
            @NonNull String className,
            @NonNull List<ViewField> fields) {
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

        for (ViewField field : fields) {
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
