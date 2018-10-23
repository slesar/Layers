package com.psliusar.layers.sample.screen.dialog;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.psliusar.layers.DialogWrapper;
import com.psliusar.layers.Layer;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.sample.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DialogsLayer extends Layer<DialogsViewModel> implements View.OnClickListener,
        CustomDialogLayer.OnCustomDialogListener {

    private static final String DIALOG_SIMPLE = "SimpleDialog";
    private static final String DIALOG_CUSTOM = "CustomDialog";

    @Bind(value = R.id.dialogs_simple, clicks = true) View simpleButton;
    @Bind(value = R.id.dialogs_custom, clicks = true) View customButton;

    @Nullable
    @Override
    protected DialogsViewModel onCreateViewModel() {
        return new DialogsViewModel();
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_dialogs, parent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.dialogs_simple:
            getViewModel().simpleDialogClick(this);
            break;
        case R.id.dialogs_custom:
            getViewModel().customDialogClick(this);
            break;
        }
    }

    @Override
    public void onDialogAction1(@NonNull CustomDialogLayer layer) {
        if (DIALOG_CUSTOM.equals(layer.getName())) {
            Toast.makeText(getContext(), "Action 1 was selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDialogAction2(@NonNull CustomDialogLayer layer) {
        if (DIALOG_CUSTOM.equals(layer.getName())) {
            Toast.makeText(getContext(), "Action 2 was selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDialogCancel(@NonNull DialogWrapper wrapper) {
        Toast.makeText(getContext(), "Dialog " + DIALOG_SIMPLE + " was dismissed", Toast.LENGTH_SHORT).show();
    }

    void showSimpleDialog(String title, String message) {
        final Bundle args = SimpleDialogLayer.createArguments(title, message);
        getLayers().add(SimpleDialogLayer.class)
                .setArguments(args)
                .setName(DIALOG_SIMPLE)
                .commit();
    }

    void showCustomDialog(String title) {
        final Bundle args = CustomDialogLayer.createArguments(title);
        getLayers().add(CustomDialogLayer.class)
                .setArguments(args)
                .setName(DIALOG_CUSTOM)
                .commit();
    }
}
