package com.psliusar.layers.sample.screen.listener;

import android.net.Uri;

import com.psliusar.layers.Presenter;

public class ListenerPresenter extends Presenter<ListenerModel, ListenerLayer> {

    void viewCreated() {
        getModel().startUpdates(this);
    }

    void viewDestroyed() {
        getModel().stopUpdates();
    }

    void takePhotoClick() {
        getModel().takePhoto(getHost().getActivity());
    }

    void setPhotoUri(Uri uri) {
        getLayer().showPhoto(uri);
    }

    @Override
    protected ListenerModel onCreateModel() {
        return new ListenerModel();
    }
}
