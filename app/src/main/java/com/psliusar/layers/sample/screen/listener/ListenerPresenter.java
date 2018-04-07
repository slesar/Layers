package com.psliusar.layers.sample.screen.listener;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.psliusar.layers.Presenter;
import com.psliusar.layers.sample.MainActivity;

public class ListenerPresenter extends Presenter<ListenerModel> {

    private final MainActivity activity;
    private final ListenerLayer layer;

    public ListenerPresenter(@NonNull MainActivity activity, @NonNull ListenerLayer layer) {
        this.activity = activity;
        this.layer = layer;
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
        getModel().takePhoto(activity);
    }

    void setPhotoUri(Uri uri) {
        layer.showPhoto(uri);
    }

    @NonNull
    MainActivity getActivity() {
        return activity;
    }
}
