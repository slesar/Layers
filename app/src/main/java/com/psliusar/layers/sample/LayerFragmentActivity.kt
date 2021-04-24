package com.psliusar.layers.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.psliusar.layers.sample.screen.fragment.FragmentLayer

class LayerFragmentActivity : AppCompatActivity() {

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_main)
        if (state == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, FragmentLayer(), null).commit()
        }
    }
}
