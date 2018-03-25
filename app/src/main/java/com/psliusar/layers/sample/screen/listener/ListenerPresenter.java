package com.psliusar.layers.sample.screen.listener;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.psliusar.layers.Presenter;

public class ListenerPresenter extends Presenter<ListenerModel, ListenerLayer> {

    public ListenerPresenter(@NonNull ListenerLayer layer) {
        super(layer);
    }

    @Override
    protected ListenerModel onCreateModel() {
        return new ListenerModel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getModel().startUpdates(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getModel().stopUpdates();
    }

    void takePhotoClick() {
        getModel().takePhoto(getActivity());
    }

    void setPhotoUri(Uri uri) {
        getLayer().showPhoto(uri);
    }
}
