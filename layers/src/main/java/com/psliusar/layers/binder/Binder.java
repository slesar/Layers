package com.psliusar.layers.binder;

import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.psliusar.layers.Layer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Binder {

    private static final Map<Class<?>, ViewBinder> BINDERS = new ConcurrentHashMap<>();
    private static final String BINDER_SUFFIX = "$$ViewBinder";

    public static void bind(@NonNull View.OnClickListener target, @NonNull View view) {
        getBinder(target.getClass()).bind(target, view);
    }

    public static void unbind(@NonNull View.OnClickListener target) {
        getBinder(target.getClass()).unbind(target);
    }

    private static ViewBinder getBinder(@NonNull Class<?> targetClass) {
        ViewBinder binder = BINDERS.get(targetClass);
        if (binder == null) {
            try {
                final Class<?> binderClass = Class.forName(targetClass.getName() + BINDER_SUFFIX);
                binder = (ViewBinder) binderClass.newInstance();
                BINDERS.put(targetClass, binder);
            } catch (Exception ignored) {

            }
        }
        if (binder == null) {
            // TODO throw new Exception
        }
        return binder;
    }

    private List<Field> boundFields;

    public Binder() {

    }

    @Nullable
    public List<Field> bindViews(@NonNull View.OnClickListener target, @NonNull View container) {
        final ArrayList<Field> fieldsList = new ArrayList<>();
        Class<?> targetClass = target.getClass();
        while (targetClass != null && targetClass != Object.class) {
            for (Field field : targetClass.getDeclaredFields()) {
                // TODO check for synthetic, static, final - throw if any

                final Bind bind = field.getAnnotation(Bind.class);
                if (bind == null) {
                    continue;
                }

                final Class<?> type = field.getType();
                final View view = findViewOrThrow(type, container, bind.value(), bind.parent());

                bindViewToField(target, field, type, view);
                if (bind.clicks()) {
                    view.setOnClickListener(target);
                }

                fieldsList.add(field);
            }

            targetClass = targetClass.getSuperclass();
        }
        boundFields = fieldsList.size() > 0 ? fieldsList : null;
        return boundFields;
    }

    private static void bindViewToField(@NonNull Object target, @NonNull Field field, @NonNull Class<?> type, @NonNull View value) {
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(target, type.cast(value));
        } catch (SecurityException ex) {
            throw new IllegalArgumentException("Failed to bind view", ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("Could not set field value", ex);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("Cannot assign value to a field", ex);
        }
    }

    @NonNull
    public static View findViewOrThrow(@NonNull Class<?> type, @NonNull View container, @IdRes int viewResId, @IdRes int parentResId) {
        if (!View.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Could not bind field not of type View");
        }

        if (parentResId != View.NO_ID) {
            container = findViewOrThrow(container, parentResId, "Parent view");
        }

        return findViewOrThrow(container, viewResId, "View");
    }

    private static View findViewOrThrow(@NonNull View container, int viewResId, String messagePrefix) {
        final View view = container.findViewById(viewResId);
        if (view == null) {
            final String viewResName = resolveResourceName(container.getResources(), viewResId);
            throw new IllegalArgumentException(messagePrefix + " with ID 0x" + Integer.toHexString(viewResId)
                    + (viewResName != null ? " (" + viewResName + ")" : "") + " not found!");
        }
        return view;
    }

    @Nullable
    private static String resolveResourceName(@NonNull Resources res, int resId) {
        try {
            return res.getResourceEntryName(resId);
        } catch (Resources.NotFoundException ex) {
            return null;
        }
    }

    public void unbindViews(@NonNull Object holder) {
        if (boundFields == null) {
            return;
        }
        try {
            for (Field field : boundFields) {
                field.set(holder, null);
            }
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("Could not reset field value", ex);
        }
        boundFields = null;
    }
}
