package com.psliusar.layers.sample.screen.child;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.psliusar.layers.Layer;
import com.psliusar.layers.ViewModel;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.binder.Save;
import com.psliusar.layers.sample.R;

public class ChildLayer extends Layer<ViewModel<?>> {

    @Bind(R.id.child_title) TextView titleTextView;
    @Save String title;

    @Nullable
    @Override
    protected ViewModel<?> onCreateViewModel() {
        return null;
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_child, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        titleTextView.setText(title);
    }

    public void setParameters(String title) {
        this.title = title;
    }
}
