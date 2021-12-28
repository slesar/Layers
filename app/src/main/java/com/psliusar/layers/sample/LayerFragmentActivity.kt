package com.psliusar.layers.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.psliusar.layers.sample.screen.fragment.FragmentLayer

class LayerFragmentActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (state == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, FragmentLayer(), null).commit()
        }
    }
}
