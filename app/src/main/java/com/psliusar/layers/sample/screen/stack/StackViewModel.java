package com.psliusar.layers.sample.screen.stack;

import com.psliusar.layers.ViewModel;
import com.psliusar.layers.sample.MainActivity;
import com.psliusar.layers.sample.screen.listener.ListenerModel;

import androidx.annotation.NonNull;

public class StackViewModel extends ViewModel {

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

    void replaceClick(@NonNull StackLayer layer) {
        ((MainActivity) layer.getActivity()).replaceInStack(
            layer.getNextLayerTitle(),
            layer.getLevel() + 1,
            layer.isNextOpaque()
        );
    }
}
