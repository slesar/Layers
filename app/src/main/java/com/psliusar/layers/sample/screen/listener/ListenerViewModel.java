package com.psliusar.layers.sample.screen.listener;

import android.app.Activity;
import android.graphics.Bitmap;

import com.psliusar.layers.LayersActivity;
import com.psliusar.layers.ViewModel;

import androidx.annotation.NonNull;

public class ListenerViewModel extends ViewModel {

    private final ListenerModel model = new ListenerModel();

    void getPhoto(@NonNull LayersActivity activity, @NonNull ListenerModel.Updatable<Bitmap> updatable) {
        manage(model.getPhotoUri(activity, updatable));
    }

    void takePhotoClick(@NonNull Activity activity) {
        model.requestPhoto(activity);
    }
}
