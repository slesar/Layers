package com.psliusar.layers.binder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Binder {

    private static final Map<Class<?>, ObjectBinder> BINDERS = new ConcurrentHashMap<>();

    private static final ObjectBinder DEFAULT_BINDER = new ObjectBinder() { };

    public static void restore(@NonNull Object target, @NonNull Bundle state) {
        final ObjectBinder binder = getBinder(target);
        if (binder != DEFAULT_BINDER) {
            binder.restore(target, state);
        }
    }

    public static void save(@NonNull Object target, @NonNull Bundle state) {
        final ObjectBinder binder = getBinder(target);
        if (binder != DEFAULT_BINDER) {
            binder.save(target, state);
        }
    }

    public static void bind(@NonNull View.OnClickListener target, @NonNull View view) {
        final ObjectBinder binder = getBinder(target);
        if (binder != DEFAULT_BINDER) {
            binder.bind(target, view);
        }
    }

    public static void unbind(@NonNull View.OnClickListener target) {
        final ObjectBinder binder = getBinder(target);
        if (binder != DEFAULT_BINDER) {
            binder.unbind(target);
        }
    }

    @Nullable
    private static Class<?> getClass(@NonNull String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return null;
    }

    @NonNull
    private static ObjectBinder getBinder(@NonNull Object target) {
        final Class<?> targetClass = target.getClass();
        ObjectBinder binder = null;
        BinderHolder holder = null;
        boolean saveToHolder = false;
        boolean saveToCache = false;
        // Try to get binder from holder
        if (target instanceof BinderHolder) {
            holder = (BinderHolder) target;
            binder = holder.getObjectBinder();
        }
        // Try to get cached binder
        if (binder == null) {
            saveToHolder = true;
            binder = BINDERS.get(targetClass);
        }
        // Load binder
        if (binder == null) {
            saveToCache = true;
            try {
                Class<?> cl = targetClass;
                while (cl != Object.class) {
                    final Class<?> binderClass = getClass(cl.getName() + BinderConstants.BINDER_SUFFIX);
                    if (binderClass != null) {
                        binder = (ObjectBinder) binderClass.newInstance();
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
        // Save binder to holder
        if (holder != null && saveToHolder) {
            holder.setObjectBinder(binder);
        }
        return binder;
    }

    private Binder() {

    }
}
