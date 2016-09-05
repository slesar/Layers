package com.psliusar.layers.sample.screen.listener;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.psliusar.layers.ActivityCallbacks;
import com.psliusar.layers.Layer;
import com.psliusar.layers.LayersActivity;
import com.psliusar.layers.Presenter;
import com.psliusar.layers.sample.R;

public class ListenerLayer extends Layer<Presenter<?, ?>> {

    @Nullable
    @Override
    protected View onCreateView(@NonNull ViewGroup parent) {
        return inflate(R.layout.screen_listener, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        final ActivityCallbacks.EventSubscription subscription = ((LayersActivity) getActivity()).getActivityCallbacks().add(new ActivityCallbacks.BaseActivityListener(ActivityCallbacks.EVENT_ON_ACTIVITY_RESULT) {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                //
            }
        });

        manage(subscription);
    }

    @Override
    protected Presenter<?, ?> onCreatePresenter() {
        return null;
    }
}
