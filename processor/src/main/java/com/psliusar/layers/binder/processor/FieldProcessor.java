package com.psliusar.layers.binder.processor;

import android.support.annotation.NonNull;

import com.psliusar.layers.binder.Bind;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class FieldProcessor extends BinderProcessor {

    @Override
    protected void process(
            @NonNull BinderAnnotationProcessor parentProcessor,
            @NonNull RoundEnvironment env,
            @NonNull Elements elements,
            @NonNull Types types,
            @NonNull Map<String, BinderClassHolder> holders) {

        for (Element element : env.getElementsAnnotatedWith(Bind.class)) {
            try {
                processField(element, elements, types, holders);
            } catch (Exception e) {
                parentProcessor
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

    @NonNull
    @Override
    protected String getRootClassName() {
        return "android.view.View";
    }

    private void processField(
            @NonNull Element element,
            @NonNull Elements elements,
            @NonNull Types types,
            @NonNull Map<String, BinderClassHolder> holders) {

        final BinderClassHolder holder = getHolderForClass(holders, element, elements, types);

        final Bind annotation = element.getAnnotation(Bind.class);
        final String fieldName = element.getSimpleName().toString();
        final String fieldType = element.asType().toString();

        holder.addField(
                fieldName,
                fieldType,
                annotation.value(),
                annotation.parent(),
                annotation.clicks()
        );
    }
}
