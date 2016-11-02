package com.psliusar.layers;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public abstract class DialogLayer<P extends Presenter> extends Layer<P>
        implements DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {

    /**
     * Simple, normal dialog.
     */
    public static final int STYLE_NORMAL = 0;

    /**
     * Dialog without title
     */
    public static final int STYLE_NO_TITLE = 1;

    /**
     * Don't use dialog's frame
     */
    public static final int STYLE_NO_FRAME = 2;

    /**
     * Same as {@link DialogLayer#STYLE_NO_FRAME} but without any user interaction available
     */
    public static final int STYLE_NO_INPUT = 3;

    private static final String PRIVATE_ARGS_DIALOG_STYLE = "PRIVATE_DIALOG_STYLE";
    private static final String PRIVATE_ARGS_DIALOG_THEME = "PRIVATE_DIALOG_THEME";
    private static final String PRIVATE_ARGS_DIALOG_CANCELABLE = "PRIVATE_DIALOG_CANCELABLE";

    private Dialog dialog;

    private int style = STYLE_NORMAL;
    private int theme = 0;
    private boolean cancelable = true;
    private boolean notifyOnDismiss = true;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        if (savedState != null) {
            style = savedState.getInt(PRIVATE_ARGS_DIALOG_STYLE, style);
            theme = savedState.getInt(PRIVATE_ARGS_DIALOG_THEME, theme);
            cancelable = savedState.getBoolean(PRIVATE_ARGS_DIALOG_CANCELABLE, cancelable);
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        if (dialog == null) {
            getLayoutInflater();
        }
        showDialog();
    }

    @Override
    void restoreViewState(@Nullable SparseArray<Parcelable> inState) {
        super.restoreViewState(inState);
        if (dialog != null && inState != null) {
            dialog.onRestoreInstanceState((Bundle) inState.get(0));
        }
    }

    @Override
    void saveViewState(@NonNull SparseArray<Parcelable> outState) {
        super.saveViewState(outState);
        if (dialog != null) {
            final Bundle bundle = dialog.onSaveInstanceState();
            if (bundle != null && bundle.size() > 0) {
                outState.put(0, bundle);
            }
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        hideDialog();
    }

    @Override
    protected void onDestroyView() {
        super.onDestroyView();
        dialog = null;
    }

    @Override
    void saveLayerState(@NonNull Bundle outState) {
        super.saveLayerState(outState);
        if (style != STYLE_NORMAL) {
            outState.putInt(PRIVATE_ARGS_DIALOG_STYLE, style);
        }
        if (theme != 0) {
            outState.putInt(PRIVATE_ARGS_DIALOG_THEME, theme);
        }
        if (!cancelable) {
            outState.putBoolean(PRIVATE_ARGS_DIALOG_CANCELABLE, cancelable);
        }
    }

    @Override
    public boolean isViewInLayout() {
        return false;
    }

    @NonNull
    @Override
    protected LayoutInflater getLayoutInflater() {
        dialog = onCreateDialog();
        setupDialog();
        return (LayoutInflater) dialog.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setStyle(int style, @StyleRes int theme) {
        this.style = style;
        if (theme != 0) {
            this.theme = theme;
        } else if (style == STYLE_NO_FRAME || style == STYLE_NO_INPUT) {
            this.theme = android.R.style.Theme_Panel;
        }
    }

    @NonNull
    public Dialog onCreateDialog() {
        return new Dialog(getActivity(), theme);
    }

    @Nullable
    public Dialog getDialog() {
        return dialog;
    }

    protected void setupDialog() {
        switch (style) {
        case STYLE_NO_INPUT:
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        case STYLE_NO_FRAME:
        case STYLE_NO_TITLE:
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
    }

    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        if (dialog != null) {
            dialog.setCancelable(cancelable);
        }
    }

    public boolean isCancelable() {
        return cancelable;
    }

    private void showDialog() {
        notifyOnDismiss = true;
        final View view = getView();
        if (view != null) {
            dialog.setContentView(view);
        }
        dialog.setOwnerActivity(getActivity());
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(this);
        dialog.setOnDismissListener(this);
        dialog.show();
    }

    private void hideDialog() {
        notifyOnDismiss = false;
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (notifyOnDismiss) {
            final OnLayerDialogListener parent = getParent(OnLayerDialogListener.class);
            if (parent != null) {
                parent.onDialogCancel(this);
            }
        }
        if (isAttached()) {
            final LayersHost host = getParent(LayersHost.class);
            if (host != null) {
                host.getLayers().remove(this);
            }
        }
    }

    public void dismiss(boolean notify) {
        notifyOnDismiss = notify;
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public interface OnLayerDialogListener {

        void onDialogCancel(@NonNull DialogLayer<?> dialog);
    }
}
