package com.psliusar.layers.binder.processor;

import android.support.annotation.NonNull;

import com.psliusar.layers.binder.ViewBinder;
import com.squareup.javapoet.ClassName;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public abstract class BinderProcessor {

    public static String getPackageName(@NonNull TypeElement type, @NonNull Elements elements) {
        return elements.getPackageOf(type).getQualifiedName().toString();
    }

    public static String getBinderClassName(@NonNull String packageName, @NonNull String simpleClassName) {
        return packageName + "." + simpleClassName + ViewBinder.BINDER_SUFFIX;
    }

    public static String getSimpleClassName(@NonNull TypeElement type, @NonNull String packageName) {
        return type.getQualifiedName().toString().substring(packageName.length() + 1).replace('.', '$');
    }

    protected abstract void process(
            @NonNull BinderAnnotationProcessor parentProcessor,
            @NonNull RoundEnvironment env,
            @NonNull Elements elements,
            @NonNull Types types,
            @NonNull Map<String, BinderClassHolder> holders);

    @NonNull
    protected abstract Class<? extends Annotation> getAnnotation();

    @NonNull
    protected abstract String getRootClassName();

    @NonNull
    protected BinderClassHolder getHolderForClass(
            @NonNull Map<String, BinderClassHolder> holders,
            @NonNull Element element,
            @NonNull Elements elements,
            @NonNull Types types) {

        validateField(element, elements, types, getRootClassName());

        final TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        final String packageName = getPackageName(enclosingElement, elements);
        final String className = getSimpleClassName(enclosingElement, packageName);
        final String fqcn = getBinderClassName(packageName, className);

        final String parentClassName = resolveParentClassName(enclosingElement, elements, types);

        BinderClassHolder holder = holders.get(fqcn);
        if (holder == null) {
            holder = new BinderClassHolder(packageName, className, parentClassName);
            holders.put(fqcn, holder);
        }
        return holder;
    }

    @NonNull
    private String resolveParentClassName(
            @NonNull TypeElement enclosingElement,
            @NonNull Elements elements,
            @NonNull Types types) {

        TypeMirror superclass = enclosingElement.getSuperclass();
        final Class<? extends Annotation> annotation = getAnnotation();
        while (superclass.getKind() != TypeKind.NONE) {
            final TypeElement superclassElement = (TypeElement) types.asElement(superclass);

            for (Element element : elements.getAllMembers(superclassElement)) {
                if (element.getAnnotation(annotation) != null) {
                    final String superclassPackageName = elements.getPackageOf(superclassElement)
                            .getQualifiedName()
                            .toString();
                    final String simpleClassName = getSimpleClassName(superclassElement, superclassPackageName);
                    return ClassName.get(superclassPackageName, simpleClassName).toString();
                }
            }

            superclass = superclassElement.getSuperclass();
        }

        return (ViewBinder.class.getCanonicalName());
    }

    protected static boolean validateField(
            @NonNull Element element,
            @NonNull Elements elements,
            @NonNull Types types,
            @NonNull String targetClassName) {

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

        final TypeMirror targetType = elements.getTypeElement(targetClassName).asType();
        if (!types.isAssignable(element.asType(), targetType)) {
            throw new IllegalArgumentException("Field must be of type " + targetClassName + " or its subclass.");
        }

        return true;
    }
}
