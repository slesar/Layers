package com.psliusar.layers.callbacks

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle

interface OnActivityEventListener {

    enum class Event {

    }

    fun onCreate(state: Bundle?)

    fun onRestoreInstanceState(state: Bundle)

    fun onRestart()

    fun onStart()

    fun onPostCreate(state: Bundle?)

    fun onResume()

    fun onPostResume()

    fun onSaveInstanceState(outState: Bundle)

    fun onPause()

    fun onStop()

    fun onDestroy()

    fun onNewIntent(intent: Intent)

    fun onConfigurationChanged(newConfig: Configuration?)

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)

    fun onTrimMemory(level: Int)

    fun onLowMemory()
}
