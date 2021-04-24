package com.psliusar.layers.binder;

import android.os.Bundle;

import com.psliusar.layers.Layer__ObjectBinder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Binder {

    private static final Map<Class<?>, ObjectBinder> BINDERS = new ConcurrentHashMap<>();

    private static final ObjectBinder DEFAULT_BINDER = new Layer__ObjectBinder();

    public static void restore(@NonNull Object target, @NonNull Bundle state) {
        getBinder(target).restore(target, state);
    }

    public static void save(@NonNull Object target, @NonNull Bundle state) {
        getBinder(target).save(target, state);
    }

    @Nullable
    private static <T> Class<T> getClass(@NonNull String className) {
        try {
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return null;
    }

    @NonNull
    private static ObjectBinder getBinder(@NonNull Object target) {
        final Class<?> targetClass = target.getClass();
        // Try to get cached binder
        ObjectBinder binder = BINDERS.get(targetClass);
        boolean saveToCache = false;
        // Load binder
        if (binder == null) {
            saveToCache = true;
            try {
                Class<?> cl = targetClass;
                while (cl != null && cl != Object.class) {
                    final Class<ObjectBinder> binderClass = getClass(cl.getName() + "$$ObjectBinder");//BinderConstants.BINDER_SUFFIX);
                    if (binderClass != null) {
                        binder = binderClass.newInstance();
                        break;
                    }
                    cl = cl.getSuperclass();
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException("Could not instantiate LayerBinder for class " + targetClass.getName(), ex);
            }
        }
        // Use default binder if not found
        if (binder == null) {
            binder = DEFAULT_BINDER;
        }
        // Cache binder
        if (saveToCache) {
            BINDERS.put(targetClass, binder);
        }
        return binder;
    }

    private Binder() {
        // No instances
    }
}
