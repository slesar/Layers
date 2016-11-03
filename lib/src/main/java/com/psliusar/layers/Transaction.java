package com.psliusar.layers;

import android.os.Bundle;
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

    public LAYER commit() {
        switch (action) {
            case ACTION_ADD:
                return layers.add(layerClass, arguments, name, opaque);
            case ACTION_REPLACE:
                return layers.replace(layerClass, arguments, name, opaque);
            default:
                // TODO
                throw new IllegalArgumentException("");
        }
    }
}
