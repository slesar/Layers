package com.psliusar.layers.sample.screen.dialog;

import android.support.annotation.NonNull;

import com.psliusar.layers.Model;
import com.psliusar.layers.Presenter;

public class CustomDialogPresenter extends Presenter<Model, CustomDialogLayer> {

    public CustomDialogPresenter(@NonNull CustomDialogLayer layer) {
        super(layer);
    }

    void onAction1Click() {
        getLayer().performAction1Callback();
    }

    void onAction2Click() {
        getLayer().performAction2Callback();
    }
}
