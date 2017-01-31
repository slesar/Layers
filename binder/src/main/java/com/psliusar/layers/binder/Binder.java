package com.psliusar.layers.binder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Binder {

    private static final Map<Class<?>, LayerBinder> BINDERS = new ConcurrentHashMap<>();

    private static final LayerBinder DEFAULT_BINDER = new LayerBinder() { };

    public static void restore(@NonNull Object target, @NonNull Bundle state) {
        final LayerBinder binder = getBinder(target.getClass());
        if (binder != DEFAULT_BINDER) {
            binder.restore(target, state);
        }
    }

    public static void save(@NonNull Object target, @NonNull Bundle state) {
        final LayerBinder binder = getBinder(target.getClass());
        if (binder != DEFAULT_BINDER) {
            binder.save(target, state);
        }
    }

    public static void bind(@NonNull View.OnClickListener target, @NonNull View view) {
        final LayerBinder binder = getBinder(target.getClass());
        if (binder != DEFAULT_BINDER) {
            binder.bind(target, view);
        }
    }

    public static void unbind(@NonNull View.OnClickListener target) {
        final LayerBinder binder = getBinder(target.getClass());
        if (binder != DEFAULT_BINDER) {
            binder.unbind(target);
        }
    }

    @Nullable
    private static Class<?> getClass(@NonNull String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    private static LayerBinder getBinder(@NonNull Class<?> targetClass) {
        LayerBinder binder = BINDERS.get(targetClass);
        if (binder == null) {
            try {
                Class<?> cl = targetClass;
                while (cl != Object.class) {
                    final Class<?> binderClass = getClass(cl.getCanonicalName() + LayerBinder.BINDER_SUFFIX);
                    if (binderClass != null) {
                        binder = (LayerBinder) binderClass.newInstance();
                        break;
                    }
                    cl = cl.getSuperclass();
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException("Could not instantiate LayerBinder for class " + targetClass.getCanonicalName(), ex);
            }
        }
        if (binder == null) {
            binder = DEFAULT_BINDER;
        }
        BINDERS.put(targetClass, binder);
        return binder;
    }

    private Binder() {

    }
}
