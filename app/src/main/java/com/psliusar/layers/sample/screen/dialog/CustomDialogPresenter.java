package com.psliusar.layers.sample.screen.dialog;

import android.support.annotation.NonNull;

import com.psliusar.layers.Model;
import com.psliusar.layers.Presenter;

public class CustomDialogPresenter extends Presenter<Model> {

    private final CustomDialogLayer layer;

    public CustomDialogPresenter(@NonNull CustomDialogLayer layer) {
        this.layer = layer;
    }

    void onAction1Click() {
        layer.performAction1Callback();
    }

    void onAction2Click() {
        layer.performAction2Callback();
    }
}
