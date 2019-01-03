package com.psliusar.layers.sample.screen.listener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.psliusar.layers.LayersActivity;
import com.psliusar.layers.callbacks.BaseActivityListener;
import com.psliusar.layers.subscription.Subscription;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.app.Activity.RESULT_OK;

public class ListenerModel {

    public interface Updatable<T> {

        void onUpdate(@Nullable T value);
    }

    private static final int CAMERA_IMAGE = 1001;

    public Subscription getPhotoUri(@NonNull LayersActivity activity, @NonNull final Updatable<Bitmap> updatable) {
        return activity.getActivityCallbacks().add(new BaseActivityListener() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                if (requestCode == CAMERA_IMAGE && resultCode == RESULT_OK) {
                    final Bitmap image = (Bitmap) intent.getExtras().get("data");
                    updatable.onUpdate(image);
                }
            }
        });
    }

    public void requestPhoto(@NonNull Activity activity) {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, CAMERA_IMAGE);
        }
    }
}
