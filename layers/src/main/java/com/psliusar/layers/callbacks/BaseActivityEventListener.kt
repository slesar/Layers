package com.psliusar.layers.callbacks

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle

abstract class BaseActivityEventListener : OnActivityEventListener {

    override fun onCreate(state: Bundle?) {}

    override fun onRestoreInstanceState(state: Bundle) {}

    override fun onRestart() {}

    override fun onStart() {}

    override fun onPostCreate(state: Bundle?) {}

    override fun onResume() {}

    override fun onPostResume() {}

    override fun onSaveInstanceState(outState: Bundle) {}

    override fun onPause() {}

    override fun onStop() {}

    override fun onDestroy() {}

    override fun onNewIntent(intent: Intent) {}

    override fun onConfigurationChanged(newConfig: Configuration?) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean = false

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {}

    override fun onTrimMemory(level: Int) {}

    override fun onLowMemory() {}
}
