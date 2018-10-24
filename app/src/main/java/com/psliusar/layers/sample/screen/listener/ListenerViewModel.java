package com.psliusar.layers.sample.screen.listener;

import android.app.Activity;
import android.graphics.Bitmap;

import com.psliusar.layers.LayersActivity;
import com.psliusar.layers.ViewModel;

import androidx.annotation.NonNull;

public class ListenerViewModel extends ViewModel<ListenerModel> {

    public ListenerViewModel() {
        super(new ListenerModel());
    }

    void getPhoto(@NonNull LayersActivity activity, @NonNull ListenerModel.Updatable<Bitmap> updatable) {
        manage(getModel().getPhotoUri(activity, updatable));
    }

    void takePhotoClick(@NonNull Activity activity) {
        getModel().requestPhoto(activity);
    }
}
