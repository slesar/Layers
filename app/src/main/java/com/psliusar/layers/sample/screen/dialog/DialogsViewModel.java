package com.psliusar.layers.sample.screen.dialog;

import com.psliusar.layers.Model;
import com.psliusar.layers.ViewModel;

import androidx.annotation.NonNull;

public class DialogsViewModel extends ViewModel<Model> {

    public DialogsViewModel() {
        super(null);
    }

    void simpleDialogClick(@NonNull DialogsLayer layer) {
        layer.showSimpleDialog("Hello World!", "This is simple AlertDialog controlled via Layer");
    }

    void customDialogClick(@NonNull DialogsLayer layer) {
        layer.showCustomDialog("Custom dialog");
    }
}
