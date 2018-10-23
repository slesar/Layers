package com.psliusar.layers;

import androidx.annotation.NonNull;

public class ReplaceTransition<LAYER extends Layer<?>> extends Transition<LAYER> {

    ReplaceTransition(@NonNull Layers layers, @NonNull Class<LAYER> layerClass) {
        super(layers, layerClass);
    }

    ReplaceTransition(@NonNull Layers layers, int index) {
        super(layers, index);
    }

    @NonNull
    @Override
    protected LAYER performOperation() {
        // TODO index
        for (int i = lowestVisibleLayer; i < initialStackSize; i++) {
            animateLayer(layers.getStackEntryAt(i).layerInstance, AnimationType.ANIMATION_LOWER_OUT);
        }

        // Add a new layer first
        final LAYER layer = (LAYER) layers.commitStackEntry(stackEntry);

        // Then invalidate layer beneath and then remove it (after animation)
        if (initialStackSize > 0) {
            layers.getStackEntryAt(initialStackSize - 1).valid = false;
        }

        animateLayer(layer, AnimationType.ANIMATION_UPPER_IN);

        return layer;
    }

    @Override
    public void finish() {
        super.finish();
        if (initialStackSize > 0) {
            layers.removeLayerAt(initialStackSize - 1);
        }
    }
}
