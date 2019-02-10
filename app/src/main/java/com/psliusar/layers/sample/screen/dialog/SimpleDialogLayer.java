package com.psliusar.layers.sample.screen.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.psliusar.layers.DialogWrapper;
import com.psliusar.layers.Layer;
import com.psliusar.layers.ViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SimpleDialogLayer extends Layer<ViewModel> {

    private static final String ARGS_TITLE = "ARGS_TITLE";
    private static final String ARGS_MESSAGE = "ARGS_MESSAGE";

    public static Bundle createArguments(String title, String message) {
        final Bundle bundle = new Bundle();
        bundle.putString(ARGS_TITLE, title);
        bundle.putString(ARGS_MESSAGE, message);
        return bundle;
    }

    private final DialogWrapper wrapper = new DialogWrapper(this) {

        @NonNull
        @Override
        public Dialog onCreateDialog() {
            final Bundle args = getArguments();
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
    };

    @Override
    protected ViewModel onCreateViewModel() {
        return null;
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable Bundle savedState, @Nullable ViewGroup parent) {
        return null;
    }
}
