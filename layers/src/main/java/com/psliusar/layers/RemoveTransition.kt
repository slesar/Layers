package com.psliusar.layers

import android.os.Bundle
import java.util.ArrayList

/**
 * Transition removes [Layer] from the stack.
 */
internal class RemoveTransition<L : Layer> : Transition<L> {

    /** Removes [Layer] from the stack at the given index */
    constructor(layers: Layers, index: Int) : super(layers, index)

    private var lowestVisibleLayer = -1

    override var arguments: Bundle?
        get() = super.arguments
        set(_) {
            throw IllegalArgumentException("Unable to set arguments when removing layer")
        }

    override var name: String?
        get() = super.name
        set(_) {
            throw IllegalArgumentException("Unable to set name when removing layer")
        }

    override fun setInAnimation(outAnimId: Int, inAnimId: Int): Transition<L> {
        throw IllegalArgumentException("Unable to set intro animation when removing layer")
    }

    override fun onTransition() {
        // TODO remove layer with custom index

        stackEntry.inTransition = true

        val stackSize = layers.stackSize
        lowestVisibleLayer = layers.getLowestVisibleLayer()

        if (index >= lowestVisibleLayer) {
            // Mark layers for transition
            setTransitionState(lowestVisibleLayer)

            // Make sure we have all required views in layout
            layers.ensureViews()

            // Animate layers
            for (i in lowestVisibleLayer until stackSize) {
                val layer = layers.getStackEntryAt(i).layerInstance ?: throw IllegalStateException("Layer instance must exist")
                val anim = if (i == index) AnimationType.ANIMATION_UPPER_OUT else AnimationType.ANIMATION_LOWER_IN
                animateLayer(layer, anim)
            }
        }

        stackEntry.valid = false
    }

    override fun onAfterTransition() {
        if (index >= lowestVisibleLayer) {
            resetTransitionState(lowestVisibleLayer)
        }
        layers.removeLayerAt<Layer>(index)
    }

    override fun fastForward(stack: ArrayList<StackEntry>) {
        if (!started) {
            stack.removeAt(index) // size should be checked earlier
        }
    }
}