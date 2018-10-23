package com.psliusar.layers.binder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.Nullable;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD})
public @interface Save {
    @Nullable
    Class stateManager() default void.class;

    @Nullable
    String name() default "";
}
