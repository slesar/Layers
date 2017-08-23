package com.psliusar.layers;

import android.os.Bundle;
import android.support.annotation.NonNull;

public class RemoveTransition<LAYER extends Layer<?>> extends Transition<LAYER> {

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
    protected int getMinTransparentLayersCount() {
        return index < lowestVisibleLayer ? 0 : initialStackSize - index;
    }

    @NonNull
    @Override
    protected LAYER performOperation() {
        if (index < 0 || index >= initialStackSize) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + initialStackSize);
        }

        for (int i = lowestVisibleLayer; i < initialStackSize; i++) {
            animateLayer(layers.getStackEntryAt(i).layerInstance,
                    i == index ? AnimationType.ANIMATION_UPPER_OUT : AnimationType.ANIMATION_LOWER_IN);
        }

        layers.getStackEntryAt(index).valid = false;
        return layers.removeLayerAt(index);
    }
}
