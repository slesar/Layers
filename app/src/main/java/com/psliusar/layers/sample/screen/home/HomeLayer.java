package com.psliusar.layers.sample.screen.home;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.psliusar.layers.Layer;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.sample.MainActivity;
import com.psliusar.layers.sample.R;

public class HomeLayer extends Layer<HomePresenter> implements View.OnClickListener {

    @Bind(value = R.id.home_stack, clicks = true) View stackButton;
    @Bind(value = R.id.home_children, clicks = true) View childrenButton;
    @Bind(value = R.id.home_dialog, clicks = true) View dialogButton;
    @Bind(value = R.id.home_activity_listener, clicks = true) View activityListenerButton;
    @Bind(value = R.id.home_save_annotation, clicks = true) View saveAnnotationButton;
    @Bind(value = R.id.home_tasks, clicks = true) View tasksButton;

    @Nullable
    @Override
    protected HomePresenter onCreatePresenter() {
        return new HomePresenter((MainActivity) getActivity());
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_home, parent);
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
        case R.id.home_save_annotation:
            getPresenter().saveClick();
            break;
        case R.id.home_tasks:
            getPresenter().tasksClick();
            break;
        }
    }
}
