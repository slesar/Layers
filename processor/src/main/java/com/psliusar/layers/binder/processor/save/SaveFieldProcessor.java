package com.psliusar.layers.binder.processor.save;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.binder.Save;
import com.psliusar.layers.binder.processor.BinderClassHolder;
import com.psliusar.layers.binder.processor.FieldProcessor;
import com.psliusar.layers.binder.processor.LayersAnnotationProcessor;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

public class SaveFieldProcessor extends FieldProcessor {

    public static final String SUFFIX_TRACK = "Track";

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

            put(TYPE_TRACK, SUFFIX_TRACK);
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
        boolean needsClassLoader = false;
        if (manager == null) {
            final TypeDescription desc = getTypeDescription(element);
            suffix = desc.suffix;
            field.setNeedsParcelableWrapper(desc.needsParcelableWrapper);
            field.setNeedsSerializableWrapper(desc.needsSerializableWrapper);
            field.setNeedsClassCast(desc.needsClassCast);
            needsClassLoader = desc.needsClassCast;
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

        needsClassLoader |= checkFieldNeedsClassLoader(suffix);
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
            throw new IllegalArgumentException("State manager must implement interface FieldStateManager");
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
            if (isAssignable(ap, typeMirror, TYPE_TRACK)) {
                elementType = TYPE_TRACK;
                desc.needsClassCast = true;
                if (desc.isArray) {
                    // XXX Can be array?
                    //elementType += "[]";
                    //desc.needsParcelableWrapper = true;
                }
            } else if (isAssignable(ap, typeMirror, TYPE_PARCELABLE)) {
                // Detect Parcelable
                elementType = TYPE_PARCELABLE;
                if (desc.isArray) {
                    elementType += "[]";
                    desc.needsParcelableWrapper = true;
                }
            } else if (isAssignable(ap, typeMirror, TYPE_SERIALIZABLE)) {
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
