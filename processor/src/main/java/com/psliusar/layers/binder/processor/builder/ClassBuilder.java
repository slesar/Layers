package com.psliusar.layers.binder.processor.builder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.binder.BinderConstants;
import com.psliusar.layers.binder.processor.LayersAnnotationProcessor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.Modifier;

public class ClassBuilder {

    private final TypeSpec.Builder builder;

    public ClassBuilder(@NonNull String className, @NonNull String parentClassName) {
        builder = TypeSpec.classBuilder(className + BinderConstants.BINDER_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(
                        "Generated class. Do not modify.\n" +
                                "Generator: $T.\n" +
                                "Details: $L\n",
                        LayersAnnotationProcessor.class,
                        "https://github.com/slesar/Layers")
                // TODO parametrized class
                .superclass(ClassName.bestGuess(parentClassName));
    }

    @NonNull
    public ClassBuilder addStatementsBuilder(@NonNull StatementsBuilder<?> statements) {
        addFields(statements.createClassFields());
        addMethods(statements.createMethods());
        return this;
    }

    private void addFields(@Nullable List<FieldSpec> fields) {
        if (fields != null) {
            builder.addFields(fields);
        }
    }

    private void addMethods(@Nullable Iterable<MethodSpec> methods) {
        if (methods != null) {
            builder.addMethods(methods);
        }
    }

    @NonNull
    public TypeSpec build() {
        return builder.build();
    }
}
