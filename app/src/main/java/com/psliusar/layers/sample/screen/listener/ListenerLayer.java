package com.psliusar.layers.sample.screen.listener;

import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.psliusar.layers.Layer;
import com.psliusar.layers.LayersActivity;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.sample.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ListenerLayer extends Layer<ListenerViewModel> implements View.OnClickListener {

    @Bind(value = R.id.listener_take_photo, clicks = true) View takePhotoButton;

    @Nullable
    @Override
    protected ListenerViewModel onCreateViewModel() {
        return new ListenerViewModel();
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_listener, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        getViewModel().getPhoto((LayersActivity) getActivity(), new ListenerModel.Updatable<Uri>() {
            @Override
            public void onUpdate(@Nullable Uri uri) {
                Toast.makeText(getContext(), "New photo at " + (uri == null ? "null" : uri.toString()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.listener_take_photo:
            getViewModel().takePhotoClick(getActivity());
            break;
        }
    }
}
