package com.psliusar.layers.sample.screen.stack;

import android.support.annotation.NonNull;

import com.psliusar.layers.Model;
import com.psliusar.layers.Presenter;
import com.psliusar.layers.sample.MainActivity;

public class StackPresenter extends Presenter<Model, StackLayer> {

    public StackPresenter(@NonNull StackLayer layer) {
        super(layer);
    }

    void initViews() {
        getLayer().setStackLevelText(String.format("%s: %s", getLayer().getTitle(), getLayer().getLevel()));
    }

    void nextClick() {
        ((MainActivity) getActivity()).addToStack(
                getLayer().getNextLayerTitle(),
                getLayer().getLevel() + 1,
                getLayer().isNextOpaque()
        );
    }
}
