package com.psliusar.layers.sample.screen.dialog;

import android.support.annotation.NonNull;

import com.psliusar.layers.Model;
import com.psliusar.layers.Presenter;

public class DialogsPresenter extends Presenter<Model> {

    private final DialogsLayer layer;

    public DialogsPresenter(@NonNull DialogsLayer layer) {
        this.layer = layer;
    }

    void simpleDialogClick() {
        layer.showSimpleDialog("Hello World!", "This is simple AlertDialog controlled via Layer");
    }

    void customDialogClick() {
        layer.showCustomDialog("Custom dialog");
    }
}
