package com.psliusar.layers.sample.screen.save;

import android.support.annotation.NonNull;

import com.psliusar.layers.Model;
import com.psliusar.layers.Presenter;

public class SavePresenter extends Presenter<Model> {

    private final SaveLayer layer;

    public SavePresenter(@NonNull SaveLayer layer) {
        this.layer = layer;
    }
}
