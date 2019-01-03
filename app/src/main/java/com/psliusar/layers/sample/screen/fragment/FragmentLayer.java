package com.psliusar.layers.sample.screen.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.psliusar.layers.LayersFragment;
import com.psliusar.layers.Transition;
import com.psliusar.layers.sample.R;
import com.psliusar.layers.sample.screen.child.ChildLayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Logic is copied from {@link com.psliusar.layers.sample.screen.child.ChildrenContainerLayer}.
 */
public class FragmentLayer extends LayersFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.screen_children_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        if (state == null) {
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

        view.findViewById(R.id.children_container_add_layer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });
    }

    @Override
    protected int getDefaultContainerId() {
        return R.id.children_container;
    }
}
