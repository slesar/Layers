package com.psliusar.layers.sample.screen.stack;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.psliusar.layers.binder.Binder;
import com.psliusar.layers.binder.ViewBinder;
import com.psliusar.layers.sample.R;

public class StackLayer$$ViewBinder extends ViewBinder {

    public StackLayer$$ViewBinder() {

    }

    @Override
    protected void bind(@NonNull View.OnClickListener listener, @NonNull View view) {
        final StackLayer target = (StackLayer) listener;

        target.stackLevel = (TextView) Binder.findViewOrThrow(TextView.class, view, R.id.stack_level, View.NO_ID);
        target.stackLevel.setOnClickListener(target);
    }

    @Override
    protected void unbind(@NonNull View.OnClickListener listener) {
        final StackLayer target = (StackLayer) listener;

        target.stackLevel = null;
    }
}
