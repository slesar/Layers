package com.psliusar.layers

import java.util.ArrayList

/**
 * Transition adds [Layer] to the stack.
 */
internal class AddTransition<L : Layer> : Transition<L> {

    /** Adds [Layer] to top of the stack */
    constructor(layers: Layers, layerClass: Class<L>) : super(layers, layerClass)

    /** Adds [Layer] at the given index to the stack */
    constructor(layers: Layers, index: Int) : super(layers, index)

    private var lowestVisibleLayer = -1

    override fun onTransition() {
        // TODO add layer with custom index

        // Add a new layer first
        stackEntry.inTransition = true
        layers.commitStackEntry(stackEntry)

        val stackSize = layers.stackSize
        lowestVisibleLayer = layers.getLowestVisibleLayer()

        // Mark layers for transition
        setTransitionState(lowestVisibleLayer)

        if (lowestVisibleLayer >= 0) {
            for (i in lowestVisibleLayer until stackSize) {
                val layer = layers.getStackEntryAt(i).layerInstance
                    ?: throw IllegalStateException("Layer instance must exist")
                val anim = if (i == stackSize - 1)
                    AnimationType.ANIMATION_UPPER_IN else AnimationType.ANIMATION_LOWER_OUT
                animateLayer(layer, anim)
            }
        }
    }

    override fun onAfterTransition() {
        if (resetTransitionState(lowestVisibleLayer)) {
            layers.ensureViews()
        }
    }

    override fun fastForward(stack: ArrayList<StackEntry>) {
        if (!started) {
            stack.add(stackEntry)
        }
    }
}