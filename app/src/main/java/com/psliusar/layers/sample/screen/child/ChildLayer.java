package com.psliusar.layers.sample.screen.child;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.psliusar.layers.Layer;
import com.psliusar.layers.Presenter;
import com.psliusar.layers.sample.R;

public class ChildLayer extends Layer<Presenter<?, ?>> {

    private static final String ARGS_TITLE = "ARGS_TITLE";

    @NonNull
    public static Bundle createArguments(@Nullable String title) {
        final Bundle bundle = new Bundle();
        bundle.putString(ARGS_TITLE, title);
        return bundle;
    }

    @Nullable
    @Override
    protected View onCreateView(@NonNull ViewGroup parent) {
        return inflate(R.layout.screen_child, parent);
    }

    @Override
    protected Presenter<?, ?> onCreatePresenter() {
        return null;
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        ((TextView) getView(R.id.child_title)).setText(getArguments().getString(ARGS_TITLE));
    }
}
