package com.psliusar.layers.sample.screen.home;

import android.view.View;
import android.view.ViewGroup;

import com.psliusar.layers.Layer;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.sample.MainActivity;
import com.psliusar.layers.sample.R;

import androidx.annotation.Nullable;

public class HomeLayer extends Layer<HomeViewModel> implements View.OnClickListener {

    @Bind(value = R.id.home_stack, clicks = true) View stackButton;
    @Bind(value = R.id.home_children, clicks = true) View childrenButton;
    @Bind(value = R.id.home_dialog, clicks = true) View dialogButton;
    @Bind(value = R.id.home_activity_listener, clicks = true) View activityListenerButton;
    @Bind(value = R.id.home_save_annotation, clicks = true) View saveAnnotationButton;
    @Bind(value = R.id.home_tasks, clicks = true) View tasksButton;
    @Bind(value = R.id.home_fragment, clicks = true) View fragmentButton;

    @Nullable
    @Override
    protected HomeViewModel onCreateViewModel() {
        return new HomeViewModel();
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
            getViewModel().stackClick((MainActivity) getActivity());
            break;
        case R.id.home_children:
            getViewModel().childrenClick((MainActivity) getActivity());
            break;
        case R.id.home_dialog:
            getViewModel().dialogClick((MainActivity) getActivity());
            break;
        case R.id.home_activity_listener:
            getViewModel().listenerClick((MainActivity) getActivity());
            break;
        case R.id.home_save_annotation:
            getViewModel().saveClick((MainActivity) getActivity());
            break;
        case R.id.home_tasks:
            getViewModel().tasksClick((MainActivity) getActivity());
            break;
        case R.id.home_fragment:
            getViewModel().fragmentClick((MainActivity) getActivity());
            break;
        }
    }
}
