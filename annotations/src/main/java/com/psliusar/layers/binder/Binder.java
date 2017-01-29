package com.psliusar.layers.binder;

import android.support.annotation.NonNull;
import android.view.View;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Binder {

    private static final Map<Class<?>, ViewBinder> BINDERS = new ConcurrentHashMap<>();

    public static void bind(@NonNull View.OnClickListener target, @NonNull View view) {
        getBinder(target.getClass()).bind(target, view);
    }

    public static void unbind(@NonNull View.OnClickListener target) {
        getBinder(target.getClass()).unbind(target);
    }

    @NonNull
    private static ViewBinder getBinder(@NonNull Class<?> targetClass) {
        ViewBinder binder = BINDERS.get(targetClass);
        if (binder == null) {
            try {
                final Class<?> binderClass = Class.forName(targetClass.getCanonicalName() + ViewBinder.BINDER_SUFFIX);
                binder = (ViewBinder) binderClass.newInstance();
                BINDERS.put(targetClass, binder);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Could not find ViewBinder class for " + targetClass.getCanonicalName(), ex);
            }
        }
        return binder;
    }

    private Binder() {

    }
}
