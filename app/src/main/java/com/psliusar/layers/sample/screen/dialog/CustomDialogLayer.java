package com.psliusar.layers.sample.screen.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.psliusar.layers.DialogLayer;
import com.psliusar.layers.sample.R;

public class CustomDialogLayer extends DialogLayer<CustomDialogPresenter> implements View.OnClickListener {

    private static final String ARGS_TITLE = "ARGS_TITLE";

    public static Bundle createArguments(String title) {
        final Bundle bundle = new Bundle();
        bundle.putString(ARGS_TITLE, title);
        return bundle;
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
        ((TextView) getView(R.id.dialog_title)).setText(getArguments().getString(ARGS_TITLE));
        onClick(this, R.id.dialog_action1, R.id.dialog_action2);
    }

    @Override
    protected CustomDialogPresenter onCreatePresenter() {
        return new CustomDialogPresenter();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_action1:
                getPresenter().onAction1Click();
                break;
            case R.id.dialog_action2:
                getPresenter().onAction2Click();
                break;
        }
    }

    void performAction1Callback() {
        OnCustomDialogListener parent = getParent(OnCustomDialogListener.class);
        if (parent != null) {
            parent.onDialogAction1(this);
        }
        dismiss(false);
    }

    void performAction2Callback() {
        OnCustomDialogListener parent = getParent(OnCustomDialogListener.class);
        if (parent != null) {
            parent.onDialogAction2(this);
        }
        dismiss(false);
    }

    public interface OnCustomDialogListener extends OnLayerDialogListener {

        void onDialogAction1(CustomDialogLayer dialog);

        void onDialogAction2(CustomDialogLayer dialog);
    }
}
