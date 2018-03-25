package com.psliusar.layers.sample.screen.dialog;

import android.support.annotation.NonNull;

import com.psliusar.layers.Model;
import com.psliusar.layers.Presenter;

public class DialogsPresenter extends Presenter<Model, DialogsLayer> {

    public DialogsPresenter(@NonNull DialogsLayer layer) {
        super(layer);
    }

    void simpleDialogClick() {
        getLayer().showSimpleDialog("Hello World!", "This is simple AlertDialog controlled via Layer");
    }

    void customDialogClick() {
        getLayer().showCustomDialog("Custom dialog");
    }
}
