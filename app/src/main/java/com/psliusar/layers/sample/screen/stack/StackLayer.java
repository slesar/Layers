package com.psliusar.layers.sample.screen.stack;

import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.Layer;
import com.psliusar.layers.sample.R;

public class StackLayer extends Layer<StackPresenter> {

    static final String ARGS_TITLE = "ARGS_TITLE";
    static final String ARGS_LEVEL = "ARGS_LEVEL";

    public static Bundle createArguments(CharSequence title, int level) {
        final Bundle bundle = new Bundle();
        bundle.putCharSequence(ARGS_TITLE, title);
        bundle.putInt(ARGS_LEVEL, level);
        return bundle;
    }

    @Keep
    @Bind(R.id.stack_level)
    private TextView stackLevel;

    @Keep
    @Bind(R.id.stack_next_opaque)
    private CheckBox nextOpaque;

    @Keep
    @Bind(R.id.stack_next_title)
    private TextView nextTitle;

    @Keep
    @Bind(value = R.id.stack_next, clicks = true)
    private View buttonNext;

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_stack, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        getPresenter().initViews();
    }

    @Override
    protected StackPresenter onCreatePresenter() {
        return new StackPresenter();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.stack_next:
            getPresenter().nextClick();
            break;
        }
    }

    @NonNull
    @Override
    public Bundle getArguments() {
        final Bundle args = super.getArguments();
        if (args == null) {
            throw new IllegalArgumentException("StackLayer must have arguments");
        }
        return args;
    }

    void setStackLevelText(@Nullable CharSequence text) {
        stackLevel.setText(text);
    }

    CharSequence getNextLayerTitle() {
        return nextTitle.getText();
    }

    boolean isNextOpaque() {
        return nextOpaque.isChecked();
    }
}
