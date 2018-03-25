package com.psliusar.layers.binder.processor.save;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.binder.processor.Processor;
import com.psliusar.layers.binder.processor.builder.StatementsBuilder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.lang.model.element.Modifier;

public class SaveStatementsBuilder extends StatementsBuilder<SaveField> {

    private static final String METHOD_PARAM_OBJECT = "object";
    private static final String METHOD_PARAM_STATE = "state";
    private static final String METHOD_VAR_TARGET = "target";
    private static final String METHOD_VAR_KEY_PREFIX = "keyPrefix";

    public SaveStatementsBuilder(@NonNull String packageName, @NonNull String innerClassName, @NonNull List<SaveField> fields) {
        super(packageName, innerClassName, fields);
    }

    @Nullable
    @Override
    public List<FieldSpec> createClassFields() {
        final ArrayList<FieldSpec> specs = new ArrayList<>();
        final HashMap<String, FieldSpec> managers = new HashMap<>();
        for (SaveField field : getFields()) {
            final String manager = field.getManager();
            if (manager == null) {
                continue;
            }
            FieldSpec managerField = managers.get(manager);
            if (managerField == null) {
                final String managerFieldName = Processor.typeNameToFieldName(manager);
                final ClassName managerClassName = ClassName.bestGuess(manager);

                // -> protected final ManagerType managerType = new ManagerType();
                managerField = FieldSpec
                        .builder(managerClassName, managerFieldName, Modifier.PROTECTED, Modifier.FINAL)
                        .initializer("new $T()", managerClassName)
                        .build();

                managers.put(manager, managerField);
                specs.add(managerField);
            }
            field.setManagerField(managerField);
        }
        return specs;
    }

    @Nullable
    @Override
    public Iterable<MethodSpec> createMethods() {
        final ArrayList<MethodSpec> methods = new ArrayList<>();
        methods.add(getSaveMethod());
        methods.add(getRestoreMethod());
        final MethodSpec unbindTracks = getUnbindTrackManagersMethod();
        if (unbindTracks != null) {
            methods.add(unbindTracks);
        }
        return methods;
    }

    @NonNull
    public MethodSpec getSaveMethod() {
        final ClassName targetClass = ClassName.get(getPackageName(), getClassName());

        final MethodSpec.Builder builder = MethodSpec.methodBuilder("save")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterSpec
                        .builder(ClassName.get("java.lang", "Object"), METHOD_PARAM_OBJECT)
                        .addAnnotation(NonNull.class)
                        .build())
                .addParameter(ParameterSpec
                        .builder(ClassName.get("android.os", "Bundle"), METHOD_PARAM_STATE)
                        .addAnnotation(NonNull.class)
                        .build());

        // -> super.save(object, state);
        builder.addStatement(
                "super.save($L, $L)",
                METHOD_PARAM_OBJECT,
                METHOD_PARAM_STATE
        );

        // -> final NextObject target = (NextObject) object;
        builder.addStatement(
                "final $T $L = ($T) $L",
                targetClass,
                METHOD_VAR_TARGET,
                targetClass,
                METHOD_PARAM_OBJECT
        );

        // -> final String keyPrefix = "SUBCLASS_NAME$$";
        builder.addStatement(
                "final String $L = $S",
                METHOD_VAR_KEY_PREFIX,
                Processor.getKeyPrefix(getClassName())
        );

        for (SaveField field : getFields()) {
            final FieldSpec manager = field.getManagerField();
            if (manager == null) {
                // Save known types
                // -> putInt(key, target.field, state);
                builder.addStatement(
                        "put$L($L + $S, $L.$L, $L)",
                        field.getMethodSuffix(),
                        METHOD_VAR_KEY_PREFIX,
                        field.getKey(),
                        METHOD_VAR_TARGET,
                        field.getFieldName(),
                        METHOD_PARAM_STATE
                );
            } else {
                // Save with manager
                // -> managerField.put(key, target.field, state);
                builder.addStatement(
                        "$N.put($L + $S, $L.$L, $L)",
                        manager,
                        METHOD_VAR_KEY_PREFIX,
                        field.getKey(),
                        METHOD_VAR_TARGET,
                        field.getFieldName(),
                        METHOD_PARAM_STATE
                );
            }
        }

