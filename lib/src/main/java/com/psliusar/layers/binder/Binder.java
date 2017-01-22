package com.psliusar.layers.binder;

import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Binder {

    private List<Field> boundFields;

    public Binder() {

    }

    @Nullable
    public List<Field> bindViews(@NonNull View.OnClickListener layer, @NonNull View container) {
        final ArrayList<Field> fieldsList = new ArrayList<>();
        Class<?> targetClass = layer.getClass();
        while (targetClass != null && targetClass != Object.class) {
            for (Field field : targetClass.getDeclaredFields()) {
                // TODO check for synthetic, static, final - throw if any

                final Bind bind = field.getAnnotation(Bind.class);
                if (bind == null) {
                    continue;
                }

                final View view = bindViewToField(layer, container, field, bind.value(), bind.parent());
                if (bind.clicks()) {
                    view.setOnClickListener(layer);
                }

                fieldsList.add(field);
            }

            targetClass = targetClass.getSuperclass();
        }
        boundFields = fieldsList.size() > 0 ? fieldsList : null;
        return boundFields;
    }

    @NonNull
    private View bindViewToField(@NonNull Object target, @NonNull View container, @NonNull Field field, @IdRes int viewResId, @IdRes int parentResId) {
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            final Class<?> type = field.getType();
            if (View.class.isAssignableFrom(type)) {
                final View view;
                if (parentResId != View.NO_ID) {
                    container = findViewOrThrow(container, parentResId, "Parent view");
                }
                view = findViewOrThrow(container, viewResId, "View");
                field.set(target, type.cast(view));
                return view;
            } else {
                throw new IllegalArgumentException("Could not bind field not of type View");
            }
        } catch (SecurityException ex) {
            throw new IllegalArgumentException("Failed to bind view", ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("Could not set field value", ex);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("Cannot assign value to a field", ex);
        }
    }

    private View findViewOrThrow(@NonNull View container, int viewResId, String messagePrefix) {
        final View view = container.findViewById(viewResId);
        if (view == null) {
            final String viewResName = resolveResourceName(container.getResources(), viewResId);
            throw new IllegalArgumentException(messagePrefix + " with ID 0x" + Integer.toHexString(viewResId)
                    + (viewResName != null ? " (" + viewResName + ")" : "") + " not found!");
        }
        return view;
    }

    @Nullable
    private String resolveResourceName(@NonNull Resources res, int resId) {
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
