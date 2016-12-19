package com.psliusar.layers.animation;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AnimRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import java.util.ArrayList;

public class SimpleAnimation extends Animator implements Animation.AnimationListener, Interpolator {

    private static final int EVENT_START = 1;
    private static final int EVENT_FINISH = 2;
    private static final int EVENT_REPEAT = 3;
    private static final int EVENT_CANCEL = 4;

    private final View view;
    private final Animation animation;
    private Handler uiHandler;
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
        animation.cancel();
        postEvent(EVENT_CANCEL, true);
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

    @Nullable
    ArrayList<AnimatorListener> cloneListeners() {
        ArrayList<AnimatorListener> listeners = getListeners();
        if (listeners != null) {
            listeners = (ArrayList<AnimatorListener>) listeners.clone();
        }
        return listeners;
    }

    private void postEvent(final int eventType, boolean delayed) {
        final Runnable runnable = new Runnable() {
            private final int type = eventType;

            @Override
            public void run() {
                final ArrayList<AnimatorListener> listeners = cloneListeners();
                final int numListeners = listeners == null ? 0 : listeners.size();
                for (int i = 0; i < numListeners; i++) {
                    final AnimatorListener listener = listeners.get(i);
                    switch (type) {
                        case EVENT_START:
                            listener.onAnimationStart(SimpleAnimation.this);
                            break;
                        case EVENT_FINISH:
                            listener.onAnimationEnd(SimpleAnimation.this);
                            break;
                        case EVENT_REPEAT:
                            listener.onAnimationRepeat(SimpleAnimation.this);
                            break;
                        case EVENT_CANCEL:
                            listener.onAnimationCancel(SimpleAnimation.this);
                            break;
                    }
                }
            }
        };
        if (delayed) {
            if (uiHandler == null) {
                uiHandler = new Handler(Looper.myLooper());
            }
            uiHandler.post(runnable);
        } else {
            runnable.run();
        }
    }
}
