package com.psliusar.layers.sample.screen.dialog;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.psliusar.layers.DialogWrapper;
import com.psliusar.layers.Layer;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.sample.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomDialogLayer extends Layer<CustomDialogViewModel> implements View.OnClickListener {

    private static final String ARGS_TITLE = "ARGS_TITLE";

    public static Bundle createArguments(String title) {
        final Bundle bundle = new Bundle();
        bundle.putString(ARGS_TITLE, title);
        return bundle;
    }

    @Bind(value = R.id.dialog_action1, clicks = true) View action1Button;
    @Bind(value = R.id.dialog_action2, clicks = true) View action2Button;
    @Bind(R.id.dialog_title) TextView titleTextView;

    private final DialogWrapper wrapper = new DialogWrapper(this);

    @Nullable
    @Override
    protected CustomDialogViewModel onCreateViewModel() {
        return new CustomDialogViewModel();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        wrapper.setCancelable(false);
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable Bundle savedState, @Nullable ViewGroup parent) {
        return inflate(R.layout.screen_dialog_custom, parent);
    }

    @Override
    protected void onBindView(@Nullable Bundle savedState, @NonNull View view) {
        super.onBindView(savedState, view);
        titleTextView.setText(getArguments().getString(ARGS_TITLE));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_action1:
                getViewModel().onAction1Click(this);
                break;
            case R.id.dialog_action2:
                getViewModel().onAction2Click(this);
                break;
        }
    }

    @NonNull
    public DialogWrapper getDialogWrapper() {
        return wrapper;
    }

    public interface OnCustomDialogListener extends DialogWrapper.OnLayerDialogListener {

        void onDialogAction1(@NonNull CustomDialogLayer dialog);

        void onDialogAction2(@NonNull CustomDialogLayer dialog);
    }
}
