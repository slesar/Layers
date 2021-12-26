package com.psliusar.layers

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.annotation.LayoutRes
import com.psliusar.layers.animation.SimpleAnimation
import java.util.ArrayList

/**
 * General class that performs transitions between layers.
 */
abstract class Transition<L : Layer> internal constructor(
    internal val layers: Layers,
    internal val stackEntry: StackEntry
) {

    /**
     * Indicates whether the transition has been started.
     */
    internal var started = false
        private set

    private var animatorSet: AnimatorSet? = null
    private var toAnimate: MutableSet<Animator>? = null
    private var prepareLayer: (L.() -> Unit)? = null

    private val animationListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            animatorSet = null
            finish()
        }
    }

    //region Builder arguments

    /**
     * The index of the Layer in stack.
     */
    open var index: Int = -1

    /**
     * Flag that controls whether the animation is enabled for current transition.
     */
    var isAnimationEnabled = true

    /**
     * Custom arguments that will be set to the [Layer] once instantiated.
     */
    open var arguments: Bundle?
        get() = stackEntry.arguments
        set(v) {
            stackEntry.arguments = v
        }

    /**
     * Custom name for the [Layer] which can be used later to work with layers stack.
     */
    open var name: String?
        get() = stackEntry.name
        set(v) {
            stackEntry.name = v
        }

    /**
     * Opaque layer does not allow any visible layers below it in the stack. When the underlying
     * layer should be visible, current layer's flag should be set to `false`.
     */
    open var opaque: Boolean
        get() = stackEntry.layerType == StackEntry.TYPE_OPAQUE
        set(v) {
            stackEntry.layerType = if (v) StackEntry.TYPE_OPAQUE else StackEntry.TYPE_TRANSPARENT
        }

    /**
     * Custom layout resource that could be set to [Layer]. This is useful when the same class can
     * be used with different layouts in some circumstances.
     */
    open var layoutResId: Int
        get() = stackEntry.layoutResId
        set(@LayoutRes v) {
            stackEntry.layoutResId = v
        }

    /**
     * Callback to setup the layer during transition.
     */
    open fun withLayer(block: L.() -> Unit) {
        prepareLayer = block
    }

    /**
     * Direct animation.
     * Note that [Layer] has a callback to specify a custom animation - [Layer.getAnimation].
     *
     * @param outAnimId layer(s) that comes out
     * @param inAnimId layer that comes in
     * @return this transaction
     */
    open fun setInAnimation(@AnimRes outAnimId: Int, @AnimRes inAnimId: Int): Transition<L> {
        val anim = stackEntry.animations.or { IntArray(4).also { stackEntry.animations = it } }
        anim[AnimationType.ANIMATION_LOWER_OUT.value] = outAnimId
        anim[AnimationType.ANIMATION_UPPER_IN.value] = inAnimId
        return this
    }

    /**
     * Reverse animation.
     * Note that [Layer] has a callback to specify a custom animation - [Layer.getAnimation].
     *
     * @param outAnimId layer that pops out from the top of the stack
     * @param inAnimId layer(s) that returns back from the stack
     * @return this transaction
     */
    open fun setOutAnimation(@AnimRes outAnimId: Int, @AnimRes inAnimId: Int): Transition<L> {
        val anim = stackEntry.animations.or { IntArray(4).also { stackEntry.animations = it } }
        anim[AnimationType.ANIMATION_UPPER_OUT.value] = outAnimId
        anim[AnimationType.ANIMATION_LOWER_IN.value] = inAnimId
        return this
    }

    // endregion

    /**
     * Says whether the transition is complete (animation is finished) or not.
     */
    fun isFinished(): Boolean = started && animatorSet == null

    /**
     * Checks if the transition is started and performs animations.
     */
    fun hasAnimations(): Boolean = started && (toAnimate?.size ?: 0) > 0

    /**
     * Starts the transition. If there any animations specified for the transition, they will be
     * started immediately.
     */
    internal fun start() {
        // TODO for specific index and non-empty transition queue - throw exception?
        onBeforeTransition()

        check(!started) { "Current transaction has been already started" }

        stackEntry.instantiateLayer()
        @Suppress("UNCHECKED_CAST")
        prepareLayer?.invoke(stackEntry.layerInstance as L)

        started = true
        onTransition()

        val animate = toAnimate
        if (animate == null) {
            finish()
        } else {
            AnimatorSet().apply {
                animatorSet = this
                addListener(animationListener)
                playTogether(animate)
                start()
            }
        }
    }

    /**
     * Called when the transition is just about to start.
     */
    internal open fun onBeforeTransition() {

    }

    /**
     * A transition itself should be generally happening in this method. This is called right after
     * the transition is started.
     */
    internal abstract fun onTransition()

    /**
     * Any clean-up after transition should happen here.
     */
    internal abstract fun onAfterTransition()

    /**
     * This method is called when the transition should be finished immediately. The final state
     * should be applied if the transition is still in progress.
     */
    internal open fun fastForward(stack: ArrayList<StackEntry>) {
        animatorSet?.end()
    }

    /**
     * Marks the end of the transition and starts next transition in queue in [Layers].
     */
    internal fun finish() {
        onAfterTransition()

        layers.nextTransition()
    }

    /**
     * Creates animation for the layer.
     */
    internal fun animateLayer(layer: Layer, animationType: AnimationType): Animator? {
        val view = layer.view
        if (!isAnimationEnabled || layers.isViewPaused || !layer.isViewInLayout || view == null) {
            return null
        }
        var anim = layer.getAnimation(animationType)
        val animations = stackEntry.animations
        if (anim == null && animations != null && animations[animationType.value] != 0) {
            anim = SimpleAnimation(view, animations[animationType.value])
            anim.setTarget(view)
        }
        if (anim != null) {
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    layer.onAnimationStart(animationType)
                }

                override fun onAnimationEnd(animation: Animator) {
                    layer.onAnimationFinish(animationType)
                }
            })
            toAnimate.or { mutableSetOf<Animator>().also { toAnimate = it } }.add(anim)
        }
        return anim
    }

    /**
     * Marks layers in stack as in transition.
     *
     * @param fromIndex the index from which layers should be marked for transition
     */
    internal fun setTransitionState(fromIndex: Int): Boolean {
        return if (fromIndex >= 0) {
            val stackSize = layers.stackSize
            // Do not touch the lowest one, because it will make getLowestVisibleLayer return
            // different value later.
            for (i in fromIndex + 1 until stackSize) {
                layers.getStackEntryAt(i).inTransition = true
            }
            true
        } else {
            false
        }
    }

    /**
     * Resets transition state in layers stack.
     *
     * @param fromIndex the index from which to reset transition state
     */
    internal fun resetTransitionState(fromIndex: Int): Boolean {
        return if (fromIndex >= 0) {
            val stackSize = layers.stackSize
            for (i in fromIndex until stackSize) {
                layers.getStackEntryAt(i).inTransition = false
            }
            true
        } else {
            false
        }
    }
}