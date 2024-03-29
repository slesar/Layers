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
        getView<View>(R.id.home_stack).setOnClickListener {
            mainActivity.addToStack("Level", 1, true)
        }
        getView<View>(R.id.home_cards).setOnClickListener {
            mainActivity.showCards()
        }
        getView<View>(R.id.home_children).setOnClickListener {
            mainActivity.showChildrenLayers()
        }
        getView<View>(R.id.home_dialog).setOnClickListener {
            mainActivity.showDialogLayers()
        }
        getView<View>(R.id.home_activity_listener).setOnClickListener {
            mainActivity.showActivityListener()
        }
        getView<View>(R.id.home_save_annotation).setOnClickListener {
            mainActivity.showSaveState()
        }
        getView<View>(R.id.home_fragment).setOnClickListener {
            mainActivity.showFragment()
        }
    }
}
