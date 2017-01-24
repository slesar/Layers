package com.psliusar.layers.binder;

import android.support.annotation.NonNull;
import android.view.View;

public abstract class ViewBinder {

    protected abstract void bind(@NonNull View.OnClickListener listener, @NonNull View view);

    protected abstract void unbind(@NonNull View.OnClickListener listener);
}
