package com.psliusar.layers

import android.content.Context
import android.view.View
import android.view.ViewGroup

class MockedViewGroup(context: Context) : ViewGroup(context) {

    private var childCount = 0

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}

    override fun setSaveFromParentEnabled(enabled: Boolean) {}

    override fun getChildCount(): Int = childCount

    override fun addView(child: View, index: Int) {
        childCount++
    }

    override fun removeView(view: View) {
        childCount--
    }
}
