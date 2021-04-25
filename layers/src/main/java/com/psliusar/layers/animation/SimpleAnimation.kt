package com.psliusar.layers.animation

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.TimeInterpolator
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.annotation.AnimRes

/**
 * Wrapper that uses {@link Animation} to animate View, but behaves like {@link Animator}.
 *
 * When cancel is called, listeners will get both calls -
 * [AnimatorListener.onAnimationCancel] and [AnimatorListener.onAnimationEnd]
 */
class SimpleAnimation(
    /** Target view to apply animation to  */
    private val view: View,
    @AnimRes animResId: Int
) : Animator(), AnimationListener, Interpolator, Handler.Callback {

    /** Animation loaded from resources  */
    private val animation: Animation = AnimationUtils.loadAnimation(view.context, animResId)

    /**
     * Some listener methods should be called in a next frame. This handler will be used to send message
     * to next frame.
     */
    private var uiHandler: Handler? = null

    /** Custom interpolator from animation package  */
    private var _interpolator: TimeInterpolator? = null

    init {
        animation.setAnimationListener(this)
    }

    override fun getStartDelay(): Long = animation.startOffset

    override fun setStartDelay(startDelay: Long) {
        animation.startOffset = startDelay
    }

    override fun getDuration(): Long = animation.duration

    override fun setDuration(duration: Long): Animator {
        animation.duration = duration
        return this
    }

    override fun setInterpolator(value: TimeInterpolator?) {
        _interpolator = value
        animation.interpolator = this
    }

    override fun isRunning(): Boolean {
        return animation.hasStarted() && !animation.hasEnded()
    }

    override fun start() {
        view.startAnimation(animation)
    }

    override fun end() {
        animation.cancel()
    }

    override fun cancel() {
        postEvent(EVENT_CANCEL, true)
        animation.cancel()
    }

    override fun onAnimationStart(animation: Animation?) {
        postEvent(EVENT_START, false)
    }

    override fun onAnimationEnd(animation: Animation?) {
        postEvent(EVENT_FINISH, true)
    }

    override fun onAnimationRepeat(animation: Animation?) {
        postEvent(EVENT_REPEAT, false)
    }

    override fun getInterpolation(input: Float): Float {
        if (_interpolator == null) {
            _interpolator = AccelerateDecelerateInterpolator()
        }
        return _interpolator!!.getInterpolation(input)
    }

    override fun handleMessage(msg: Message): Boolean = when (msg.what) {
        EVENT_START, EVENT_FINISH, EVENT_REPEAT, EVENT_CANCEL -> {
            postEvent(msg.what, false)
            true
        }
        else -> false
    }

    private fun postEvent(eventType: Int, delayed: Boolean) {
        if (delayed) {
            if (uiHandler == null) {
                uiHandler = Handler(Looper.getMainLooper(), this)
            }
            uiHandler!!.sendEmptyMessage(eventType)
        } else {
            listeners?.toList()?.forEach {
                when (eventType) {
                    EVENT_START -> it.onAnimationStart(this)
                    EVENT_FINISH -> it.onAnimationEnd(this)
                    EVENT_REPEAT -> it.onAnimationRepeat(this)
                    EVENT_CANCEL -> it.onAnimationCancel(this)
                }
            }
        }
    }

    companion object {
        /** Listener event type - animation start  */
        private const val EVENT_START = 1

        /** Listener event type - animation finish  */
        private const val EVENT_FINISH = 2

        /** Listener event type - animation repeat  */
        private const val EVENT_REPEAT = 3

        /** Listener event type - animation cancel  */
        private const val EVENT_CANCEL = 4
    }
}