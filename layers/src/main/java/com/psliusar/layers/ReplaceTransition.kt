package com.psliusar.layers

import java.util.ArrayList

class ReplaceTransition<L : Layer> : Transition<L> {

    constructor(layers: Layers, layerClass: Class<L>) : super(layers, layerClass)

    constructor(layers: Layers, index: Int) : super(layers, index)

    private var lowestVisibleLayer = -1
    private var replaceLayerIndex = -1

    override fun onTransition() {
        // TODO replace layer by custom index

        // Add a new layer first
        stackEntry.inTransition = true
        layers.commitStackEntry(stackEntry)

        val stackSize = layers.stackSize
        lowestVisibleLayer = layers.getLowestVisibleLayer()
        replaceLayerIndex = stackSize - 2
        if (replaceLayerIndex >= 0) {
            layers.getStackEntryAt(replaceLayerIndex).valid = false
        }

        if (replaceLayerIndex >= lowestVisibleLayer) {
            // Mark layers for transition
            setTransitionState(lowestVisibleLayer)

            // Animate layers
            for (i in lowestVisibleLayer until stackSize) {
                val layer = layers.getStackEntryAt(i).layerInstance ?: throw IllegalStateException("Layer instance must exist")
                val anim = if (i == stackSize - 1) AnimationType.ANIMATION_UPPER_IN else AnimationType.ANIMATION_LOWER_OUT
                animateLayer(layer, anim)
            }
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
        if (!applied) {
            stack.add(stackEntry)
        }
        if ((!applied || hasAnimations()) && replaceLayerIndex >= 0) {
            stack.removeAt(replaceLayerIndex)
        }
    }
}