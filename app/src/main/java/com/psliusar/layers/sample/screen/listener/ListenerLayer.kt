package com.psliusar.layers.sample.screen.listener

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import com.psliusar.layers.Layer
import com.psliusar.layers.LayersActivity
import com.psliusar.layers.callbacks.BaseActivityEventListener
import com.psliusar.layers.sample.R

private const val CAMERA_IMAGE = 1001

class ListenerLayer : Layer(R.layout.screen_listener) {

    private val imageListener = object : BaseActivityEventListener() {
        override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
            if (requestCode == CAMERA_IMAGE) {
                if (resultCode == Activity.RESULT_OK) {
                    val image = intent?.extras?.get("data") as Bitmap?
                    getView<ImageView>(R.id.listener_image).setImageBitmap(image)
                }
                return true
            }
            return false
        }
    }

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)

        getView<View>(R.id.listener_take_photo).setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivityForResult(intent, CAMERA_IMAGE)
            }
        }
        (activity as LayersActivity).addEventListener(imageListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as LayersActivity).removeEventListener(imageListener)
    }
}
