package com.psliusar.layers.sample.screen.listener;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.psliusar.layers.Layer;
import com.psliusar.layers.LayersActivity;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.sample.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ListenerLayer extends Layer<ListenerViewModel> implements View.OnClickListener {

    @Bind(value = R.id.listener_take_photo, clicks = true) View takePhotoButton;
    @Bind(value = R.id.listener_image, clicks = true) ImageView imageView;

    @Nullable
    @Override
    protected ListenerViewModel onCreateViewModel() {
        return new ListenerViewModel();
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable Bundle savedState, @Nullable ViewGroup parent) {
        return inflate(R.layout.screen_listener, parent);
    }

    @Override
    protected void onBindView(@Nullable Bundle savedState, @NonNull View view) {
        super.onBindView(savedState, view);
        getViewModel().getPhoto((LayersActivity) getActivity(), new ListenerModel.Updatable<Bitmap>() {
            @Override
            public void onUpdate(@Nullable Bitmap image) {
                imageView.setImageBitmap(image);
            }
        });
    }

    @Override
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
        case R.id.listener_take_photo:
            getViewModel().takePhotoClick(getActivity());
            break;
        }
    }
}
