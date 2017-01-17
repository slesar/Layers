package com.psliusar.layers.sample.screen.listener;

import android.content.Intent;
import android.provider.MediaStore;

import com.psliusar.layers.ActivityCallbacks;
import com.psliusar.layers.Model;
import com.psliusar.layers.sample.MainActivity;

public class ListenerModel implements Model {

    private static final int CAMERA_IMAGE = 1001;

    private final ActivityCallbacks.ManagedSubscriptions subscriptions = new ActivityCallbacks.ManagedSubscriptions();
    ListenerPresenter presenter;

    void startUpdates(ListenerPresenter p) {
        this.presenter = p;

        final MainActivity activity = (MainActivity) presenter.getHost().getActivity();
        activity.getActivityCallbacks().add(new ActivityCallbacks.BaseActivityListener() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                presenter.setPhotoUri(intent != null ? intent.getData() : null);
            }
        }).manageWith(subscriptions);
    }

    void stopUpdates() {
        subscriptions.unsubscribeAll();
    }

    void pickPhoto() {
        presenter.getHost().getActivity().startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_IMAGE);
    }
}
