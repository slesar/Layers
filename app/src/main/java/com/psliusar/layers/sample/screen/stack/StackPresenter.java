package com.psliusar.layers.sample.screen.stack;

import android.os.Bundle;

import com.psliusar.layers.Model;
import com.psliusar.layers.Presenter;
import com.psliusar.layers.sample.MainActivity;

public class StackPresenter extends Presenter<Model, StackLayer> {

    void initViews() {
        final Bundle args = getLayer().getArguments();
        getLayer().setStackLevelText(String.format("%s: %s",
                args.getCharSequence(StackLayer.ARGS_TITLE), args.getInt(StackLayer.ARGS_LEVEL)));
    }

    void nextClick() {
        ((MainActivity) getHost().getActivity()).addToStack(
                getLayer().getNextLayerTitle(),
                getLayer().getArguments().getInt(StackLayer.ARGS_LEVEL) + 1,
                getLayer().isNextOpaque()
        );
    }
}
