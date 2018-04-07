package com.psliusar.layers.sample.screen.listener;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.psliusar.layers.Model;
import com.psliusar.layers.callbacks.BaseActivityListener;
import com.psliusar.layers.callbacks.ManagedSubscriptions;

public class ListenerModel implements Model {

    private static final int CAMERA_IMAGE = 1001;

    private final ManagedSubscriptions subscriptions = new ManagedSubscriptions();

    void startUpdates(@NonNull final ListenerPresenter presenter) {
        subscriptions.manage(presenter.getActivity().getActivityCallbacks().add(new BaseActivityListener() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                presenter.setPhotoUri(intent != null ? intent.getData() : null);
            }
        }));
    }

    void stopUpdates() {
        subscriptions.unSubscribeAll();
    }

    void takePhoto(@NonNull Activity activity) {
        activity.startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_IMAGE);
    }
}
