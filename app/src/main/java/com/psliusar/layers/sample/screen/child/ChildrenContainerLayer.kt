package com.psliusar.layers.sample.screen.child

import android.os.Bundle
import android.view.View
import com.psliusar.layers.Layer
import com.psliusar.layers.sample.R

class ChildrenContainerLayer : Layer(R.layout.screen_children_container) {

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)
        if (!isFromSavedState) {
            layers.at(R.id.children_container_top)
                .add<ChildLayer> {
                    withLayer { setParameters("Top layer") }
                    name = "Top"
                }

            layers.at(R.id.children_container_middle)
                .add<ChildLayer> {
                    withLayer { setParameters("Middle layer") }
                    name = "Middle"
                }

            layers.at(R.id.children_container_bottom)
                .add<ChildLayer> {
                    withLayer { setParameters("Bottom layer") }
                    name = "Bottom"
                }
        }

        view.findViewById<View>(R.id.children_container_add_layer).setOnClickListener {
            val number = layers.stackSize + 1
            layers
                .add<ChildLayer> {
                    withLayer { setParameters("Stack layer $number") }
                    name = "Stack #$number"
                    opaque = false
                }
        }
    }
}
