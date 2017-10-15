package com.psliusar.layers.sample.screen.listener;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.psliusar.layers.Layer;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.sample.R;

public class ListenerLayer extends Layer<ListenerPresenter> implements View.OnClickListener {

    @Bind(value = R.id.listener_take_photo, clicks = true) View takePhotoButton;

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_listener, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        getPresenter().viewCreated();
    }

    @Override
    protected void onDestroyView() {
        super.onDestroyView();
        getPresenter().viewDestroyed();
    }

    @Override
    protected ListenerPresenter onCreatePresenter() {
        return new ListenerPresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.listener_take_photo:
            getPresenter().takePhotoClick();
            break;
        }
    }

    void showPhoto(Uri uri) {
        Toast.makeText(getContext(), "New photo at " + (uri == null ? "null" : uri.toString()), Toast.LENGTH_SHORT).show();
    }
}
