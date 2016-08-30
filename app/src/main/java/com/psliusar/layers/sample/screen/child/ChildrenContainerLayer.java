package com.psliusar.layers.sample.screen.child;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.psliusar.layers.Layer;
import com.psliusar.layers.sample.R;

public class ChildrenContainerLayer extends Layer<ChildrenContainerPresenter> {

    @Nullable
    @Override
    protected View onCreateView(@NonNull ViewGroup parent) {
        return inflate(R.layout.screen_children_container, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);


    }

    @Override
    protected ChildrenContainerPresenter onCreatePresenter() {
        return new ChildrenContainerPresenter();
    }
}
