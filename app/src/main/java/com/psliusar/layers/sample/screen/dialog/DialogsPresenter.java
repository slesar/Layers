package com.psliusar.layers.sample.screen.dialog;

import com.psliusar.layers.Model;
import com.psliusar.layers.Presenter;

public class DialogsPresenter extends Presenter<Model, DialogsLayer> {

    void simpleDialogClick() {
        getLayer().showSimpleDialog("Hello World!", "This is simple AlertDialog controlled via Layer");
    }

    void customDialogClick() {
        getLayer().showCustomDialog("Custom dialog");
    }
}
