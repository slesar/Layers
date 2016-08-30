package com.psliusar.layers.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.psliusar.layers.LayersActivity;
import com.psliusar.layers.sample.screen.child.ChildrenContainerLayer;
import com.psliusar.layers.sample.screen.home.HomeLayer;
import com.psliusar.layers.sample.screen.stack.StackLayer;

public class MainActivity extends LayersActivity {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        if (state == null) {
            getLayers().add(HomeLayer.class, null, null, true);
        }
        getLayers().resumeView();
    }

    @NonNull
    @Override
    public ViewGroup getDefaultContainer() {
        return getView(R.id.container);
    }

    public void addToStack(CharSequence title, int level, boolean opaque) {
        final Bundle args = StackLayer.createArguments(title, level);
        getLayers().add(StackLayer.class, args, "Stack" + level, opaque);
    }

    public void showChildrenLayers() {
        getLayers().add(ChildrenContainerLayer.class, null, "Children", true);
    }

    public void showDialogLayers() {

    }

    public void showActivityListener() {

    }
}
