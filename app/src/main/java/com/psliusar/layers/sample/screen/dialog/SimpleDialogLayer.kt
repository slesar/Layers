package com.psliusar.layers.sample.screen.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.psliusar.layers.DialogWrapper
import com.psliusar.layers.Layer

private const val ARGS_TITLE = "ARGS_TITLE"
private const val ARGS_MESSAGE = "ARGS_MESSAGE"

class SimpleDialogLayer : Layer() {

    private val dialogWrapper = object : DialogWrapper(this@SimpleDialogLayer) {

        override fun onCreateDialog(): Dialog {
            val args = arguments ?: throw IllegalArgumentException("Caller must provide arguments")
            return AlertDialog.Builder(activity)
                .setTitle(args.getString(ARGS_TITLE))
                .setMessage(args.getString(ARGS_MESSAGE))
                .setPositiveButton("Great!") { _, _ -> dismiss(false) }
                .create()
        }
    }

    init {
        addDelegate(dialogWrapper)
    }

    companion object {

        fun createArguments(title: String, message: String): Bundle {
            val bundle = Bundle()
            bundle.putString(ARGS_TITLE, title)
            bundle.putString(ARGS_MESSAGE, message)
            return bundle
        }
    }
}
