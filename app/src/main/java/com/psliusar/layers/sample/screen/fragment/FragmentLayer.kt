package com.psliusar.layers.sample.screen.fragment

import android.os.Bundle
import android.view.View
import com.psliusar.layers.LayersFragment
import com.psliusar.layers.sample.R
import com.psliusar.layers.sample.screen.child.ChildLayer

/**
 * Logic is copied from [com.psliusar.layers.sample.screen.child.ChildrenContainerLayer].
 */
class FragmentLayer : LayersFragment(R.layout.screen_children_container) {

    override val defaultContainerId: Int = R.id.children_container

    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state)
        if (state == null) {
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

        getView<View>(R.id.children_container_add_layer).setOnClickListener {
            val number = layers.stackSize + 1
            layers.add<ChildLayer> {
                withLayer { setParameters("Stack layer $number") }
                name = "Stack #$number"
                opaque = false
            }
        }
    }
}
