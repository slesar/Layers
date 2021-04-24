package com.psliusar.layers

/**
 * All possible values of animation type
 */
enum class AnimationType(val value: Int) {

    /** Layer goes deeper to the stack, will be covered with a new layer */
    ANIMATION_LOWER_OUT(0),

    /** Layer appears at the top of the stack */
    ANIMATION_UPPER_IN(1),

    /** Layer pops out of the stack */
    ANIMATION_UPPER_OUT(2),

    /** Layer goes up to the top of the stack */
    ANIMATION_LOWER_IN(3)
}
