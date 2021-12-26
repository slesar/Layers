package com.psliusar.layers

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.psliusar.layers.callbacks.ActivityEventListeners
import com.psliusar.layers.callbacks.OnActivityEventListener

private const val SAVED_STATE_LAYERS = "LAYERS.SAVED_STATE_LAYERS"

/**
 * An [AppCompatActivity] that implements [LayersHost].
 */
abstract class LayersActivity(
    @LayoutRes contentLayoutId: Int = 0
) : AppCompatActivity(contentLayoutId), LayersHost {

    override val layers: Layers
        get() {
            ensureLayerViews()
            return _layers ?: throw IllegalStateException("Layers not initialized yet")
        }

    override val defaultContainer: ViewGroup
        get() = getView(android.R.id.content)

    override val activity: Activity
        get() = this

    override val parentLayer: Layer?
        get() = null

    /**
     * Indicates whether the Activity reached saved state.
     */
    var isInSavedState = false
        private set

    private val activityEventListeners = ActivityEventListeners()
    private var _layers: Layers? = null
    private var layersStateRestored = false

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        layersStateRestored = state != null
        _layers = Layers(this, state?.getBundle(SAVED_STATE_LAYERS))
        activityEventListeners.onCreate(state)
    }

    override fun onRestoreInstanceState(state: Bundle) {
        super.onRestoreInstanceState(state)
        activityEventListeners.onRestoreInstanceState(state)
    }

    override fun onRestart() {
        super.onRestart()
        activityEventListeners.onRestart()
    }

    override fun onStart() {
        super.onStart()
        isInSavedState = false
        ensureLayerViews()
        activityEventListeners.onStart()
    }

    override fun onPostCreate(state: Bundle?) {
        super.onPostCreate(state)
        activityEventListeners.onPostCreate(state)
    }

    override fun onResume() {
        super.onResume()
        activityEventListeners.onResume()
    }

    override fun onPostResume() {
        super.onPostResume()
        activityEventListeners.onPostResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        isInSavedState = true
        if (!isFinishing) {
            layers.saveState()?.let {
                outState.putBundle(SAVED_STATE_LAYERS, it)
            }
        }
        activityEventListeners.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        activityEventListeners.onPause()
    }

    override fun onStop() {
        super.onStop()
        activityEventListeners.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        layers.destroy()
        activityEventListeners.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        activityEventListeners.onNewIntent(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        activityEventListeners.onConfigurationChanged(newConfig)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activityEventListeners.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        activityEventListeners.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        activityEventListeners.onTrimMemory(level)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        activityEventListeners.onLowMemory()
    }

    override fun onBackPressed() {
        if (layers.onBackPressed()) {
            return
        }
        if (layers.stackSize > 1) {
            layers.pop<Layer>()
            return
        }
        super.onBackPressed()
    }

    override fun <T : View> getView(@IdRes viewId: Int): T {
        return findViewById(viewId) ?: throw IllegalArgumentException("View not found")
    }

    /**
     * Adds [OnActivityEventListener].
     */
    fun addEventListener(listener: OnActivityEventListener) {
        activityEventListeners.addListener(listener)
    }

    /**
     * Removes [OnActivityEventListener].
     */
    fun removeEventListener(listener: OnActivityEventListener) {
        activityEventListeners.removeListener(listener)
    }

    /**
     * Ensures that [Layers] has created all needed views in the stack.
     */
    private fun ensureLayerViews() {
        if (layersStateRestored) {
            layersStateRestored = false
            layers.resumeView()
        }
    }
}
