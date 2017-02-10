package com.psliusar.layers.sample.screen.stack;

import com.psliusar.layers.Model;
import com.psliusar.layers.Presenter;
import com.psliusar.layers.sample.MainActivity;

public class StackPresenter extends Presenter<Model, StackLayer> {

    void initViews() {
        getLayer().setStackLevelText(String.format("%s: %s", getLayer().getTitle(), getLayer().getLevel()));
    }

    void nextClick() {
        ((MainActivity) getHost().getActivity()).addToStack(
                getLayer().getNextLayerTitle(),
                getLayer().getLevel() + 1,
                getLayer().isNextOpaque()
        );
    }
}
