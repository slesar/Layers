package com.psliusar.layers.sample.screen.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.psliusar.layers.DialogLayer;
import com.psliusar.layers.Layer;
import com.psliusar.layers.sample.R;

public class DialogsLayer extends Layer<DialogsPresenter> implements View.OnClickListener,
        CustomDialogLayer.OnCustomDialogListener {

    private static final String DIALOG_SIMPLE = "SimpleDialog";
    private static final String DIALOG_CUSTOM = "CustomDialog";

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_dialogs, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        bindClickListener(this,
                R.id.dialogs_simple,
                R.id.dialogs_custom);
    }

    @Override
    protected DialogsPresenter onCreatePresenter() {
        return new DialogsPresenter();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.dialogs_simple:
            getPresenter().simpleDialogClick();
            break;
        case R.id.dialogs_custom:
            getPresenter().customDialogClick();
            break;
        }
    }

    @Override
    public void onDialogAction1(CustomDialogLayer dialog) {
        if (DIALOG_CUSTOM.equals(dialog.getName())) {
            Toast.makeText(getContext(), "Action 1 was selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDialogAction2(CustomDialogLayer dialog) {
        if (DIALOG_CUSTOM.equals(dialog.getName())) {
            Toast.makeText(getContext(), "Action 2 was selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDialogCancel(@NonNull DialogLayer<?> dialog) {
        Toast.makeText(getContext(), "Dialog " + DIALOG_SIMPLE + " was dismissed", Toast.LENGTH_SHORT).show();
    }

    void showSimpleDialog(String title, String message) {
        Bundle args = SimpleDialogLayer.createArguments(title, message);
        getLayers().add(SimpleDialogLayer.class)
                .setArguments(args)
                .setName(DIALOG_SIMPLE)
                .commit();
    }

    void showCustomDialog(String title) {
        Bundle args = CustomDialogLayer.createArguments(title);
        getLayers().add(CustomDialogLayer.class)
                .setArguments(args)
                .setName(DIALOG_CUSTOM)
                .commit();
    }
}
