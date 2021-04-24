package com.psliusar.layers.sample

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import com.psliusar.layers.LayersActivity
import com.psliusar.layers.sample.screen.child.ChildrenContainerLayer
import com.psliusar.layers.sample.screen.dialog.DialogsLayer
import com.psliusar.layers.sample.screen.home.HomeLayer
import com.psliusar.layers.sample.screen.listener.ListenerLayer
import com.psliusar.layers.sample.screen.save.SaveLayer
import com.psliusar.layers.sample.screen.stack.StackLayer

class MainActivity : LayersActivity() {

    override val defaultContainer: ViewGroup
        get() = getView(R.id.container)

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_main)
        if (state == null) {
            layers.add<HomeLayer> {
                name = "Home"
            }
        }
    }

    fun addToStack(title: CharSequence, level: Int, opaque: Boolean) {
        layers.add<StackLayer> {
            withLayer { setParameters(title, level) }
            name = "Stack$level"
            this.opaque = opaque
            setInAnimation(R.anim.lower_out, R.anim.upper_in)
            setOutAnimation(R.anim.upper_out, R.anim.lower_in)
        }
    }

    fun replaceInStack(title: CharSequence, level: Int, opaque: Boolean) {
        layers.replace<StackLayer> {
            withLayer { setParameters(title, level) }
            name = "Stack$level"
            this.opaque = opaque
            setInAnimation(R.anim.lower_out, R.anim.upper_in)
            setOutAnimation(R.anim.upper_out, R.anim.lower_in)
        }
    }

    fun showChildrenLayers() {
        layers.add<ChildrenContainerLayer> {
            name = "Children"
        }
    }

    fun showDialogLayers() {
        layers.add<DialogsLayer> {
            name = "Dialogs"
        }
    }

    fun showActivityListener() {
        layers.add<ListenerLayer> {
            name = "Listener"
        }
    }

    fun showSaveState() {
        layers.add<SaveLayer> {
            withLayer { setParameters("First", "Second", "Third") }
            name = "Save"
        }
    }

    fun showFragment() {
        startActivity(Intent(this, LayerFragmentActivity::class.java))
    }
}
