package com.psliusar.layers.sample.screen.stack;

import android.support.annotation.NonNull;

import com.psliusar.layers.Model;
import com.psliusar.layers.ViewModel;
import com.psliusar.layers.sample.MainActivity;
import com.psliusar.layers.sample.screen.listener.ListenerModel;

public class StackViewModel extends ViewModel<Model> {

    public StackViewModel() {
        super(null);
    }

    void getStackLevelText(@NonNull StackLayer layer, @NonNull ListenerModel.Updatable<String> updatable) {
        updatable.onUpdate(String.format("%s: %s", layer.getTitle(), layer.getLevel()));
    }

    void nextClick(@NonNull StackLayer layer) {
        ((MainActivity) layer.getActivity()).addToStack(
                layer.getNextLayerTitle(),
                layer.getLevel() + 1,
                layer.isNextOpaque()
        );
    }
}
