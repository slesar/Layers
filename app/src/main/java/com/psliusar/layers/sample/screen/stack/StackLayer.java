package com.psliusar.layers.sample.screen.stack;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.psliusar.layers.Layer;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.binder.Binder;
import com.psliusar.layers.binder.Save;
import com.psliusar.layers.binder.ViewBindManager;
import com.psliusar.layers.sample.R;
import com.psliusar.layers.sample.screen.listener.ListenerModel;

public class StackLayer extends Layer<StackViewModel> {

    @Bind(value = R.id.stack_level, parent = R.id.stack_container) TextView stackLevel;
    @Bind(value = R.id.stack_next_opaque, parent = R.id.stack_container) CheckBox nextOpaque;
    @Bind(value = R.id.stack_next_title, bindManager = TextViewBindManager.class) TextView nextTitle;
    @Bind(value = R.id.stack_next, clicks = true) View buttonNext;

    @Save CharSequence title;
    @Save int level;

    @Nullable
    @Override
    protected StackViewModel onCreateViewModel() {
        return new StackViewModel();
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_stack, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        new InnerLayerBinder(view);

        getViewModel().getStackLevelText(this, new ListenerModel.Updatable<String>() {
            @Override
            public void onUpdate(@Nullable String value) {
                stackLevel.setText(value);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.stack_next:
            getViewModel().nextClick(this);
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

    CharSequence getNextLayerTitle() {
        return nextTitle.getText();
    }

    boolean isNextOpaque() {
        return nextOpaque.isChecked();
    }

    public static class InnerLayerBinder implements View.OnClickListener {

        @Bind(R.id.stack_next_title) TextView nextTitle;

        public InnerLayerBinder(@NonNull View view) {
            Binder.bind(this, view);
        }

        @Override
        public void onClick(View v) {

        }
    }

    public static class TextViewBindManager extends ViewBindManager {

        @NonNull
        @Override
        public View find(@NonNull View.OnClickListener listener, @NonNull View container, int viewResId) {
            final TextView view = (TextView) super.find(listener, container, viewResId);
            view.setTypeface(Typeface.SERIF);
            return view;
        }
    }
}
