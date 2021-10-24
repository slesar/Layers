package com.psliusar.layers.state

import androidx.lifecycle.ViewModelStore
import com.psliusar.layers.Layer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Reads and writes [ViewModelStore] into the state bundle of the [Layer].
 */
internal class ViewModelStoreState :
    StateWrapper<ViewModelStore>(ViewModelStore::class.java),
    ReadWriteProperty<Layer, ViewModelStore?> {

    override fun getValue(thisRef: Layer, property: KProperty<*>): ViewModelStore? {
        val bundle = thisRef.state
        return getValue(bundle, property.name)
    }

    override fun setValue(thisRef: Layer, property: KProperty<*>, value: ViewModelStore?) {
        val bundle = thisRef.state
        saveValue(bundle, property.name, value)
    }
}