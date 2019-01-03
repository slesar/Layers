package com.psliusar.layers.sample;

import android.os.Bundle;

import com.psliusar.layers.sample.screen.fragment.FragmentLayer;

import androidx.fragment.app.FragmentActivity;

public class LayerFragmentActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        if (state == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new FragmentLayer(), null).commit();
        }
    }
}
