package com.psliusar.layers.sample.screen.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.psliusar.layers.DialogLayer;
import com.psliusar.layers.ViewModel;

public class SimpleDialogLayer extends DialogLayer<ViewModel<?>> {

    private static final String ARGS_TITLE = "ARGS_TITLE";
    private static final String ARGS_MESSAGE = "ARGS_MESSAGE";

    public static Bundle createArguments(String title, String message) {
        final Bundle bundle = new Bundle();
        bundle.putString(ARGS_TITLE, title);
        bundle.putString(ARGS_MESSAGE, message);
        return bundle;
    }

    @Override
    protected ViewModel<?> onCreateViewModel() {
        return null;
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog() {
        Bundle args = getArguments();
        if (args == null) {
            throw new IllegalArgumentException("Caller must provide arguments");
        }
        return new AlertDialog.Builder(getActivity())
                .setTitle(args.getString(ARGS_TITLE))
                .setMessage(args.getString(ARGS_MESSAGE))
                .setPositiveButton("Great!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss(false);
                    }
                })
                .create();
    }
}
