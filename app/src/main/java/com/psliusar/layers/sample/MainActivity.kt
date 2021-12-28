package com.psliusar.layers.sample

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.psliusar.layers.Layer
import com.psliusar.layers.Layers
import com.psliusar.layers.LayersActivity
import com.psliusar.layers.Transition
import com.psliusar.layers.sample.screen.cards.CardsLayer
import com.psliusar.layers.sample.screen.child.ChildrenContainerLayer
import com.psliusar.layers.sample.screen.dialog.DialogsLayer
import com.psliusar.layers.sample.screen.home.HomeLayer
import com.psliusar.layers.sample.screen.listener.ListenerLayer
import com.psliusar.layers.sample.screen.save.SaveLayer
import com.psliusar.layers.sample.screen.stack.StackLayer

class MainActivity : LayersActivity(R.layout.activity_main) {

    override val defaultContainer: ViewGroup
        get() = getView(R.id.container)

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val toolbar = getView<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        if (state == null) {
            layers.add<HomeLayer> {
                name = resources.getString(R.string.app_name)
            }
        }

        layers.addLayersEventListener(object : Layers.LayersEventListener {
            override fun onTransitionStarted(layers: Layers, transition: Transition<*>) {

            }

            override fun onTransitionFinished(layers: Layers, transition: Transition<*>) {

            }

            override fun onStackChanged(layers: Layers) {
                updateToolbar()
            }
        })
        updateToolbar()
    }

    fun addToStack(title: CharSequence, level: Int, opaque: Boolean) {
        layers.add<StackLayer> {
            withLayer { setParameters(title, level) }
            name = "Layer $level in Stack"
            this.opaque = opaque
            if (opaque) {
                setInAnimation(R.anim.lower_out, R.anim.upper_in)
                setOutAnimation(R.anim.upper_out, R.anim.lower_in)
            } else {
                setInAnimation(0, R.anim.upper_in)
                setOutAnimation(R.anim.upper_out, 0)
            }
        }
    }

    fun replaceInStack(title: CharSequence, level: Int, opaque: Boolean) {
        layers.replace<StackLayer> {
            withLayer { setParameters(title, level) }
            name = "Layer $level in Stack"
            this.opaque = opaque
            setInAnimation(R.anim.lower_out, R.anim.upper_in)
            setOutAnimation(R.anim.upper_out, R.anim.lower_in)
        }
    }

    fun showCards() {
        layers.add<CardsLayer> {
            name = "Cards Demo"
        }
    }

    fun showChildrenLayers() {
        layers.add<ChildrenContainerLayer> {
            name = "Children Layers Demo"
        }
    }

    fun showDialogLayers() {
        layers.add<DialogsLayer> {
            name = "Dialogs Layers Demo"
        }
    }

    fun showActivityListener() {
        layers.add<ListenerLayer> {
            name = "Activity Lifecycle Demo"
        }
    }

    fun showSaveState() {
        layers.add<SaveLayer> {
            withLayer { setParameters("First", "Second", "Third") }
            name = "State Saving Demo"
        }
    }

    fun showFragment() {
        startActivity(Intent(this, LayerFragmentActivity::class.java))
    }

    private fun updateToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(layers.stackSize > 1)
            title = layers.peek<Layer>()?.name
        }
    }
}
