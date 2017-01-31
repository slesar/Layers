package com.psliusar.layers.binder.processor.state;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.binder.Save;
import com.psliusar.layers.binder.processor.BinderClassHolder;
import com.psliusar.layers.binder.processor.FieldProcessor;
import com.psliusar.layers.binder.processor.LayersAnnotationProcessor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import java.lang.annotation.Annotation;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

public class SaveFieldProcessor extends FieldProcessor {

    private static final String METHOD_PARAM_TARGET = "target";
    private static final String METHOD_PARAM_STATE = "state";

    public SaveFieldProcessor(@NonNull LayersAnnotationProcessor proc) {
        super(proc);
    }

    @NonNull
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return Save.class;
    }

    @Nullable
    @Override
    protected String getRootClassName() {
        return null;
    }

    @Override
    protected void process(@NonNull RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(Save.class)) {
            try {
                processField(element);
            } catch (Exception e) {
                getAnnotationProcessor()
                        .logError("Unable to generate save binder for %s.")
                        .arguments(element.toString())
                        .throwable(e)
                        .element(element)
                        .print();
            }
        }
    }

    private void processField(@NonNull Element element) {
        final BinderClassHolder holder = getHolderForClass(element);

        final Save annotation = element.getAnnotation(Save.class);
        final String fieldName = element.getSimpleName().toString();
        final String fieldType = element.asType().toString();

        Class managerClass = annotation.stateManager();
        final String manager = managerClass == void.class ? "" : managerClass.getCanonicalName();

        String name = annotation.name();
        final String customName;
        if ("".equals(name)) {
            // TODO convert to UPPER_CASE
            customName = fieldName;
        } else {
            customName = name;
        }

        holder.addSaveField(
                fieldName,
                fieldType,
                manager,
                customName
        );
    }

    @NonNull
    public static MethodSpec getRestoreMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("restore")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterSpec
                        .builder(ClassName.get("java.lang", "Object"), METHOD_PARAM_TARGET)
                        .addAnnotation(ClassName.get(NonNull.class))
                        .build())
                .addParameter(ParameterSpec
                        .builder(ClassName.get("android.os", "Bundle"), METHOD_PARAM_STATE)
                        .addAnnotation(ClassName.get(NonNull.class))
                        .build());

        builder.addStatement(
                "super.restore($L, $L)",
                METHOD_PARAM_TARGET,
                METHOD_PARAM_STATE
        );

        return builder.build();
    }

    @NonNull
    public static MethodSpec getSaveMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("save")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterSpec
                        .builder(ClassName.get("java.lang", "Object"), METHOD_PARAM_TARGET)
                        .addAnnotation(ClassName.get(NonNull.class))
                        .build())
                .addParameter(ParameterSpec
                        .builder(ClassName.get("android.os", "Bundle"), METHOD_PARAM_STATE)
                        .addAnnotation(ClassName.get(NonNull.class))
                        .build());

        builder.addStatement(
                "super.save($L, $L)",
                METHOD_PARAM_TARGET,
                METHOD_PARAM_STATE
        );

        return builder.build();
    }
}
