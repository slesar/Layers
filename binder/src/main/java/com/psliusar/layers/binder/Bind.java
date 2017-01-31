package com.psliusar.layers.binder;

import android.support.annotation.IdRes;
import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Bind {
    @IdRes
    int value();

    @IdRes
    int parent() default View.NO_ID;

    boolean clicks() default false;
}
