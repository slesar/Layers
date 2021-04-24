package com.psliusar.layers.sample.screen.listener

import android.app.Activity
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.psliusar.layers.LayersActivity

class ListenerViewModel : ViewModel() {

    private val _photoUri = MutableLiveData<Bitmap>()
    val photoUri: LiveData<Bitmap> = _photoUri

    private val model = ListenerModel()

    fun getPhoto(activity: LayersActivity, updatable: ListenerModel.Updatable<Bitmap>) {
        //manage(model.getPhotoUri(activity, updatable))


    }

    fun takePhotoClick(activity: Activity) {
        model.requestPhoto(activity)
    }
}
