package com.psliusar.layers.binder.processor.state;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.binder.Save;
import com.psliusar.layers.binder.processor.BinderClassHolder;
import com.psliusar.layers.binder.processor.FieldProcessor;
import com.psliusar.layers.binder.processor.LayersAnnotationProcessor;
import com.psliusar.layers.binder.processor.Processor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class SaveFieldProcessor extends FieldProcessor {

    private static final String METHOD_PARAM_OBJECT = "object";
    private static final String METHOD_PARAM_STATE = "state";
    private static final String METHOD_VAR_TARGET = "target";
    private static final String METHOD_VAR_KEY_PREFIX = "keyPrefix";

    private static final HashMap<String, String> PRIMITIVES = new HashMap<String, String>() {
        {
            put("boolean", "Boolean");
            put("java.lang.Boolean", "BooleanBoxed");
            put("byte", "Byte");
            put("java.lang.Byte", "ByteBoxed");
            put("char", "Char");
            put("java.lang.Character", "CharBoxed");
            put("short", "Short");
            put("java.lang.Short", "ShortBoxed");
            put("int", "Int");
            put("java.lang.Integer", "IntBoxed");
            put("long", "Long");
            put("java.lang.Long", "LongBoxed");
            put("float", "Float");
            put("java.lang.Float", "FloatBoxed");
            put("double", "Double");
            put("java.lang.Double", "DoubleBoxed");
            put("java.lang.String", "String");
            put("java.lang.CharSequence", "CharSequence");

            // XXX
            put("android.os.Parcelable", "Parcelable");
            put("android.os.Parcelable[]", "ParcelableArray");
            put("java.util.ArrayList<android.os.Parcelable>", "ParcelableArrayList");
            put("android.util.SparseArray<android.os.Parcelable>", "SparseParcelableArray");

            put("java.util.ArrayList<java.lang.Integer>", "IntegerArrayList");
            put("java.util.ArrayList<java.lang.String>", "StringArrayList");
            put("java.util.ArrayList<java.lang.CharSequence>", "CharSequenceArrayList");

            put("boolean[]", "BooleanArray");
            put("byte[]", "ByteArray");
            put("short[]", "ShortArray");
            put("char[]", "CharArray");
            put("int[]", "IntArray");
            put("long[]", "LongArray");
            put("float[]", "FloatArray");
            put("double[]", "DoubleArray");
            put("java.lang.String[]", "StringArray");
            put("java.lang.CharSequence[]", "CharSequenceArray");

            put("android.os.Bundle", "Bundle");
            put("java.io.Serializable", "Serializable");
        }
    };

    public SaveFieldProcessor(@NonNull LayersAnnotationProcessor proc) {
        super(proc);
    }

    @NonNull
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return Save.class;
    }

    @Nullable
    @Override
    protected String getRootClassName() {
        return null;
    }

    @Override
    protected void process(@NonNull RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(Save.class)) {
            try {
                processField(element);
            } catch (Exception e) {
                getAnnotationProcessor()
                        .logError("Unable to generate save binder for %s.")
                        .arguments(element.toString())
                        .throwable(e)
                        .element(element)
                        .print();
            }
        }
    }

    private void processField(@NonNull Element element) {
        final BinderClassHolder holder = getHolderForClass(element);

        final Save annotation = element.getAnnotation(Save.class);
        final String fieldName = element.getSimpleName().toString();
        final String fieldType = element.asType().toString();

        //Class managerClass = annotation.stateManager();
        //final String manager = managerClass == void.class ? "" : managerClass.getCanonicalName();
        final String manager = null;

        final String customName = annotation.name();
        final String key;
        if ("".equals(customName)) {
            key = elementNameToSnakeCase(fieldName);
        } else {
            key = customName;
        }

        String suffix = getTypeSuffix(element);

        holder.addSaveField(
                fieldName,
                fieldType,
                manager,
                key,
                suffix
        );
    }

    private static String getTypeSuffix(@NonNull Element element) {
        final TypeMirror typeMirror = element.asType();

        // Detect arrays
        String elementType = typeMirror.toString();
        if (typeMirror.getKind() == TypeKind.ARRAY) {
            elementType += "[]";
        }

        // Detect primitive types
        final String primitive = PRIMITIVES.get(elementType);
        if (primitive != null) {
            return primitive;
        }

        // TODO Detect Parcelables

        return "";
    }

    @NonNull
    public static MethodSpec getRestoreMethod(
            @NonNull String packageName,
            @NonNull String className,
            @NonNull List<SaveField> fields) {
        final ClassName targetClass = ClassName.get(packageName, className);

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

        builder.addStatement(
                "super.restore($L, $L)",
                METHOD_PARAM_OBJECT,
                METHOD_PARAM_STATE
        );

        // TODO -> initClassLoader(state, object);
        // Loop through fields and find out whether they need to define class loader or not

        // -> final NextLayer target = (NextLayer) object;
        builder.addStatement(
                "final $T $L = ($T) $L",
                targetClass,
                METHOD_VAR_TARGET,
                targetClass,
                METHOD_PARAM_OBJECT
        );

        // TODO -> final String keyPrefix = "...";
        builder.addStatement(
                "final String $L = $S",
                METHOD_VAR_KEY_PREFIX,
                getKeyPrefix(className)
        );

        for (SaveField field : fields) {
            // -> target.number = getInt(key, target.field, state);
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


        return builder.build();
    }

    @NonNull
    public static MethodSpec getSaveMethod(
            @NonNull String packageName,
            @NonNull String className,
            @NonNull List<SaveField> fields) {
        final ClassName targetClass = ClassName.get(packageName, className);

        final MethodSpec.Builder builder = MethodSpec.methodBuilder("save")
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

        builder.addStatement(
                "super.save($L, $L)",
                METHOD_PARAM_OBJECT,
                METHOD_PARAM_STATE
        );

        // -> final NextLayer target = (NextLayer) object;
        builder.addStatement(
                "final $T $L = ($T) $L",
                targetClass,
                METHOD_VAR_TARGET,
                targetClass,
                METHOD_PARAM_OBJECT
        );

        // TODO -> final String keyPrefix = "...";
        builder.addStatement(
                "final String $L = $S",
                METHOD_VAR_KEY_PREFIX,
                getKeyPrefix(className)
        );

        for (SaveField field : fields) {
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
        }

        return builder.build();
    }

    @NonNull
    private static String getKeyPrefix(@NonNull String className) {
        return Processor.elementNameToSnakeCase(className) + "$$";
    }
}
