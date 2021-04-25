package com.psliusar.layers.sample.screen.dialog

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.psliusar.layers.DialogWrapper
import com.psliusar.layers.Layer
import com.psliusar.layers.sample.R

private const val ARGS_TITLE = "ARGS_TITLE"

class CustomDialogLayer : Layer(R.layout.screen_dialog_custom) {

    private val dialogWrapper = DialogWrapper(this).apply {
        cancelable = false
    }

    init {
        addDelegate(dialogWrapper)
    }

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)
        view.findViewById<TextView>(R.id.dialog_title).text = arguments!!.getString(ARGS_TITLE)
        view.findViewById<View>(R.id.dialog_action1).setOnClickListener {
            getParent<OnCustomDialogListener>().onDialogAction1(this)
            dialogWrapper.dismiss(false)
        }
        view.findViewById<View>(R.id.dialog_action2).setOnClickListener {
            getParent<OnCustomDialogListener>().onDialogAction2(this)
            dialogWrapper.dismiss(false)
        }
    }

    interface OnCustomDialogListener : DialogWrapper.OnLayerDialogListener {

        fun onDialogAction1(dialog: CustomDialogLayer)

        fun onDialogAction2(dialog: CustomDialogLayer)
    }

    companion object {

        fun createArguments(title: String): Bundle {
            val bundle = Bundle()
            bundle.putString(ARGS_TITLE, title)
            return bundle
        }
    }
}
