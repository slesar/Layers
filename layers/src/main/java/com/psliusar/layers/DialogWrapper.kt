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

open class DialogWrapper(
    val layer: Layer
) : LayerDelegate, DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {

    interface OnLayerDialogListener {

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

    override val isViewInLayout: Boolean = false

    var dialog: Dialog? = null

    var theme = 0
        private set

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
            theme = it.getInt(PRIVATE_ARGS_DIALOG_THEME, theme)
            cancelable = it.getBoolean(PRIVATE_ARGS_DIALOG_CANCELABLE, cancelable)
        }
    }

    override fun onAttach() {
        if (dialog == null) {
            // Init Layout Inflater
            layoutInflater
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
        dialog = null
    }

    override fun onDismiss() {
        // Callback from LayerDelegate. Nothing to do.
    }

    override fun saveLayerState(outState: Bundle) {
        if (style != STYLE_NORMAL) {
            outState.putInt(PRIVATE_ARGS_DIALOG_STYLE, style)
        }
        if (theme != 0) {
            outState.putInt(PRIVATE_ARGS_DIALOG_THEME, theme)
        }
        if (!cancelable) {
            outState.putBoolean(PRIVATE_ARGS_DIALOG_CANCELABLE, false)
        }
    }

    override val layoutInflater: LayoutInflater?
        get() {
            if (dialog == null) {
                dialog = onCreateDialog()
                setupDialog()
            }
            return LayoutInflater.from(layer.activity)
        }

    fun setStyle(style: Int, @StyleRes theme: Int) {
        this.style = style
        if (theme != 0) {
            this.theme = theme
        } else if (style == STYLE_NO_FRAME || style == STYLE_NO_INPUT) {
            this.theme = android.R.style.Theme_Panel
        }
    }

    open fun onCreateDialog(): Dialog = Dialog(layer.activity, theme)

    open fun setupDialog() {
        val dialog = dialog ?: return
        when (style) {
            STYLE_NO_INPUT -> {
                dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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

    fun dismiss(notify: Boolean) {
        notifyOnDismiss = notify
        dialog?.takeIf { it.isShowing }?.dismiss()
    }
}