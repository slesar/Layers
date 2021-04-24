package com.psliusar.layers.sample.screen.listener

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.psliusar.layers.Layer
import com.psliusar.layers.LayersActivity
import com.psliusar.layers.callbacks.BaseActivityEventListener
import com.psliusar.layers.sample.R

private const val CAMERA_IMAGE = 1001

class ListenerLayer : Layer() {

    private lateinit var viewModel: ListenerViewModel

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        viewModel = getViewModel()
    }

    override fun onCreateView(savedState: Bundle?, parent: ViewGroup?): View? = inflate(R.layout.screen_listener, parent)

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)

        viewModel.photoUri.observe(this, {
            view.findViewById<ImageView>(R.id.listener_image).setImageBitmap(it)
        })

        view.findViewById<View>(R.id.listener_take_photo).setOnClickListener {
            //viewModel.takePhotoClick(activity)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivityForResult(intent, CAMERA_IMAGE)
            }
        }

        (activity as LayersActivity).activityEventListeners.addListener(object : BaseActivityEventListener() {
            override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
                if (requestCode == CAMERA_IMAGE) {
                    if (resultCode == Activity.RESULT_OK) {
                        val image = intent?.extras?.get("data") as Bitmap?
                        view.findViewById<ImageView>(R.id.listener_image).setImageBitmap(image)
                    }
                    return true
                }
                return false
            }
        })
    }
}
