package com.psliusar.layers.binder;

import android.support.annotation.IdRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD})
public @interface Bind {
    @IdRes
    int value();

    @IdRes
    int parent() default BinderConstants.NO_ID;

    boolean clicks() default false;
}
