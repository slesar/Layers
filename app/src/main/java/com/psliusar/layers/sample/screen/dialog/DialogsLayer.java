package com.psliusar.layers.sample.screen.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.psliusar.layers.Layer;
import com.psliusar.layers.sample.R;

public class DialogsLayer extends Layer<DialogsPresenter> implements View.OnClickListener {
    @Nullable
    @Override
    protected View onCreateView(@NonNull ViewGroup parent) {
        return inflate(R.layout.screen_dialogs, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        onClick(this, R.id.dialogs_simple, R.id.dialogs_custom);
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

    void showSimpleDialog(String title, String message) {
        Bundle args = SimpleDialogLayer.createArguments(title, message);
        getLayers().add(SimpleDialogLayer.class, args, "SimpleDialog", false);
    }
}
