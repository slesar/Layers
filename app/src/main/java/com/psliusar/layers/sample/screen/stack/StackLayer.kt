package com.psliusar.layers.sample.screen.stack

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.psliusar.layers.Layer
import com.psliusar.layers.sample.MainActivity
import com.psliusar.layers.sample.R

class StackLayer : Layer(R.layout.screen_stack) {

    private var title: CharSequence? by savedState()
    private var level: Int? by savedState()

    private lateinit var viewModel: StackViewModel

    private val nextLayerTitle: CharSequence
        get() = getView<TextView>(R.id.stack_next_title).text

    private val isNextOpaque: Boolean
        get() = getView<CheckBox>(R.id.stack_next_opaque).isChecked

    private lateinit var mainActivity: MainActivity

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        mainActivity = getParent()
        viewModel = getViewModel()
    }

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)
        getView<View>(R.id.stack_add).setOnClickListener {
            mainActivity.addToStack(
                nextLayerTitle,
                level!! + 1,
                isNextOpaque
            )
        }

        getView<View>(R.id.stack_replace).setOnClickListener {
            mainActivity.replaceInStack(
                nextLayerTitle,
                level!! + 1,
                isNextOpaque
            )
        }

        title?.let {
            getView<TextView>(R.id.stack_level).text = viewModel.getStackLevelText(it, level!!)
        }
    }

    fun setParameters(title: CharSequence, level: Int) {
        this.title = title
        this.level = level
    }
}
