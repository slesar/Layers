package com.psliusar.layers

import androidx.annotation.IntDef

/**
 * All possible values of animation type
 */
@IntDef(
    AnimationType.ANIMATION_LOWER_OUT,
    AnimationType.ANIMATION_UPPER_IN,
    AnimationType.ANIMATION_UPPER_OUT,
    AnimationType.ANIMATION_LOWER_IN
)
@Retention(AnnotationRetention.SOURCE)
annotation class AnimationType {
    companion object {

        /**
         * Layer goes deeper to the stack, will be covered with a new layer
         */
        const val ANIMATION_LOWER_OUT = 0

        /**
         * Layer appears at the top of the stack
         */
        const val ANIMATION_UPPER_IN = 1

        /**
         * Layer pops out of the stack
         */
        const val ANIMATION_UPPER_OUT = 2

        /**
         * Layer goes up to the top of the stack
         */
        const val ANIMATION_LOWER_IN = 3
    }
}
