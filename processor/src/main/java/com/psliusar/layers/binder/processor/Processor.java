package com.psliusar.layers.binder.processor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.binder.LayerBinder;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public abstract class Processor {

    public static String getPackageName(
            @NonNull TypeElement type,
            @NonNull Elements elements) {
        return elements.getPackageOf(type).getQualifiedName().toString();
    }

    public static String getBinderClassName(
            @NonNull String packageName,
            @NonNull String simpleClassName) {
        return packageName + "." + simpleClassName + LayerBinder.BINDER_SUFFIX;
    }

    public static String getSimpleClassName(
            @NonNull TypeElement type,
            @NonNull String packageName) {
        return type.getQualifiedName().toString().substring(packageName.length() + 1).replace('.', '$');
    }

    private final LayersAnnotationProcessor annotationProcessor;

    public Processor(@NonNull LayersAnnotationProcessor annotationProcessor) {
        this.annotationProcessor = annotationProcessor;
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
