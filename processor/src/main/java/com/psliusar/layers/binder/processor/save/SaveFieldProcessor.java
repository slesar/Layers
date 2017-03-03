package com.psliusar.layers.binder.processor.save;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.binder.Save;
import com.psliusar.layers.binder.processor.BinderClassHolder;
import com.psliusar.layers.binder.processor.FieldProcessor;
import com.psliusar.layers.binder.processor.LayersAnnotationProcessor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

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
    private static final String TYPE_TRACK = "com.psliusar.layers.track.Track";

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
            put("java.io.Serializable[]", "SerializableArray");

            put(TYPE_TRACK, "Track");
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
        final BinderClassHolder holder = getClassHolderForField(element);

        final Save annotation = element.getAnnotation(Save.class);
        final String fieldName = element.getSimpleName().toString();
        final String fieldType = element.asType().toString();

        final SaveField field = holder.addSaveField(fieldName, fieldType);

        final String manager = getManagerName(annotation);
        field.setManager(manager);

        final String suffix;
        if (manager == null) {
            final TypeDescription desc = getTypeDescription(element);
            suffix = desc.suffix;
            field.setNeedsParcelableWrapper(desc.needsParcelableWrapper);
            field.setNeedsSerializableWrapper(desc.needsSerializableWrapper);
            field.setNeedsClassCast(desc.needsClassCast);
        } else {
            suffix = "";
        }
        field.setMethodSuffix(suffix);

        final String customName = annotation.name();
        final String key;
        if ("".equals(customName)) {
            key = elementNameToSnakeCase(fieldName);
        } else {
            key = customName;
        }
        field.setKey(key);

        boolean needsClassLoader = checkFieldNeedsClassLoader(suffix);
        field.setNeedsClassLoader(needsClassLoader);
    }

    @Nullable
    private String getManagerName(@NonNull Save annotation) {
        final LayersAnnotationProcessor ap = getAnnotationProcessor();
        final Elements elements = ap.getElementUtils();
        TypeMirror typeMirror;
        try {
            typeMirror = elements.getTypeElement(annotation.stateManager().getCanonicalName()).asType();
        } catch (MirroredTypeException ex) {
            typeMirror = ex.getTypeMirror();
        }

        if (typeMirror == null || "void".equals(typeMirror.toString())) {
            return null;
        }

        if (!ensureSubtypeOfType(elements, ap.getTypeUtils(), typeMirror, "com.psliusar.layers.binder.FieldStateManager")) {
            throw new IllegalArgumentException("StateManager must implement interface FieldStateManager");
        }

        return typeMirror.toString();
    }

    private boolean checkFieldNeedsClassLoader(@NonNull String typeSuffix) {
        return "".equals(typeSuffix)
                || typeSuffix.contains("Parcelable")
                || typeSuffix.contains("Serializable")
                || typeSuffix.contains("Bundle")
                || !PREDEFINED.containsValue(typeSuffix);
    }

    @NonNull
    private TypeDescription getTypeDescription(@NonNull Element element) {
        final LayersAnnotationProcessor ap = getAnnotationProcessor();
        TypeMirror typeMirror = element.asType();
        String elementType = typeMirror.toString();
        final TypeDescription desc = new TypeDescription();
        desc.canBeArray = true;

        // Detect Bundle
        if (elementType.equals(TYPE_BUNDLE)) {
            desc.canBeArray = false;
        }

        // Detect SparseArray<Parcelable>
        Matcher matcher = PATTERN_SPARSE_ARRAY.matcher(elementType);
        if (matcher.matches()) {
            final String typeArgument = matcher.group(1);
            if (!isAssignable(ap, typeArgument, TYPE_PARCELABLE)) {
                throw new IllegalArgumentException("Type " + element + " must be a subclass of Parcelable. Or you can define custom FieldStateManager.");
            }
            desc.canBeArray = false;
        }

        // Detect ArrayList<String | Integer | CharSequence | Parcelable>
        matcher = PATTERN_ARRAY_LIST.matcher(elementType);
        if (matcher.matches()) {
            final String typeArgument = matcher.group(1);
            if (!TYPE_STRING.equals(typeArgument)
                    && !TYPE_INTEGER.equals(typeArgument)
                    && !TYPE_CHAR_SEQUENCE.equals(typeArgument)
                    && !isAssignable(ap, typeArgument, TYPE_PARCELABLE)) {
                throw new IllegalArgumentException("Type " + element + " must be the String, Integer, CharSequence or subclass of Parcelable." +
                        " Or you can define custom FieldStateManager.");
            }
            desc.canBeArray = false;
        }

        // Detect arrays
        if (typeMirror.getKind() == TypeKind.ARRAY) {
            elementType += "[]";
            desc.isArray = true;
            ArrayType arrayType = (ArrayType) typeMirror;
            typeMirror = arrayType.getComponentType();
        }

        // Search for predefined type
        desc.suffix = PREDEFINED.get(elementType);

        if (desc.suffix == null) {
            // Detect Tracks
            // XXX This code smells
            if (findParentType((TypeElement) ap.getTypeUtils().asElement(typeMirror), ap.getElementUtils().getTypeElement(TYPE_TRACK))) {
            //if (isAssignable(ap, typeMirror.toString(), TYPE_CHARGER)) {
            //if (ap.getTypeUtils().isSubtype(ap.getElementUtils().getTypeElement(TYPE_CHARGER).asType(), typeMirror)) {
                elementType = TYPE_TRACK;
                desc.needsClassCast = true;
                if (desc.isArray) {
                    // XXX Can be array?
                    //elementType += "[]";
                    //desc.needsParcelableWrapper = true;
                }
            } else if (isAssignable(ap, typeMirror.toString(), TYPE_PARCELABLE)) {
                // Detect Parcelable
                elementType = TYPE_PARCELABLE;
                if (desc.isArray) {
                    elementType += "[]";
                    desc.needsParcelableWrapper = true;
                }
            } else if (isAssignable(ap, typeMirror.toString(), TYPE_SERIALIZABLE)) {
                // Detect Serializable
                elementType = TYPE_SERIALIZABLE;
                if (desc.isArray) {
                    elementType += "[]";
                    desc.needsSerializableWrapper = true;
                }
            }

            // Try again, we might find something new
            desc.suffix = PREDEFINED.get(elementType);
        }

        if (desc.suffix == null) {
            throw new IllegalArgumentException("Can't determine the type of " + element + ". You have to define custom FieldStateManager.");
        } else if (desc.isArray && !desc.canBeArray) {
            throw new IllegalArgumentException("Type " + element + " can't be defined as array. You have to define custom FieldStateManager.");
        }
        return desc;
    }

    private boolean findParentType(@NonNull TypeElement typeElement, @NonNull TypeElement parent) {
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
    public static List<FieldSpec> getFields(@NonNull List<SaveField> fields) {
        final ArrayList<FieldSpec> specs = new ArrayList<>();
        final HashMap<String, FieldSpec> managers = new HashMap<>();
        for (SaveField field : fields) {
            final String manager = field.getManager();
            if (manager == null) {
                continue;
            }
            FieldSpec managerField = managers.get(manager);
            if (managerField == null) {
                final String managerFieldName = typeNameToFieldName(manager);
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

        // -> super.restore(object, state);
        builder.addStatement(
                "super.restore($L, $L)",
                METHOD_PARAM_OBJECT,
                METHOD_PARAM_STATE
        );

        // Loop through fields and find out whether they need to define class loader or not
        for (SaveField field : fields) {
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
                getKeyPrefix(className)
        );

        // TODO Needs optimization
        for (SaveField field : fields) {
            final FieldSpec manager = field.getManagerField();
            if (manager == null) {
                if (field.needsParcelableWrapper()) {
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
                    if (field.needsClassCast()) {
                        // -> target.field = (FieldClass) getValue(key, state);
                        builder.addStatement(
                                "$L.$L = ($T) get$L($L + $S, $L)",
                                METHOD_VAR_TARGET,
                                field.getFieldName(),
                                ClassName.bestGuess(field.getFieldType()),
                                field.getMethodSuffix(),
                                METHOD_VAR_KEY_PREFIX,
                                field.getKey(),
                                METHOD_PARAM_STATE
                        );
                    } else {
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
            } else {
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
            }
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

        // -> super.save(object, state);
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

        // -> final String keyPrefix = "SUBCLASS_NAME$$";
        builder.addStatement(
                "final String $L = $S",
                METHOD_VAR_KEY_PREFIX,
                getKeyPrefix(className)
        );

        // TODO needs optimization
        for (SaveField field : fields) {
            final FieldSpec manager = field.getManagerField();
            if (manager == null) {
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
    private static String getKeyPrefix(@NonNull String className) {
        return elementNameToSnakeCase(className) + "$$";
    }

    private static class TypeDescription {

        String suffix;
        boolean isArray;
        boolean canBeArray;
        boolean needsParcelableWrapper;
        boolean needsSerializableWrapper;
        boolean needsClassCast;

        TypeDescription() {

        }
    }
}
