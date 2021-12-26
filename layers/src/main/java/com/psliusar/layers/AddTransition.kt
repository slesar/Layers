package com.psliusar.layers

import java.util.ArrayList

/**
 * Transition adds [Layer] to the stack at the given index. Adds to the top of the stack if
 * the index is omitted.
 */
internal class AddTransition<L : Layer>(
    layers: Layers,
    layerClass: Class<L>
) : Transition<L>(layers, StackEntry(layerClass)) {

    private var lowestVisibleLayer = -1

    override fun onTransition() {
        // Add a new layer first
        stackEntry.inTransition = true
        layers.addStackEntry(stackEntry, index)

        val stackSize = layers.stackSize
        val insertedIndex = if (index == -1) {
            stackSize - 1
        } else {
            index
        }

        lowestVisibleLayer = layers.getLowestVisibleLayer()
        // No animations for invisible changes
        if (lowestVisibleLayer < 0 || insertedIndex < lowestVisibleLayer) return

        // Mark layers for transition
        setTransitionState(lowestVisibleLayer)

        // Animate Layers
        for (i in lowestVisibleLayer until stackSize) {
            val layer = layers.getStackEntryAt(i).layerInstance
                ?: throw IllegalStateException("Layer instance must exist")
            val anim = when {
                i == insertedIndex -> AnimationType.ANIMATION_UPPER_IN // New Layer
                i > insertedIndex -> continue // Do not animate Layers above the inserted one
                else -> AnimationType.ANIMATION_LOWER_OUT // Existing Layers below
            }
            animateLayer(layer, anim)
        }
    }

    override fun onAfterTransition() {
        if (resetTransitionState(lowestVisibleLayer)) {
            layers.ensureViews()
        }
    }

    override fun fastForward(stack: ArrayList<StackEntry>) {
        if (started) return

        if (index == -1) {
            stack.add(stackEntry)
        } else {
            stack.add(index, stackEntry)
        }
    }
}