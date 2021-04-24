package com.psliusar.layers.sample.screen.child

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.psliusar.layers.Layer
import com.psliusar.layers.sample.R

class ChildLayer : Layer() {

    //@field:Save
    //@JvmField
    var title: String = ""

    override fun onCreateView(savedState: Bundle?, parent: ViewGroup?): View? = inflate(R.layout.screen_child, parent)

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)
        view.findViewById<TextView>(R.id.child_title).text = title
    }

    fun setParameters(title: String) {
        this.title = title
    }
}
