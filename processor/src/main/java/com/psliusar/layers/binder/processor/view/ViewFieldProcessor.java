package com.psliusar.layers.binder.processor.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.binder.processor.BinderClassHolder;
import com.psliusar.layers.binder.processor.FieldProcessor;
import com.psliusar.layers.binder.processor.LayersAnnotationProcessor;

import java.lang.annotation.Annotation;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

public class ViewFieldProcessor extends FieldProcessor {

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

        final ViewField field = holder.addViewField(
                fieldName,
                fieldType,
                annotation.value(),
                annotation.parent(),
                annotation.clicks()
        );

        final String manager = getManagerName(annotation);
        field.setManager(manager);
    }

    @Nullable
    private String getManagerName(@NonNull Bind annotation) {
        final LayersAnnotationProcessor ap = getAnnotationProcessor();
        final Elements elements = ap.getElementUtils();
        TypeMirror typeMirror;
        try {
            typeMirror = elements.getTypeElement(annotation.bindManager().getCanonicalName()).asType();
        } catch (MirroredTypeException ex) {
            typeMirror = ex.getTypeMirror();
        }

        if (typeMirror == null || "void".equals(typeMirror.toString())) {
            return null;
        }

        if (!isAssignable(ap, typeMirror, "com.psliusar.layers.binder.ViewBindManager")) {
            throw new IllegalArgumentException("Bind manager must extend class ViewBindManager");
        }

        return typeMirror.toString();
    }
}
