package com.psliusar.layers.sample.screen.save

import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.widget.TextView
import com.psliusar.layers.Layer
import com.psliusar.layers.sample.R

class SaveLayer : Layer(R.layout.screen_save) {

    private var stringList: List<String>? by savedState()
    private var parcelables: SparseArray<Parcelable>? = null
    private var parcelablesArray: Array<Rect>? = null

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)
        view.findViewById<TextView>(R.id.save_string_list).text = stringList?.joinToString()
    }

    fun setParameters(vararg items: String) {
        stringList = items.toList()
    }
}
