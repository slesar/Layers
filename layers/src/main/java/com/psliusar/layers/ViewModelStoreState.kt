package com.psliusar.layers

import android.os.Bundle
import androidx.lifecycle.ViewModelStore
import com.psliusar.layers.binder.StateWrapper
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ViewModelStoreState : StateWrapper<ViewModelStore>(ViewModelStore::class.java), ReadWriteProperty<Layer, ViewModelStore?> {

    override fun getValue(thisRef: Layer, property: KProperty<*>): ViewModelStore? {
        val bundle = thisRef.stateOrArguments ?: Bundle.EMPTY
        return getValue(bundle, property.name)
    }

    override fun setValue(thisRef: Layer, property: KProperty<*>, value: ViewModelStore?) {
        val bundle = thisRef.state
        saveValue(bundle, property.name, value)
    }
}