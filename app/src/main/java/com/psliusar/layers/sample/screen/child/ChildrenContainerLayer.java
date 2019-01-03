package com.psliusar.layers.sample.screen.child;

import android.view.View;
import android.view.ViewGroup;

import com.psliusar.layers.Layer;
import com.psliusar.layers.Transition;
import com.psliusar.layers.ViewModel;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.sample.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ChildrenContainerLayer extends Layer<ViewModel> {

    @Bind(value = R.id.children_container_add_layer, clicks = true)
    View addLayerButton;

    @Nullable
    @Override
    protected ViewModel onCreateViewModel() {
        return null;
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_children_container, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        if (!isFromSavedState()) {
            getLayers().at(R.id.children_container_top)
                    .add(ChildLayer.class)
                    .prepareLayer(new Transition.OnLayerTransition<ChildLayer>() {
                        @Override
                        public void onBeforeTransition(@NonNull ChildLayer layer) {
                            layer.setParameters("Top layer");
                        }
                    })
                    .setName("Top")
                    .commit();

            getLayers().at(R.id.children_container_middle)
                    .add(ChildLayer.class)
                    .prepareLayer(new Transition.OnLayerTransition<ChildLayer>() {
                        @Override
                        public void onBeforeTransition(@NonNull ChildLayer layer) {
                            layer.setParameters("Middle layer");
                        }
                    })
                    .setName("Middle")
                    .commit();

            getLayers().at(R.id.children_container_bottom)
                    .add(ChildLayer.class)
                    .prepareLayer(new Transition.OnLayerTransition<ChildLayer>() {
                        @Override
                        public void onBeforeTransition(@NonNull ChildLayer layer) {
                            layer.setParameters("Bottom layer");
                        }
                    })
                    .setName("Bottom")
                    .commit();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.children_container_add_layer:
            final int number = getLayers().getStackSize() + 1;
            getLayers()
                    .add(ChildLayer.class)
                    .prepareLayer(new Transition.OnLayerTransition<ChildLayer>() {
                        @Override
                        public void onBeforeTransition(@NonNull ChildLayer layer) {
                            layer.setParameters("Stack layer " + number);
                        }
                    })
                    .setName("Stack #" + number)
                    .setOpaque(false)
                    .commit();
            break;
        default:
            super.onClick(v);
        }
    }
}
