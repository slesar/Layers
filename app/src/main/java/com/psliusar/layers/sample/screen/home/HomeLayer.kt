package com.psliusar.layers.sample.screen.home

import android.os.Bundle
import android.view.View
import com.psliusar.layers.Layer
import com.psliusar.layers.sample.MainActivity
import com.psliusar.layers.sample.R

class HomeLayer : Layer(R.layout.screen_home) {

    private val mainActivity: MainActivity
        get() = activity as MainActivity

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)
        view.findViewById<View>(R.id.home_stack).setOnClickListener {
            mainActivity.addToStack("Level", 1, true)
        }
        view.findViewById<View>(R.id.home_children).setOnClickListener {
            mainActivity.showChildrenLayers()
        }
        view.findViewById<View>(R.id.home_dialog).setOnClickListener {
            mainActivity.showDialogLayers()
        }
        view.findViewById<View>(R.id.home_activity_listener).setOnClickListener {
            mainActivity.showActivityListener()
        }
        view.findViewById<View>(R.id.home_save_annotation).setOnClickListener {
            mainActivity.showSaveState()
        }
        view.findViewById<View>(R.id.home_fragment).setOnClickListener {
            mainActivity.showFragment()
        }
    }
}
