package com.psliusar.layers;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class ReplaceTransition<LAYER extends Layer<?>> extends Transition<LAYER> {

    private int lowestVisibleLayer = -1;
    private int replaceLayerIndex = -1;

    ReplaceTransition(@NonNull Layers layers, @NonNull Class<LAYER> layerClass) {
        super(layers, layerClass);
    }

    ReplaceTransition(@NonNull Layers layers, int index) {
        super(layers, index);
    }

    @Override
    protected void onTransition() {
        super.onTransition();

        // TODO replace layer by custom index

        // Add a new layer first
        stackEntry.inTransition = true;
        layers.commitStackEntry(stackEntry);

        final int stackSize = layers.getStackSize();
        lowestVisibleLayer = layers.getLowestVisibleLayer();
        replaceLayerIndex = stackSize - 2;
        if (replaceLayerIndex >= 0) {
            layers.getStackEntryAt(replaceLayerIndex).valid = false;
        }

        if (replaceLayerIndex >= lowestVisibleLayer) {
            // Mark layers for transition
            setTransitionState(lowestVisibleLayer);

            // Animate layers
            for (int i = lowestVisibleLayer; i < stackSize; i++) {
                animateLayer(layers.getStackEntryAt(i).layerInstance,
                    i == stackSize - 1 ? AnimationType.ANIMATION_UPPER_IN : AnimationType.ANIMATION_LOWER_OUT);
            }
        }
    }

    @Override
    protected void onAfterTransition() {
        super.onAfterTransition();

        if (replaceLayerIndex >= lowestVisibleLayer) {
            resetTransitionState(lowestVisibleLayer);
        }

        if (replaceLayerIndex >= 0) {
            layers.removeLayerAt(replaceLayerIndex);
        }
    }

    @Override
    protected void fastForward(@NonNull ArrayList<StackEntry> stack) {
        if (!isApplied()) {
            stack.add(stackEntry);
        }
        if (!isApplied() || hasAnimations()) {
            if (replaceLayerIndex >= 0) {
                stack.remove(replaceLayerIndex);
            }
        }
    }
}
