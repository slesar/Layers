package com.psliusar.layers.sample.screen.dialog;

import com.psliusar.layers.Model;
import com.psliusar.layers.Presenter;

public class CustomDialogPresenter extends Presenter<Model, CustomDialogLayer> {

    void onAction1Click() {
        getLayer().performAction1Callback();
    }

    void onAction2Click() {
        getLayer().performAction2Callback();
    }
}
