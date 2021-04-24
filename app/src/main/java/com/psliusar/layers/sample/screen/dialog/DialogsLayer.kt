package com.psliusar.layers.sample.screen.dialog

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.psliusar.layers.DialogWrapper
import com.psliusar.layers.Layer
import com.psliusar.layers.sample.R

private const val DIALOG_SIMPLE = "SimpleDialog"
private const val DIALOG_CUSTOM = "CustomDialog"

class DialogsLayer : Layer(), CustomDialogLayer.OnCustomDialogListener {

    override fun onCreateView(savedState: Bundle?, parent: ViewGroup?): View? {
        return inflate(R.layout.screen_dialogs, parent)
    }

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)
        view.findViewById<View>(R.id.dialogs_simple).setOnClickListener {
            showSimpleDialog("Hello World!", "This is simple AlertDialog controlled via Layer")
        }
        view.findViewById<View>(R.id.dialogs_custom).setOnClickListener {
            showCustomDialog("Custom dialog")
        }
    }

    override fun onDialogAction1(dialog: CustomDialogLayer) {
        if (dialog.name == DIALOG_CUSTOM) {
            Toast.makeText(context, "Action 1 was selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDialogAction2(dialog: CustomDialogLayer) {
        if (dialog.name == DIALOG_CUSTOM) {
            Toast.makeText(context, "Action 2 was selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDialogCancel(wrapper: DialogWrapper) {
        Toast.makeText(context, "Dialog $DIALOG_SIMPLE was dismissed", Toast.LENGTH_SHORT).show()
    }

    private fun showSimpleDialog(title: String, message: String) {
        layers.add<SimpleDialogLayer> {
            arguments = SimpleDialogLayer.createArguments(title, message)
            name = DIALOG_SIMPLE
        }
    }

    private fun showCustomDialog(title: String) {
        layers.add<CustomDialogLayer> {
            arguments = CustomDialogLayer.createArguments(title)
            name = DIALOG_CUSTOM
        }
    }
}
