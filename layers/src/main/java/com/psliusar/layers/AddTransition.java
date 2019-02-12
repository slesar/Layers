package com.psliusar.layers;

import androidx.annotation.NonNull;

public class AddTransition<LAYER extends Layer<?>> extends Transition<LAYER> {

    private int lowestVisibleLayer = -1;

    AddTransition(@NonNull Layers layers, @NonNull Class<LAYER> layerClass) {
        super(layers, layerClass);
    }

    AddTransition(@NonNull Layers layers, int index) {
        super(layers, index);
    }

    @Override
    protected void onTransition() {
        super.onTransition();

        // TODO add layer with custom index

        // Add a new layer first
        stackEntry.inTransition = true;
        layers.commitStackEntry(stackEntry);

        final int stackSize = layers.getStackSize();
        lowestVisibleLayer = layers.getLowestVisibleLayer();

        // Mark layers for transition
        setTransitionState(lowestVisibleLayer);

        if (lowestVisibleLayer >= 0) {
            for (int i = lowestVisibleLayer; i < stackSize; i++) {
                animateLayer(layers.getStackEntryAt(i).layerInstance,
                    i == stackSize - 1 ? AnimationType.ANIMATION_UPPER_IN : AnimationType.ANIMATION_LOWER_OUT);
            }
        }
    }

    @Override
    protected void onAfterTransition() {
        super.onAfterTransition();
        if (resetTransitionState(lowestVisibleLayer)) {
            layers.ensureViews();
        }
    }
}
