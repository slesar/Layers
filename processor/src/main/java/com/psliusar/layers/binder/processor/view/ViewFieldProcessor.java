package com.psliusar.layers.binder.processor.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.binder.processor.BinderClassHolder;
import com.psliusar.layers.binder.processor.FieldProcessor;
import com.psliusar.layers.binder.processor.LayersAnnotationProcessor;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class ViewFieldProcessor extends FieldProcessor {

    public ViewFieldProcessor(@NonNull LayersAnnotationProcessor annotationProcessor) {
        super(annotationProcessor);
    }

    @Override
    protected void process(@NonNull RoundEnvironment env) {

        for (Element element : env.getElementsAnnotatedWith(Bind.class)) {
            try {
                processField(element);
            } catch (Exception e) {
                getAnnotationProcessor()
                        .logError("Unable to generate binder for %s.")
                        .arguments(element.toString())
                        .throwable(e)
                        .element(element)
                        .print();
            }
        }
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

    private void processField(@NonNull Element element) {

        final BinderClassHolder holder = getHolderForClass(element);

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
}
