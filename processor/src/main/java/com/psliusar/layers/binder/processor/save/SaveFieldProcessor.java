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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
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

        final String manager = getManagerName(annotation);
        final String suffix;
        if (manager == null) {
            suffix = getTypeSuffix(element);
        } else {
            suffix = "";
        }

        final String customName = annotation.name();
        final String key;
        if ("".equals(customName)) {
            key = elementNameToSnakeCase(fieldName);
        } else {
            key = customName;
        }

        boolean needsClassLoader = checkFieldNeedsClassLoader(suffix);

        holder.addSaveField(
                fieldName,
                fieldType,
                manager,
                key,
                suffix,
                needsClassLoader
        );
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

        if (!isSubtypeOfType(elements, ap.getTypeUtils(), typeMirror, "com.psliusar.layers.binder.FieldStateManager")) {
            throw new IllegalArgumentException("StateManager must implement interface FieldStateManager");
        }

        return typeMirror.toString();
    }

    private boolean isSubtypeOfType(
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

    private boolean checkFieldNeedsClassLoader(@NonNull String typeSuffix) {
        return "".equals(typeSuffix)
                || typeSuffix.contains("Parcelable")
                || typeSuffix.contains("Serializable")
                || typeSuffix.contains("Bundle")
                || !PREDEFINED.containsValue(typeSuffix);
    }

    private boolean isAssignable(@NonNull String type, @NonNull String target) {
        final LayersAnnotationProcessor ap = getAnnotationProcessor();
        final Elements elements = ap.getElementUtils();
        return ap.getTypeUtils()
                .isAssignable(
                        elements.getTypeElement(type).asType(),
                        elements.getTypeElement(target).asType()
                );
    }

    private boolean isAssignable(@NonNull TypeMirror type, @NonNull String target) {
        return isAssignable(type.toString(), target);
    }

    @NonNull
    private String getTypeSuffix(@NonNull Element element) {
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
            final String typeArgument = matcher.group(1);
            if (!isAssignable(typeArgument, TYPE_PARCELABLE)) {
                throw new IllegalArgumentException("Type " + element + " must be a subclass of Parcelable. Or you can define custom FieldStateManager.");
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
                    && !isAssignable(typeArgument, TYPE_PARCELABLE)) {
                throw new IllegalArgumentException("Type " + element + " must be the String, Integer, CharSequence or subclass of Parcelable." +
                        " Or you can define custom FieldStateManager.");
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
            if (isAssignable(typeMirror, TYPE_PARCELABLE)) {
                elementType = TYPE_PARCELABLE;
                if (isArray) {
                    // TODO wrapper method for copying target array
                }
            } else if (isAssignable(typeMirror, TYPE_SERIALIZABLE)) {
                // Detect Serializable
                elementType = TYPE_SERIALIZABLE;
                if (isArray) {
                    // TODO wrapper method for copying target array
                }
            }

            // Try again, we might find something new
            predefined = PREDEFINED.get(elementType);
        }

        if (predefined == null) {
            throw new IllegalArgumentException("Can't determine the type of " + element + ". You have to define custom FieldStateManager.");
        } else if (isArray && !canBeArray) {
            throw new IllegalArgumentException("Type " + element + " can't be defined as array. You have to define custom FieldStateManager.");
        }
        return predefined;
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

        for (SaveField field : fields) {
            final FieldSpec manager = field.getManagerField();
            if (manager == null) {
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
}
