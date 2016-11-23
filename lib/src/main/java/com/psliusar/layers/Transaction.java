package com.psliusar.layers;

import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.NonNull;

public class Transaction<LAYER extends Layer<?>> {

    static final int ACTION_ADD = 1;
    static final int ACTION_REPLACE = 2;

    private final Layers layers;
    private final Class<LAYER> layerClass;
    private final int action;
    private Bundle arguments;
    private String name;
    private boolean opaque = true;

    Transaction(@NonNull Layers layers, @NonNull Class<LAYER> layerClass, int action) {
        this.layers = layers;
        this.layerClass = layerClass;
        this.action = action;
    }

    public Transaction<LAYER> setArguments(@NonNull Bundle arguments) {
        this.arguments = arguments;
        return this;
    }

    public Transaction<LAYER> setName(@NonNull String name) {
        this.name = name;
        return this;
    }

    public Transaction<LAYER> setOpaque(boolean opaque) {
        this.opaque = opaque;
        return this;
    }

    /**
     * Direct animation
     *
     * @param outAnimId layer(s) that comes out
     * @param inAnimId layer that comes in
     * @return this transaction
     */
    public Transaction<LAYER> setInAnimations(@AnimRes int outAnimId, @AnimRes int inAnimId) {
        //
        return this;
    }

    /**
     * Reverse animation
     *
     * @param inAnimId layer(s) that returns back from the stack
     * @param outAnimId layer that pops out from the top of the stack
     * @return this transaction
     */
    public Transaction<LAYER> setOutAnimations(@AnimRes int inAnimId, @AnimRes int outAnimId) {
        //
        return this;
    }

    public LAYER commit() {
        final LAYER layer;
        switch (action) {
            case ACTION_ADD:
                layer = layers.add(layerClass, arguments, name, opaque);
                break;
            case ACTION_REPLACE:
                layer = layers.replace(layerClass, arguments, name, opaque);
                break;
            default:
                throw new IllegalArgumentException("Invalid action ID: " + action);
        }
        return layer;
    }
}
