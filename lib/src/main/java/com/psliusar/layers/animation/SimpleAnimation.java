package com.psliusar.layers.animation;

import android.support.annotation.AnimRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.psliusar.layers.Layer;
import com.psliusar.layers.Layers;

public class SimpleAnimation implements LayerAnimation {

    private final Layers layers;
    private final Animation animation;

    public SimpleAnimation(@NonNull Layers layers, @AnimRes int animResId) {
        this.layers = layers;
        animation = AnimationUtils.loadAnimation(layers.getHost().getActivity(), animResId);
    }

    @Override
    public boolean isFinished() {
        return animation.hasEnded();
    }

    @Override
    public void start(@NonNull Layer<?> layer) {
        layer.getView().startAnimation(animation);
    }

    @Override
    public void stop() {
        animation.cancel();
    }

    @Override
    public void setOnAnimationListener(@Nullable final OnLayerAnimationListener listener) {
        if (listener != null) {
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    listener.onAnimationEnd(SimpleAnimation.this);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            animation.setAnimationListener(null);
        }
    }
}
