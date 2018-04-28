package com.psliusar.layers.sample.screen.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.psliusar.layers.DialogLayer;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.sample.R;

public class CustomDialogLayer extends DialogLayer<CustomDialogViewModel> implements View.OnClickListener {

    private static final String ARGS_TITLE = "ARGS_TITLE";

    public static Bundle createArguments(String title) {
        final Bundle bundle = new Bundle();
        bundle.putString(ARGS_TITLE, title);
        return bundle;
    }

    @Bind(value = R.id.dialog_action1, clicks = true) View action1Button;
    @Bind(value = R.id.dialog_action2, clicks = true) View action2Button;
    @Bind(R.id.dialog_title) TextView titleTextView;

    @Nullable
    @Override
    protected CustomDialogViewModel onCreateViewModel() {
        return new CustomDialogViewModel();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        setCancelable(false);
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_dialog_custom, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
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

    public interface OnCustomDialogListener extends OnLayerDialogListener {

        void onDialogAction1(@NonNull CustomDialogLayer dialog);

        void onDialogAction2(@NonNull CustomDialogLayer dialog);
    }
}
