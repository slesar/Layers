package com.psliusar.layers.sample.screen.stack;

import android.support.annotation.NonNull;

import com.psliusar.layers.Model;
import com.psliusar.layers.Presenter;
import com.psliusar.layers.sample.MainActivity;

public class StackPresenter extends Presenter<Model> {

    private final StackLayer layer;

    public StackPresenter(@NonNull StackLayer layer) {
        this.layer = layer;
    }

    @Override
    protected void onStart() {
        super.onStart();
        layer.setStackLevelText(String.format("%s: %s", layer.getTitle(), layer.getLevel()));
    }

    void nextClick() {
        ((MainActivity) layer.getActivity()).addToStack(
                layer.getNextLayerTitle(),
                layer.getLevel() + 1,
                layer.isNextOpaque()
        );
    }
}
