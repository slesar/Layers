package com.psliusar.layers.binder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD})
public @interface Bind {
    @IdRes
    int value();

    @IdRes
    int parent() default BinderConstants.NO_ID;

    boolean clicks() default false;

    @Nullable
    Class bindManager() default void.class;
}
