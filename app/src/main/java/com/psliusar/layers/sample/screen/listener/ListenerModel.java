package com.psliusar.layers.sample.screen.listener;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.psliusar.layers.LayersActivity;
import com.psliusar.layers.Model;
import com.psliusar.layers.callbacks.BaseActivityListener;
import com.psliusar.layers.subscription.Subscription;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ListenerModel implements Model {

    public interface Updatable<T> {

        void onUpdate(@Nullable T value);
    }

    private static final int CAMERA_IMAGE = 1001;

    public Subscription getPhotoUri(@NonNull LayersActivity activity, @NonNull final Updatable<Uri> updatable) {
        return activity.getActivityCallbacks().add(new BaseActivityListener() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                updatable.onUpdate(intent != null ? intent.getData() : null);
            }
        });
    }

    public void requestPhoto(@NonNull Activity activity) {
        activity.startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_IMAGE);
    }
}
