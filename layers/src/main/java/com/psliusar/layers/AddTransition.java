package com.psliusar.layers;

import androidx.annotation.NonNull;

public class AddTransition<LAYER extends Layer<?>> extends Transition<LAYER> {

    AddTransition(@NonNull Layers layers, @NonNull Class<LAYER> layerClass) {
        super(layers, layerClass);
    }

    AddTransition(@NonNull Layers layers, int index) {
        super(layers, index);
    }

    @NonNull
    @Override
    protected LAYER performOperation() {
        // TODO index
        for (int i = lowestVisibleLayer; i < initialStackSize; i++) {
            animateLayer(layers.getStackEntryAt(i).layerInstance, AnimationType.ANIMATION_LOWER_OUT);
        }

        final LAYER layer = (LAYER) layers.commitStackEntry(stackEntry);
        animateLayer(layer, AnimationType.ANIMATION_UPPER_IN);

        return layer;
    }
}
