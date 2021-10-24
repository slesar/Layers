package com.psliusar.layers

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.StyleRes

private const val PRIVATE_ARGS_DIALOG_STYLE = "DIALOG_LAYER.PRIVATE_DIALOG_STYLE"
private const val PRIVATE_ARGS_DIALOG_THEME = "DIALOG_LAYER.PRIVATE_DIALOG_THEME"
private const val PRIVATE_ARGS_DIALOG_CANCELABLE = "DIALOG_LAYER.PRIVATE_DIALOG_CANCELABLE"

/**
 * A delegate that makes [Layer] behave like a dialog.
 * Should be added to Layer in its constructor to work properly.
 */
open class DialogWrapper(
    val layer: Layer
) : LayerDelegate, DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {

    /**
     * Callback interface.
     */
    interface OnLayerDialogListener {

        /**
         * Called when a dialog gets dismissed.
         */
        fun onDialogCancel(wrapper: DialogWrapper)
    }

    companion object {
        /** Simple, normal dialog */
        const val STYLE_NORMAL = 0

        /** Dialog without title */
        const val STYLE_NO_TITLE = 1

        /** Don't use dialog's frame */
        const val STYLE_NO_FRAME = 2

        /** Same as [DialogWrapper.STYLE_NO_FRAME] but without any user interaction available */
        const val STYLE_NO_INPUT = 3
    }

    override val isViewInLayout: Boolean = false // Do not place the view in layout

    override val layoutInflater: LayoutInflater?
        get() = layer.activity.layoutInflater

    /**
     * An actual instance of dialog.
     */
    var dialog: Dialog? = null
        private set

    /**
     * Custom theme resource for the dialog.
     */
    @StyleRes
    var themeResId = 0
        private set

    /**
     * Flag that makes the dialog cancellable by user. When the dialog is not cancellable the user
     * should choose an action that will dismiss the dialog.
     */
    var cancelable = true
        set(v) {
            field = v
            dialog?.setCancelable(v)
        }

    private var style = STYLE_NORMAL
    private var notifyOnDismiss = true

    override fun onCreate(savedState: Bundle?) {
        savedState?.let {
            style = it.getInt(PRIVATE_ARGS_DIALOG_STYLE, style)
            themeResId = it.getInt(PRIVATE_ARGS_DIALOG_THEME, themeResId)
            cancelable = it.getBoolean(PRIVATE_ARGS_DIALOG_CANCELABLE, cancelable)
        }
    }

    override fun onAttach() {
        if (dialog == null) {
            dialog = onCreateDialog()
            onDialogCreated()
        }
        showDialog()
    }

    override fun onBindView(savedState: Bundle?, view: View) {
        // NO-OP
    }

    override fun restoreViewState(inState: SparseArray<Parcelable>?) {
        inState?.let {
            dialog?.onRestoreInstanceState(it.get(0) as Bundle)
        }
    }

    override fun saveViewState(outState: SparseArray<Parcelable>) {
        dialog?.onSaveInstanceState()?.takeIf { it.size() > 0 }?.let {
            outState.put(0, it)
        }
    }

    override fun onDetach() {
        hideDialog()
    }

    override fun onDestroyView() {
        notifyOnDismiss = false
        dialog?.dismiss()
        dialog = null
    }

    override fun onDismiss() {
        // Callback from LayerDelegate. Nothing to do.
    }

    override fun saveLayerState(outState: Bundle) {
        if (style != STYLE_NORMAL) {
            outState.putInt(PRIVATE_ARGS_DIALOG_STYLE, style)
        }
        if (themeResId != 0) {
            outState.putInt(PRIVATE_ARGS_DIALOG_THEME, themeResId)
        }
        if (!cancelable) {
            outState.putBoolean(PRIVATE_ARGS_DIALOG_CANCELABLE, false)
        }
    }

    /**
     * Sets the custom style and theme.
     */
    fun setStyle(style: Int, @StyleRes theme: Int) {
        this.style = style
        if (theme != 0) {
            this.themeResId = theme
        } else if (style == STYLE_NO_FRAME || style == STYLE_NO_INPUT) {
            this.themeResId = android.R.style.Theme_Panel
        }
    }

    /**
     * Creates an instance of dialog. May be overridden by subclasses.
     */
    open fun onCreateDialog(): Dialog = Dialog(layer.activity, themeResId)

    /**
     * Called when the dialog is created.
     */
    open fun onDialogCreated() {
        val dialog = dialog ?: return
        when (style) {
            STYLE_NO_INPUT -> {
                dialog.window?.addFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            }
            STYLE_NO_FRAME, STYLE_NO_TITLE -> dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
    }

    private fun showDialog() {
        notifyOnDismiss = true
        val dialog = dialog ?: return
        layer.view?.let(dialog::setContentView)
        dialog.setOwnerActivity(layer.activity)
        dialog.setCancelable(cancelable)
        dialog.setOnCancelListener(this)
        dialog.setOnDismissListener(this)
        dialog.show()
    }

    private fun hideDialog() {
        notifyOnDismiss = false
        dialog?.dismiss()
    }

    override fun onCancel(dialog: DialogInterface) {

    }

    override fun onDismiss(dialog: DialogInterface) {
        if (notifyOnDismiss) {
            layer.optParent(OnLayerDialogListener::class.java)?.onDialogCancel(this)
        }
        layer.dismiss()
    }

    /**
     * Dismisses the dialog and removes Layer from the stack.
     */
    fun dismiss(notify: Boolean) {
        notifyOnDismiss = notify
        dialog?.takeIf { it.isShowing }?.dismiss()
    }
}