package com.psliusar.layers.sample.screen.stack;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.Layer;
import com.psliusar.layers.binder.Save;
import com.psliusar.layers.sample.R;

public class StackLayer extends Layer<StackPresenter> {

    @Keep
    @Bind(value = R.id.stack_level, parent = R.id.stack_container)
    protected TextView stackLevel;

    @Keep
    @Bind(value = R.id.stack_next_opaque, parent = R.id.stack_container)
    protected CheckBox nextOpaque;

    @Keep
    @Bind(R.id.stack_next_title)
    protected TextView nextTitle;

    @Keep
    @Bind(value = R.id.stack_next, clicks = true)
    protected View buttonNext;

    @Save
    protected CharSequence title;

    @Save
    protected int level;

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

    public void setParameters(CharSequence title, int level) {
        this.title = title;
        this.level = level;
    }

    public CharSequence getTitle() {
        return title;
    }

    public int getLevel() {
        return level;
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
