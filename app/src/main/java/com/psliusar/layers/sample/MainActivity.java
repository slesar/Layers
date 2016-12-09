package com.psliusar.layers.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.psliusar.layers.LayersActivity;
import com.psliusar.layers.sample.screen.child.ChildrenContainerLayer;
import com.psliusar.layers.sample.screen.dialog.DialogsLayer;
import com.psliusar.layers.sample.screen.home.HomeLayer;
import com.psliusar.layers.sample.screen.listener.ListenerLayer;
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

    public void addToStack(CharSequence title, int level, boolean opaque) {
        final Bundle args = StackLayer.createArguments(title, level);
        getLayers().add(StackLayer.class)
                .setArguments(args)
                .setName("Stack" + level)
                .setOpaque(opaque)
                .setInAnimation(R.anim.lower_out, R.anim.upper_in)
                .setOutAnimation(R.anim.lower_in, R.anim.upper_out)
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
}
