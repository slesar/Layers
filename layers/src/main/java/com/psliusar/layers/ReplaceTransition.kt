package com.psliusar.layers

import java.util.ArrayList

/**
 * Transition replaces [Layer] by the given index in the stack. Replaces Layer at the top of the
 * stack if the index is omitted.
 */
internal class ReplaceTransition<L : Layer>(
    layers: Layers,
    layerClass: Class<L>
) : Transition<L>(layers, StackEntry(layerClass)) {

    private var lowestVisibleLayer = -1
    private var replaceLayerIndex = -1

    override fun onTransition() {
        val insertedIndex = if (index == -1) {
            layers.stackSize
        } else {
            index + 1
        }
        replaceLayerIndex = insertedIndex - 1

        // Add a new layer first
        stackEntry.inTransition = true
        layers.addStackEntry(stackEntry, insertedIndex)

        lowestVisibleLayer = layers.getLowestVisibleLayer()
        if (replaceLayerIndex >= 0) {
            layers.getStackEntryAt(replaceLayerIndex).valid = false
        }

        // No animations for invisible changes
        if (lowestVisibleLayer < 0 || insertedIndex < lowestVisibleLayer) return

        // Mark layers for transition
        setTransitionState(lowestVisibleLayer)

        // Animate Layers
        val stackSize = layers.stackSize
        for (i in replaceLayerIndex until stackSize) {
            if (i < 0) continue

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
        if (replaceLayerIndex >= lowestVisibleLayer) {
            resetTransitionState(lowestVisibleLayer)
        }

        if (replaceLayerIndex >= 0) {
            layers.removeLayerAt<Layer>(replaceLayerIndex)
        }
    }

    override fun fastForward(stack: ArrayList<StackEntry>) {
        if (started) return

        if (index == -1) {
            stack.removeLastOrNull()
            stack.add(stackEntry)
        } else {
            stack.removeAt(index)
            stack.add(index, stackEntry)
        }
    }
}