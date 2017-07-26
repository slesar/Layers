package com.psliusar.layers.animation;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.AnimRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import java.util.ArrayList;

/**
 * Wrapper that uses {@link Animation} to animate View, but behaves like {@link Animator}.
 *
 * <p>
 *     When cancel is called, listeners will get both calls -
 *     {@link AnimatorListener#onAnimationCancel(android.animation.Animator)} and
 *     {@link AnimatorListener#onAnimationEnd(android.animation.Animator)}
 * </p>
 */
public class SimpleAnimation extends Animator implements Animation.AnimationListener, Interpolator, Handler.Callback {

    /** Listener event type - animation start */
    private static final int EVENT_START = 1;

    /** Listener event type - animation finish */
    private static final int EVENT_FINISH = 2;

    /** Listener event type - animation repeat */
    private static final int EVENT_REPEAT = 3;

    /** Listener event type - animation cancel */
    private static final int EVENT_CANCEL = 4;

    /** Target view to apply animation to */
    private final View view;

    /** Animation loaded from resources */
    private final Animation animation;

    /**
     * Some listener methods should be called in a next frame. This handler will be used to send message
     * to next frame.
     */
    private Handler uiHandler;

    /** Custom interpolator from animation package */
    private TimeInterpolator interpolator;

    public SimpleAnimation(@NonNull View view, @AnimRes int animResId) {
        this.view = view;
        animation = AnimationUtils.loadAnimation(view.getContext(), animResId);
        animation.setAnimationListener(this);
    }

    @Override
    public long getStartDelay() {
        return animation.getStartOffset();
    }

    @Override
    public void setStartDelay(long startDelay) {
        animation.setStartOffset(startDelay);
    }

    @Override
    public Animator setDuration(long duration) {
        animation.setDuration(duration);
        return this;
    }

    @Override
    public long getDuration() {
        return animation.getDuration();
    }

    @Override
    public void setInterpolator(TimeInterpolator value) {
        interpolator = value;
        animation.setInterpolator(this);
    }

    @Override
    public boolean isRunning() {
        return animation.hasStarted() && !animation.hasEnded();
    }

    @Override
    public void start() {
        view.startAnimation(animation);
    }

    @Override
    public void end() {
        animation.cancel();
    }

    @Override
    public void cancel() {
        postEvent(EVENT_CANCEL, true);
        animation.cancel();
    }

    @Override
    public void onAnimationStart(Animation animation) {
        postEvent(EVENT_START, false);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        postEvent(EVENT_FINISH, true);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        postEvent(EVENT_REPEAT, false);
    }

    @Override
    public float getInterpolation(float input) {
        if (interpolator == null) {
            interpolator = new AccelerateDecelerateInterpolator();
        }
        return interpolator.getInterpolation(input);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_START:
            case EVENT_FINISH:
            case EVENT_REPEAT:
            case EVENT_CANCEL:
                postEvent(msg.what, false);
                return true;
        }
        return false;
    }

    @Nullable
    private ArrayList<AnimatorListener> cloneListeners() {
        ArrayList<AnimatorListener> listeners = getListeners();
        if (listeners != null) {
            listeners = (ArrayList<AnimatorListener>) listeners.clone();
        }
        return listeners;
    }

    private void postEvent(int eventType, boolean delayed) {
        if (delayed) {
            if (uiHandler == null) {
                uiHandler = new Handler(Looper.myLooper(), this);
            }
            uiHandler.sendEmptyMessage(eventType);
        } else {
            final ArrayList<AnimatorListener> listeners = cloneListeners();
            final int numListeners = listeners == null ? 0 : listeners.size();
            for (int i = 0; i < numListeners; i++) {
                final AnimatorListener listener = listeners.get(i);
                switch (eventType) {
                    case EVENT_START:
                        listener.onAnimationStart(this);
                        break;
                    case EVENT_FINISH:
                        listener.onAnimationEnd(this);
                        break;
                    case EVENT_REPEAT:
                        listener.onAnimationRepeat(this);
                        break;
                    case EVENT_CANCEL:
                        listener.onAnimationCancel(this);
                        break;
                }
            }
        }
    }
}