        return builder.build();
    }

    @NonNull
    public MethodSpec getRestoreMethod() {
        final ClassName targetClass = ClassName.get(getPackageName(), getClassName());

        final MethodSpec.Builder builder = MethodSpec.methodBuilder("restore")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterSpec
                        .builder(ClassName.get("java.lang", "Object"), METHOD_PARAM_OBJECT)
                        .addAnnotation(ClassName.get(NonNull.class))
                        .build())
                .addParameter(ParameterSpec
                        .builder(ClassName.get("android.os", "Bundle"), METHOD_PARAM_STATE)
                        .addAnnotation(ClassName.get(NonNull.class))
                        .build());

        // -> super.restore(object, state);
        builder.addStatement(
                "super.restore($L, $L)",
                METHOD_PARAM_OBJECT,
                METHOD_PARAM_STATE
        );

        // Loop through fields and find out whether they need to define class loader or not
        for (SaveField field : getFields()) {
            if (field.needsClassLoader()) {
                // -> initClassLoader(state, object);
                builder.addStatement(
                        "initClassLoader($L, $L)",
                        METHOD_PARAM_STATE,
                        METHOD_PARAM_OBJECT
                );
                break;
            }
        }

        // -> final NextLayer target = (NextLayer) object;
        builder.addStatement(
                "final $T $L = ($T) $L",
                targetClass,
                METHOD_VAR_TARGET,
                targetClass,
                METHOD_PARAM_OBJECT
        );

        // -> final String keyPrefix = "SUBCLASS_NAME$$";
        builder.addStatement(
                "final String $L = $S",
                METHOD_VAR_KEY_PREFIX,
                Processor.getKeyPrefix(getClassName())
        );

        // TODO Needs optimization
        for (SaveField field : getFields()) {
            final FieldSpec manager = field.getManagerField();
            if (manager != null) {
                // Has custom manager
                // -> target.field = managerField.get(key, state);
                builder.addStatement(
                        "$L.$L = $N.get($L + $S, $L)",
                        METHOD_VAR_TARGET,
                        field.getFieldName(),
                        manager,
                        METHOD_VAR_KEY_PREFIX,
                        field.getKey(),
                        METHOD_PARAM_STATE
                );
            } else if (field.needsParcelableWrapper()) {
                // Copy parcelables array to array of given type
                // -> target.field = copyParcelableArray(getParcelableArray(key, state), targetClass[].class);
                builder.addStatement(
                        "$L.$L = copyParcelableArray(get$L($L + $S, $L), $L.class)",
                        METHOD_VAR_TARGET,
                        field.getFieldName(),
                        field.getMethodSuffix(),
                        METHOD_VAR_KEY_PREFIX,
                        field.getKey(),
                        METHOD_PARAM_STATE,
                        field.getFieldType()
                );

            } else if (field.needsSerializableWrapper()) {
                // Copy serializables array to array of given type
                // -> target.field = copySerializableArray(getSerializableArray(key, state), targetClass[].class);
                builder.addStatement(
                        "$L.$L = copySerializableArray(get$L($L + $S, $L), $L.class)",
                        METHOD_VAR_TARGET,
                        field.getFieldName(),
                        field.getMethodSuffix(),
                        METHOD_VAR_KEY_PREFIX,
                        field.getKey(),
                        METHOD_PARAM_STATE,
                        field.getFieldType()
                );
            } else {
                // Everything else
                // -> target.field = getInt(key, state);
                builder.addStatement(
                        "$L.$L = get$L($L + $S, $L)",
                        METHOD_VAR_TARGET,
                        field.getFieldName(),
                        field.getMethodSuffix(),
                        METHOD_VAR_KEY_PREFIX,
                        field.getKey(),
                        METHOD_PARAM_STATE
                );
            }
        }

        return builder.build();
    }

    @Nullable
    private MethodSpec getUnbindTrackManagersMethod() {
        final ClassName targetClass = ClassName.get(getPackageName(), getClassName());

        final MethodSpec.Builder builder = MethodSpec.methodBuilder("unbindTrackManagers")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterSpec
                        .builder(ClassName.get("java.lang", "Object"), METHOD_PARAM_OBJECT)
                        .addAnnotation(ClassName.get(NonNull.class))
                        .build());

        // -> super.unbindTracks(object);
        builder.addStatement(
                "super.unbindTrackManagers($L)",
                METHOD_PARAM_OBJECT
        );

        // -> final NextLayer target = (NextLayer) object;
        builder.addStatement(
                "final $T $L = ($T) $L",
                targetClass,
                METHOD_VAR_TARGET,
                targetClass,
                METHOD_PARAM_OBJECT
        );

        int fieldsProcessed = 0;
        for (SaveField field : getFields()) {
            if (!SaveFieldProcessor.SUFFIX_TRACK_MANAGER.equals(field.getMethodSuffix())) {
                continue;
            }

            // -> if (target.trackManager != null) target.trackManager.dropCallbacks();
            builder.addStatement(
                    "if ($L.$L != null) $L.$L.dropCallbacks()",
                    METHOD_VAR_TARGET,
                    field.getFieldName(),
                    METHOD_VAR_TARGET,
                    field.getFieldName()
            );
            fieldsProcessed++;
        }

        if (fieldsProcessed > 0) {
            return builder.build();
        } else {
            return null;
        }
    }
}
