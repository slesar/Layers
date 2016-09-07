package com.psliusar.layers.sample.screen.child;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.psliusar.layers.Layer;
import com.psliusar.layers.Presenter;
import com.psliusar.layers.sample.R;

public class ChildrenContainerLayer extends Layer<Presenter<?, ?>> {

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_children_container, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        if (!isFromSavedState()) {
            final Bundle argsTop = ChildLayer.createArguments("Top layer");
            getLayers().at(R.id.children_container_top).add(ChildLayer.class, argsTop, "Top", true);

            final Bundle argsMiddle = ChildLayer.createArguments("Middle layer");
            getLayers().at(R.id.children_container_middle).add(ChildLayer.class, argsMiddle, "Middle", true);

            final Bundle argsBottom = ChildLayer.createArguments("Bottom layer");
            getLayers().at(R.id.children_container_bottom).add(ChildLayer.class, argsBottom, "Bottom", true);
        }
    }

    @Override
    protected Presenter<?, ?> onCreatePresenter() {
        return null;
    }
}
