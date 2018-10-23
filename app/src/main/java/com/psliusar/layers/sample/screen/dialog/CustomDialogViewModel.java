package com.psliusar.layers.sample.screen.dialog;

import com.psliusar.layers.Model;
import com.psliusar.layers.ViewModel;
import com.psliusar.layers.sample.screen.dialog.CustomDialogLayer.OnCustomDialogListener;

import androidx.annotation.NonNull;

public class CustomDialogViewModel extends ViewModel<Model> {

    public CustomDialogViewModel() {
        super(null);
    }

    void onAction1Click(@NonNull CustomDialogLayer layer) {
        layer.getParent(OnCustomDialogListener.class).onDialogAction1(layer);
        layer.getDialogWrapper().dismiss(false);
    }

    void onAction2Click(@NonNull CustomDialogLayer layer) {
        layer.getParent(OnCustomDialogListener.class).onDialogAction2(layer);
        layer.getDialogWrapper().dismiss(false);
    }
}
