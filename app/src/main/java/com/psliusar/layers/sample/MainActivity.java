package com.psliusar.layers.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.psliusar.layers.LayersActivity;
import com.psliusar.layers.Transition;
import com.psliusar.layers.sample.screen.track.TracksLayer;
import com.psliusar.layers.sample.screen.child.ChildrenContainerLayer;
import com.psliusar.layers.sample.screen.dialog.DialogsLayer;
import com.psliusar.layers.sample.screen.home.HomeLayer;
import com.psliusar.layers.sample.screen.listener.ListenerLayer;
import com.psliusar.layers.sample.screen.save.SaveLayer;
import com.psliusar.layers.sample.screen.stack.StackLayer;

public class MainActivity extends LayersActivity {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        if (state == null) {
            getLayers().add(HomeLayer.class).setName("Home").commit();
        }
    }

    @NonNull
    @Override
    public ViewGroup getDefaultContainer() {
        return getView(R.id.container);
    }

    public void addToStack(final CharSequence title, final int level, boolean opaque) {
        getLayers().add(StackLayer.class)
                .prepareLayer(new Transition.OnLayerTransition<StackLayer>() {
                    @Override
                    public void onBeforeTransition(@NonNull StackLayer layer) {
                        layer.setParameters(title, level);
                    }
                })
                .setName("Stack" + level)
                .setOpaque(opaque)
                .setInAnimation(R.anim.lower_out, R.anim.upper_in)
                .setOutAnimation(R.anim.upper_out, R.anim.lower_in)
                .commit();
    }

    public void showChildrenLayers() {
        getLayers().add(ChildrenContainerLayer.class)
                .setName("Children")
                .commit();
    }

    public void showDialogLayers() {
        getLayers().add(DialogsLayer.class)
                .setName("Dialogs")
                .commit();
    }

    public void showActivityListener() {
        getLayers().add(ListenerLayer.class)
                .setName("Listener")
                .commit();
    }

    public void showSaveState() {
        getLayers().add(SaveLayer.class)
                .prepareLayer(new Transition.OnLayerTransition<SaveLayer>() {
                    @Override
                    public void onBeforeTransition(@NonNull SaveLayer layer) {
                        layer.setParameters("First", "Second", "Third");
                    }
                })
                .setName("Save")
                .commit();
    }

    public void showTracks() {
        getLayers().add(TracksLayer.class)
                .setName("Tracks")
                .commit();
    }
}
