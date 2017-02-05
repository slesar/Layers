package com.psliusar.layers.binder.processor.save;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class SaveFieldProcessor extends FieldProcessor {

    private static final String METHOD_PARAM_OBJECT = "object";
    private static final String METHOD_PARAM_STATE = "state";
    private static final String METHOD_VAR_TARGET = "target";
    private static final String METHOD_VAR_KEY_PREFIX = "keyPrefix";

    private static final String TYPE_STRING = "java.lang.String";
    private static final String TYPE_INTEGER = "java.lang.Integer";
    private static final String TYPE_CHAR_SEQUENCE = "java.lang.CharSequence";
    private static final String TYPE_PARCELABLE = "android.os.Parcelable";
    private static final String TYPE_SERIALIZABLE = "java.io.Serializable";
    private static final String TYPE_BUNDLE = "android.os.Bundle";

    private static final Pattern PATTERN_SPARSE_ARRAY = Pattern.compile("android\\.util\\.SparseArray<(.*?)>");
    private static final Pattern PATTERN_ARRAY_LIST = Pattern.compile("java\\.util\\.ArrayList<(.*?)>");

    private static final HashMap<String, String> PREDEFINED = new HashMap<String, String>() {
        {
            put("boolean", "Boolean");
            put("boolean[]", "BooleanArray");
            put("java.lang.Boolean", "BooleanBoxed");

            put("byte", "Byte");
            put("byte[]", "ByteArray");
            put("java.lang.Byte", "ByteBoxed");

            put("char", "Char");
            put("char[]", "CharArray");
            put("java.lang.Character", "CharBoxed");

            put("short", "Short");
            put("short[]", "ShortArray");
            put("java.lang.Short", "ShortBoxed");

            put("int", "Int");
            put("int[]", "IntArray");
            put("java.lang.Integer", "IntBoxed");

            put("long", "Long");
            put("long[]", "LongArray");
            put("java.lang.Long", "LongBoxed");

            put("float", "Float");
            put("float[]", "FloatArray");
            put("java.lang.Float", "FloatBoxed");

            put("double", "Double");
            put("double[]", "DoubleArray");
            put("java.lang.Double", "DoubleBoxed");

            put("java.lang.String", "String");
            put("java.lang.String[]", "StringArray");
            put("java.lang.CharSequence", "CharSequence");
            put("java.lang.CharSequence[]", "CharSequenceArray");

            put("android.os.Parcelable", "Parcelable");
            put("android.os.Parcelable[]", "ParcelableArray");

            put("android.util.SparseArray<android.os.Parcelable>", "SparseParcelableArray");

            put("java.util.ArrayList<android.os.Parcelable>", "ParcelableArrayList");
            put("java.util.ArrayList<java.lang.Integer>", "IntegerArrayList");
            put("java.util.ArrayList<java.lang.String>", "StringArrayList");
            put("java.util.ArrayList<java.lang.CharSequence>", "CharSequenceArrayList");

            put("android.os.Bundle", "Bundle");
            put("java.io.Serializable", "Serializable");
            put("java.io.Serializable[]", "Serializable");
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

        final LayersAnnotationProcessor ap = getAnnotationProcessor();
        final String suffix = getTypeSuffix(element, ap.getElementUtils(), ap.getTypeUtils());

        holder.addSaveField(
                fieldName,
                fieldType,
                manager,
                key,
                suffix
        );
    }

    @NonNull
    private static String getTypeSuffix(@NonNull Element element, @NonNull Elements elements, @NonNull Types types) {
        final TypeMirror typeMirror = element.asType();
        String elementType = typeMirror.toString();
        boolean isArray = false;
        boolean canBeArray = true;

        // Detect Bundle
        if (elementType.equals(TYPE_BUNDLE)) {
            canBeArray = false;
        }

        // Detect SparseArray<Parcelable>
        Matcher matcher = PATTERN_SPARSE_ARRAY.matcher(elementType);
        if (matcher.matches()) {
            if (!types.isAssignable(elements.getTypeElement(matcher.group(1)).asType(), elements.getTypeElement(TYPE_PARCELABLE).asType())) {
                // TODO
                throw new IllegalArgumentException("!!!");
            }
            canBeArray = false;
        }

        // Detect ArrayList<String | Integer | CharSequence | Parcelable>
        matcher = PATTERN_ARRAY_LIST.matcher(elementType);
        if (matcher.matches()) {
            final String typeArgument = matcher.group(1);
            if (!TYPE_STRING.equals(typeArgument)
                    && !TYPE_INTEGER.equals(typeArgument)
                    && !TYPE_CHAR_SEQUENCE.equals(typeArgument)
                    && !types.isAssignable(elements.getTypeElement(typeArgument).asType(), elements.getTypeElement(TYPE_PARCELABLE).asType())
                    ) {
                // TODO
                throw new IllegalArgumentException("!!!");
            }
            canBeArray = false;
        }

        // Detect arrays
        if (typeMirror.getKind() == TypeKind.ARRAY) {
            elementType += "[]";
            isArray = true;
        }

        // Search for predefined type
        String predefined = PREDEFINED.get(elementType);

        if (predefined == null) {
            // Detect Parcelable
            if (types.isAssignable(typeMirror, elements.getTypeElement(TYPE_PARCELABLE).asType())) {
                elementType = TYPE_PARCELABLE;
            } else if (types.isAssignable(typeMirror, elements.getTypeElement(TYPE_SERIALIZABLE).asType())) {
                // Detect Serializable
                elementType = TYPE_SERIALIZABLE;
            }

            if (isArray) {
                // TODO wrapper method for copying target array
            }

            // Try again, we might find something new
            predefined = PREDEFINED.get(elementType);
        }

        if (predefined == null) {
            // TODO Message
            throw new IllegalArgumentException("");
        } else {
            if (isArray && !canBeArray) {
                // TODO Message
                throw new IllegalArgumentException("");
            }
            return predefined;
        }
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
                        .addAnnotation(NonNull.class)
                        .build())
                .addParameter(ParameterSpec
                        .builder(ClassName.get("android.os", "Bundle"), METHOD_PARAM_STATE)
                        .addAnnotation(NonNull.class)
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
