package com.psliusar.layers.sample.screen.cards

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.psliusar.layers.Layer
import com.psliusar.layers.sample.R
import com.psliusar.layers.sample.R.id.card_close as cardCloseButton
import com.psliusar.layers.sample.R.id.card_replace as cardReplaceButton
import com.psliusar.layers.sample.R.id.card_title as cardTitle

const val ARG_TITLE = "ARG_TITLE"
const val ARG_COLOR = "ARG_COLOR"

class CardLayer : Layer(R.layout.screen_card) {

    private val title: CharSequence by argument(ARG_TITLE)
    private val color: Int by argument(ARG_COLOR)

    private var Int.text: CharSequence?
        get() = getView<TextView>(this).text
        set(v) {
            getView<TextView>(this).text = v
        }

    private val parent: CardsLayer
        get() = getParent()

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)
        view.background?.setColorFilter(color, PorterDuff.Mode.OVERLAY)

        cardTitle.text = title
        cardCloseButton.setOnClickListener {
            parent.removeLayer(this)
        }
        cardReplaceButton.setOnClickListener {
            parent.replaceLayer(this)
        }
    }

    private fun Int.setOnClickListener(listener: View.OnClickListener) {
        getView<View>(this).setOnClickListener(listener)
    }
}