package com.psliusar.layers.sample.screen.child

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.psliusar.layers.Layer
import com.psliusar.layers.sample.R

class ChildLayer : Layer(R.layout.screen_child) {

    var title: String by savedState()

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)
        getView<TextView>(R.id.child_title).text = title
    }

    fun setParameters(title: String) {
        this.title = title
    }
}
