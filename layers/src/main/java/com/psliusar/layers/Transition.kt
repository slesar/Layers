package com.psliusar.layers

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.annotation.LayoutRes
import com.psliusar.layers.animation.SimpleAnimation
import java.util.ArrayList
import java.util.HashSet

abstract class Transition<L : Layer> private constructor(
    internal val layers: Layers,
    internal val stackEntry: StackEntry,
    internal val index: Int
) {

    constructor(
        layers: Layers,
        layerClass: Class<L>
    ) : this(
        layers,
        StackEntry(layerClass),//.also { it.instantiateLayer() },
        -1
    )

    constructor(
        layers: Layers,
        index: Int
    ) : this(
        layers,
        layers.getStackEntryAt(index),
        index
    )

    var committed = false
        private set
    internal var applied = false
        private set
    private var animatorSet: AnimatorSet? = null
    private var toAnimate: HashSet<Animator>? = null
    private var prepareLayer: (L.() -> Unit)? = null

    private val animationListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            finish()
        }
    }

    //region Builder arguments

    var animationEnabled = true

    open var arguments: Bundle?
        get() = stackEntry.arguments
        set(v) {
            stackEntry.arguments = v
        }

    open var name: String?
        get() = stackEntry.name
        set(v) {
            stackEntry.name = v
        }

    open var opaque: Boolean
        get() = stackEntry.layerType == StackEntry.TYPE_OPAQUE
        set(v) {
            stackEntry.layerType = if (v) StackEntry.TYPE_OPAQUE else StackEntry.TYPE_TRANSPARENT
        }

    open var layoutResId: Int
        get() = stackEntry.layoutResId
        set(@LayoutRes v) {
            stackEntry.layoutResId = v
        }

    open fun withLayer(block: L.() -> Unit) {
        //@Suppress("UNCHECKED_CAST") XXX
        //block(stackEntry.layerInstance as L)
        prepareLayer = block
    }

    /**
     * Direct animation
     *
     * @param outAnimId layer(s) that comes out
     * @param inAnimId layer that comes in
     * @return this transaction
     */
    open fun setInAnimation(@AnimRes outAnimId: Int, @AnimRes inAnimId: Int): Transition<L> {
        val anim = stackEntry.animations.or { IntArray(4).also { stackEntry.animations = it } }
        anim[AnimationType.ANIMATION_LOWER_OUT] = outAnimId
        anim[AnimationType.ANIMATION_UPPER_IN] = inAnimId
        return this
    }

    /**
     * Reverse animation
     *
     * @param outAnimId layer that pops out from the top of the stack
     * @param inAnimId layer(s) that returns back from the stack
     * @return this transaction
     */
    open fun setOutAnimation(@AnimRes outAnimId: Int, @AnimRes inAnimId: Int): Transition<L> {
        val anim = stackEntry.animations.or { IntArray(4).also { stackEntry.animations = it } }
        anim[AnimationType.ANIMATION_UPPER_OUT] = outAnimId
        anim[AnimationType.ANIMATION_LOWER_IN] = inAnimId
        return this
    }

    // endregion

    fun isFinished(): Boolean = committed && animatorSet == null

    fun hasAnimations(): Boolean = committed && (toAnimate?.size ?: 0) > 0

    fun commit() {
        check(!committed) { "Current transaction has been already committed" }
        committed = true

        stackEntry.instantiateLayer()
        prepareLayer?.invoke(stackEntry.layerInstance as L)
        layers.addTransition(this)

        // TODO for specific index and non-empty transition queue - throw exception?

        onBeforeTransition()
    }

    internal fun apply() {
        applied = true
        onTransition()

        if (toAnimate == null) {
            finish()
        } else {
            AnimatorSet().apply {
                animatorSet = this
                addListener(animationListener)
                playTogether(toAnimate)
                start()
            }
        }
    }

    /**
     * Called when the transition is added to the queue.
     */
    internal open fun onBeforeTransition() {

    }

    /**
     * A transition itself should be generally happening in this method. This is called right after th transition is taken from the queue.
     */
    internal open fun onTransition() {

    }

    /**
     * Any clean-up after transition should happen here.
     */
    internal open fun onAfterTransition() {

    }

    internal abstract fun fastForward(stack: ArrayList<StackEntry>)

    internal fun finish() {
        onAfterTransition()

        layers.nextTransition()
    }

    internal fun animateLayer(layer: Layer, @AnimationType animationType: Int): Animator? {
        val view = layer.view
        if (!animationEnabled || layers.isViewPaused || !layer.isViewInLayout || view == null) {
            return null
        }
        var anim = layer.getAnimation(animationType)
        if (anim == null && (stackEntry.animations?.get(animationType) ?: 0) != 0) {
            anim = SimpleAnimation(view, stackEntry.animations!![animationType])
            anim.setTarget(view)
        }
        if (anim != null) {
            toAnimate.or { HashSet<Animator>().also { toAnimate = it } }.add(anim)
        }
        return anim
    }

    internal fun setTransitionState(fromIndex: Int): Boolean {
        return if (fromIndex >= 0) {
            val stackSize = layers.stackSize
            // do not touch the lowest one, because it will make getLowestVisibleLayer return different value later
            for (i in fromIndex + 1 until stackSize) {
                layers.getStackEntryAt(i).inTransition = true
            }
            true
        } else {
            false
        }
    }

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