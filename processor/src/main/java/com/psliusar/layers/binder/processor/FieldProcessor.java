package com.psliusar.layers.binder.processor;

import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class FieldProcessor extends Processor {

    public FieldProcessor(@NonNull LayersAnnotationProcessor proc) {
        super(proc);
    }

    @NonNull
    protected BinderClassHolder getClassHolderForField(@NonNull Element element) {
        final LayersAnnotationProcessor ap = getAnnotationProcessor();
        final Elements elements = ap.getElementUtils();
        final Types types = ap.getTypeUtils();
        validateField(element, elements, types, getRootClassName());

        final TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        final String packageName = getPackageName(enclosingElement, elements);
        final String className = getSimpleClassName(enclosingElement, packageName);
        final String fqcn = getBinderClassName(packageName, className);

        final String parentClassName = getAnnotationProcessor().resolveParentClassName(enclosingElement);

        final Map<String, BinderClassHolder> holders = ap.getClassHolders();
        BinderClassHolder holder = holders.get(fqcn);
        if (holder == null) {
            holder = new BinderClassHolder(packageName, className, parentClassName);
            holders.put(fqcn, holder);
        }
        return holder;
    }

    private static boolean validateField(
            @NonNull Element element,
            @NonNull Elements elements,
            @NonNull Types types,
            @Nullable String targetClassName) {

        final TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify containing type
        if (enclosingElement.getKind() != ElementKind.CLASS) {
            throw new IllegalArgumentException("View binding may only be applied to fields in classes.");
        }

        // Verify containing class visibility is not private
        if (enclosingElement.getModifiers().contains(Modifier.PRIVATE)) {
            throw new IllegalArgumentException("Field may not be contained in private classes.");
        }

        // Verify method modifiers
        final Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC)) {
            throw new IllegalArgumentException("Field must not be private or static.");
        }

        if (targetClassName != null) {
            final TypeMirror targetType = elements.getTypeElement(targetClassName).asType();
            if (!types.isAssignable(element.asType(), targetType)) {
                throw new IllegalArgumentException("Field must be of type " + targetClassName + " or its subclass.");
            }
        }

        return true;
    }
}
