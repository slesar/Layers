package com.psliusar.layers.sample.screen.home;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.psliusar.layers.Layer;
import com.psliusar.layers.sample.R;

public class HomeLayer extends Layer<HomePresenter> implements View.OnClickListener {

    @Nullable
    @Override
    protected View onCreateView(@NonNull ViewGroup parent) {
        return inflate(R.layout.screen_home, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        onClick(this, R.id.home_stack, R.id.home_children, R.id.home_dialog, R.id.home_activity_listener);
    }

    @Override
    protected HomePresenter onCreatePresenter() {
        return new HomePresenter();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.home_stack:
            getPresenter().stackClick();
            break;
        case R.id.home_children:
            getPresenter().childrenClick();
            break;
        case R.id.home_dialog:
            getPresenter().dialogClick();
            break;
        case R.id.home_activity_listener:
            getPresenter().listenerClick();
            break;
        }
    }
}
