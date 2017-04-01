package com.psliusar.layers.binder.processor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.binder.BinderConstants;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.WildcardTypeName;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public abstract class Processor {

    protected static String getPackageName(
            @NonNull TypeElement type,
            @NonNull Elements elements) {
        return elements.getPackageOf(type).getQualifiedName().toString();
    }

    protected static String getBinderClassName(
            @NonNull String packageName,
            @NonNull String simpleClassName) {
        return packageName + "." + simpleClassName + BinderConstants.BINDER_SUFFIX;
    }

    protected static String getSimpleClassName(
            @NonNull TypeElement type,
            @NonNull String packageName) {
        return type.getQualifiedName().toString().substring(packageName.length() + 1).replace('.', '$');
    }

    protected static String elementNameToSnakeCase(@NonNull String elementName) {
        return elementName.replaceAll("([a-z0-9])([A-Z0-9])", "$1_$2").toUpperCase();
    }

    @NonNull
    protected static String typeNameToFieldName(@NonNull String typeName) {
        typeName = typeName.replaceAll("^.+\\.", "");
        String result = String.valueOf(typeName.charAt(0)).toLowerCase();
        if (typeName.length() > 1) {
            result = result + typeName.substring(1);
        }
        return result;
    }

    protected static boolean ensureSubtypeOfType(
            @NonNull Elements elements,
            @NonNull Types types,
            @NonNull TypeMirror elementType,
            @NonNull String targetInterface) {
        final TypeElement sourceElement = elements.getTypeElement(elementType.toString());

        if (sourceElement == null) {
            return false;
        }

        boolean isTargetType = false;
        TypeElement element = sourceElement;

        while (!isTargetType && element != null) {
            for (TypeMirror mirror : element.getInterfaces()) {
                if (types.erasure(mirror).toString().equals(targetInterface)) {
                    isTargetType = true;
                }
            }

            TypeMirror superClassMirror = element.getSuperclass();
            if (superClassMirror != null) {
                superClassMirror = types.erasure(superClassMirror);

                element = elements.getTypeElement(superClassMirror.toString());
            } else {
                element = null;
            }
        }

        if (!isTargetType) {
            return false;
        }

        boolean hasConstructor = false;
        for (Element enclosedElement : sourceElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR
                    && !enclosedElement.getModifiers().contains(Modifier.PRIVATE)
                    && ((ExecutableElement) enclosedElement).getParameters().size() == 0) {
                hasConstructor = true;
            }
        }

        if (!hasConstructor) {
            throw new IllegalArgumentException(elementType + " must have non-private default constructor");
        }

        return true;
    }

    protected static boolean isAssignable(@NonNull LayersAnnotationProcessor ap, @NonNull String type, @NonNull String target) {
        final Elements elements = ap.getElementUtils();
        return ap.getTypeUtils()
                .isAssignable(
                        elements.getTypeElement(type).asType(),
                        elements.getTypeElement(target).asType()
                );
    }

    protected static boolean isAssignable(@NonNull LayersAnnotationProcessor ap, @NonNull TypeMirror typeMirror, @NonNull String target) {
        TypeElement typeElement = (TypeElement) ap.getTypeUtils().asElement(typeMirror);
        final TypeElement parent = ap.getElementUtils().getTypeElement(target);

        if (ap.getTypeUtils().isAssignable(typeElement.asType(), parent.asType())) {
            return true;
        }

        TypeMirror type;
        while (true) {
            type = typeElement.getSuperclass();
            if (type.getKind() == TypeKind.NONE) {
                return false;
            }
            typeElement = (TypeElement) ((DeclaredType) type).asElement();
            if (typeElement.equals(parent)) {
                return true;
            }
        }
    }

    @NonNull
    protected static ClassName guessClassName(@NonNull String fieldType) {
        final String type = Pattern.compile("<.+>$").matcher(fieldType).replaceAll("");
        if (fieldType.length() == type.length()) {
            // No generics
            return ClassName.bestGuess(fieldType);
        } else {
            return ParameterizedTypeName.get(ClassName.bestGuess(type), WildcardTypeName.subtypeOf(Object.class)).rawType;
        }
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
