package com.psliusar.layers.sample.screen.child;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.psliusar.layers.Layer;
import com.psliusar.layers.ViewModel;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.binder.Save;
import com.psliusar.layers.sample.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ChildLayer extends Layer<ViewModel> {

    @Bind(R.id.child_title) TextView titleTextView;
    @Save String title;

    @Nullable
    @Override
    protected ViewModel onCreateViewModel() {
        return null;
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable Bundle savedState, @Nullable ViewGroup parent) {
        return inflate(R.layout.screen_child, parent);
    }

    @Override
    protected void onBindView(@Nullable Bundle savedState, @NonNull View view) {
        super.onBindView(savedState, view);
        titleTextView.setText(title);
    }

    public void setParameters(String title) {
        this.title = title;
    }
}
