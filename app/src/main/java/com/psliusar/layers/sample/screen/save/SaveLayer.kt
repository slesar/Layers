package com.psliusar.layers.sample.screen.save

import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.psliusar.layers.Layer
import com.psliusar.layers.sample.R

class SaveLayer : Layer() {

    //@Save
    internal var stringList: List<String>? = null
    //@Save
    internal var parcelables: SparseArray<Parcelable>? = null
    //@Save
    internal var parcelablesArray: Array<Rect>? = null
    //@Save(stateManager = CustomFieldStateManager::class)
    internal var customManagerSample: String? = null

    override fun onCreateView(savedState: Bundle?, parent: ViewGroup?): View? {
        return inflate(R.layout.screen_save, parent)
    }

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)
        view.findViewById<TextView>(R.id.save_string_list).text = stringList?.joinToString()
    }

    fun setParameters(vararg items: String) {
        stringList = items.toList()
    }
}
