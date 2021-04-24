package com.psliusar.layers.sample.screen.stack

import androidx.lifecycle.ViewModel

class StackViewModel : ViewModel() {

    fun getStackLevelText(title: CharSequence, level: Int): CharSequence {
        return String.format("%s: %s", title, level)
    }
}
