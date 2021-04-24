package com.psliusar.layers.callbacks

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle

class ActivityEventListeners : OnActivityEventListener {

    private val listeners = mutableListOf<OnActivityEventListener>()

    fun addListener(listener: OnActivityEventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: OnActivityEventListener) {
        listeners.remove(listener)
    }

    override fun onCreate(state: Bundle?) {
        listeners.forEach {
            it.onCreate(state)
        }
    }

    override fun onRestoreInstanceState(state: Bundle) {
        listeners.forEach {
            it.onRestoreInstanceState(state)
        }
    }

    override fun onRestart() {
        listeners.forEach {
            it.onRestart()
        }
    }

    override fun onStart() {
        listeners.forEach {
            it.onStart()
        }
    }

    override fun onPostCreate(state: Bundle?) {
        listeners.forEach {
            it.onPostCreate(state)
        }
    }

    override fun onResume() {
        listeners.forEach {
            it.onResume()
        }
    }

    override fun onPostResume() {
        listeners.forEach {
            it.onPostResume()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        listeners.forEach {
            it.onSaveInstanceState(outState)
        }
    }

    override fun onPause() {
        listeners.forEach {
            it.onPause()
        }
    }

    override fun onStop() {
        listeners.forEach {
            it.onStop()
        }
    }

    override fun onDestroy() {
        listeners.forEach {
            it.onDestroy()
        }
    }

    override fun onNewIntent(intent: Intent) {
        listeners.forEach {
            it.onNewIntent(intent)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        listeners.forEach {
            it.onConfigurationChanged(newConfig)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        return listeners.any {
            it.onActivityResult(requestCode, resultCode, intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        listeners.forEach {
            it.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onTrimMemory(level: Int) {
        listeners.forEach {
            it.onTrimMemory(level)
        }
    }

    override fun onLowMemory() {
        listeners.forEach {
            it.onLowMemory()
        }
    }
}