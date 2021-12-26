package com.psliusar.layers.sample.screen.cards

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import com.psliusar.layers.Layer
import com.psliusar.layers.Layers
import com.psliusar.layers.Transition
import com.psliusar.layers.sample.R
import kotlin.math.ceil

class CardsLayer : Layer(R.layout.screen_cards) {

    private val colors = listOf(
        0xfff44336.toInt(),
        0xffe91e63.toInt(),
        0xff9c27b0.toInt(),
        0xff673ab7.toInt(),
        0xff3f51b5.toInt(),
        0xff2196f3.toInt(),
        0xff00bcd4.toInt(),
        0xff009688.toInt(),
        0xff4caf50.toInt(),
        0xff8bc34a.toInt(),
        0xffcddc39.toInt(),
        0xffffeb3b.toInt(),
        0xffffc107.toInt(),
        0xffff9800.toInt(),
        0xffff5722.toInt(),
    )

    private val cards: Layers
        get() = layers.at(R.id.cards)

    private var counter: Int? by savedState()

    private var animationCheckBox: CheckBox? = null

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)

        getView<View>(R.id.cards_add_bottom).setOnClickListener {
            addCard(0)
        }

        getView<View>(R.id.cards_add_middle).setOnClickListener {
            val size = cards.stackSize
            addCard(ceil(size / 2.0).toInt())
        }

        getView<View>(R.id.cards_add_top).setOnClickListener {
            addCard(-1)
        }

        animationCheckBox = getView(R.id.cards_animation)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        animationCheckBox = null
    }

    fun removeLayer(layer: Layer) {
        cards.remove(layer) {
            isAnimationEnabled = animationCheckBox?.isChecked == true
        }
    }

    fun replaceLayer(layer: Layer) {
        val index = cards.indexOf(layer)
        if (index != -1) {
            cards.replace<CardLayer> {
                setup(index)
                setInAnimation(R.anim.upper_out, R.anim.upper_in)
                setOutAnimation(R.anim.lower_out, 0)
            }
        }
    }

    private fun addCard(index: Int) {
        cards.add<CardLayer> {
            setup(index)
        }
    }

    private fun Transition<*>.setup(index: Int) {
        this.index = index
        counter = (counter ?: 0) + 1
        name = "Card #$counter"
        opaque = false
        setInAnimation(0, R.anim.upper_in)
        setOutAnimation(R.anim.upper_out, 0)
        isAnimationEnabled = animationCheckBox?.isChecked == true
        arguments = Bundle().apply {
            putCharSequence(ARG_TITLE, name)
            putInt(ARG_COLOR, colors.random())
        }
    }
}