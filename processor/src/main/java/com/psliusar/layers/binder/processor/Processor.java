package com.psliusar.layers.binder.processor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.binder.BinderConstants;

import java.lang.annotation.Annotation;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public abstract class Processor {

    public static String getPackageName(
            @NonNull TypeElement type,
            @NonNull Elements elements) {
        return elements.getPackageOf(type).getQualifiedName().toString();
    }

    public static String getBinderClassName(
            @NonNull String packageName,
            @NonNull String simpleClassName) {
        return packageName + "." + simpleClassName + BinderConstants.BINDER_SUFFIX;
    }

    public static String getSimpleClassName(
            @NonNull TypeElement type,
            @NonNull String packageName) {
        return type.getQualifiedName().toString().substring(packageName.length() + 1).replace('.', '$');
    }

    public static String elementNameToSnakeCase(@NonNull String elementName) {
        return elementName.replaceAll("([a-z0-9])([A-Z0-9])", "$1_$2").toUpperCase();
    }

    @NonNull
    public static String typeNameToFieldName(@NonNull String typeName) {
        typeName = typeName.replaceAll("^.+\\.", "");
        String result = String.valueOf(typeName.charAt(0)).toLowerCase();
        if (typeName.length() > 1) {
            result = result + typeName.substring(1);
        }
        return result;
    }

    private final LayersAnnotationProcessor annotationProcessor;

    public Processor(@NonNull LayersAnnotationProcessor proc) {
        annotationProcessor = proc;
    }

    @NonNull
    protected LayersAnnotationProcessor getAnnotationProcessor() {
        return annotationProcessor;
    }

    @NonNull
    protected abstract Class<? extends Annotation> getAnnotation();

    @Nullable
    protected abstract String getRootClassName();

    protected abstract void process(@NonNull RoundEnvironment env);
}
