package com.psliusar.layers.sample.screen.listener;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.psliusar.layers.Presenter;

public class ListenerPresenter extends Presenter<ListenerModel, ListenerLayer> {

    public ListenerPresenter(@NonNull ListenerLayer layer) {
        super(layer);
    }

    void viewCreated() {
        getModel().startUpdates(this);
    }

    void viewDestroyed() {
        getModel().stopUpdates();
    }

    void takePhotoClick() {
        getModel().takePhoto(getActivity());
    }

    void setPhotoUri(Uri uri) {
        getLayer().showPhoto(uri);
    }

    @Override
    protected ListenerModel onCreateModel() {
        return new ListenerModel();
    }
}
