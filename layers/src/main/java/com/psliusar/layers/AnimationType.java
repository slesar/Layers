package com.psliusar.layers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * All possible values of animation type
 */
@IntDef({
        AnimationType.ANIMATION_LOWER_OUT,
        AnimationType.ANIMATION_UPPER_IN,
        AnimationType.ANIMATION_UPPER_OUT,
        AnimationType.ANIMATION_LOWER_IN})
@Retention(RetentionPolicy.SOURCE)
public @interface AnimationType {

    /**
     * Layer goes deeper to the stack, will be covered with a new layer
     */
    int ANIMATION_LOWER_OUT = 0;

    /**
     * Layer appears at the top of the stack
     */
    int ANIMATION_UPPER_IN = 1;

    /**
     * Layer pops out of the stack
     */
    int ANIMATION_UPPER_OUT = 2;

    /**
     * Layer goes up to the top of the stack
     */
    int ANIMATION_LOWER_IN = 3;
}
