package com.psliusar.layers;

import android.os.Bundle;

import androidx.annotation.NonNull;

public class RemoveTransition<LAYER extends Layer<?>> extends Transition<LAYER> {

    private int lowestVisibleLayer = -1;

    RemoveTransition(@NonNull Layers layers, int index) {
        super(layers, index);
    }

    @NonNull
    @Override
    public Transition<LAYER> setArguments(@NonNull Bundle arguments) {
        throw new IllegalArgumentException("Unable to set arguments when removing layer");
    }

    @NonNull
    @Override
    public Transition<LAYER> setName(@NonNull String name) {
        throw new IllegalArgumentException("Unable to set name when removing layer");
    }

    @NonNull
    @Override
    public Transition<LAYER> setInAnimation(int outAnimId, int inAnimId) {
        throw new IllegalArgumentException("Unable to set intro animation when removing layer");
    }

    @Override
    protected void onTransition() {
        super.onTransition();

        stackEntry.inTransition = true;

        final int stackSize = layers.getStackSize();
        lowestVisibleLayer = layers.getLowestVisibleLayer();

        // Mark layers for transition
        setTransitionState(lowestVisibleLayer);

        // Make sure we have all required views in layout
        layers.ensureViews();

        // Animate layers
        for (int i = lowestVisibleLayer; i < stackSize; i++) {
            animateLayer(layers.getStackEntryAt(i).layerInstance,
                i == index ? AnimationType.ANIMATION_UPPER_OUT : AnimationType.ANIMATION_LOWER_IN);
        }
    }

    @Override
    protected void onAfterTransition() {
        super.onAfterTransition();

        stackEntry.valid = false;
        resetTransitionState(lowestVisibleLayer);
        layers.removeLayerAt(index);
    }
}
