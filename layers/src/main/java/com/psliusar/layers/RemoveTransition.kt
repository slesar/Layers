package com.psliusar.layers

import android.os.Bundle
import java.util.ArrayList

/**
 * Transition removes [Layer] from the stack at the given index. Removes from the top of the stack
 * if the index is omitted.
 */
internal class RemoveTransition<L : Layer>(
    layers: Layers,
    index: Int
) : Transition<L>(layers, layers.getStackEntryAt(index)) {

    private var lowestVisibleLayer = -1

    init {
        this.index = index
    }

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
        stackEntry.inTransition = true

        val stackSize = layers.stackSize
        lowestVisibleLayer = layers.getLowestVisibleLayer()
        stackEntry.valid = false

        if (index < lowestVisibleLayer) return

        // Mark layers for transition
        setTransitionState(lowestVisibleLayer)

        // Make sure we have all required views in layout for animation
        layers.ensureViews()

        // Animate layers
        for (i in lowestVisibleLayer until stackSize) {
            val layer = layers.getStackEntryAt(i).layerInstance
                ?: throw IllegalStateException("Layer instance must exist")
            val anim = when {
                i == index -> AnimationType.ANIMATION_UPPER_OUT
                i < index -> AnimationType.ANIMATION_LOWER_IN
                else -> continue
            }
            animateLayer(layer, anim)
        }
    }

    override fun onAfterTransition() {
        if (index >= lowestVisibleLayer) {
            resetTransitionState(lowestVisibleLayer)
        }
        layers.removeLayerAt<Layer>(index)
    }

    override fun fastForward(stack: ArrayList<StackEntry>) {
        if (started) return

        stack.removeAt(index) // size should be checked earlier
    }
}