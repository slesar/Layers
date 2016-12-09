package com.psliusar.layers.animation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.Layer;

public interface LayerAnimation {

    boolean isFinished();

    void start(@NonNull Layer<?> layer);

    void stop();

    void setOnAnimationListener(@Nullable OnLayerAnimationListener listener);

    interface OnLayerAnimationListener {

        //void onAnimationStart(LayerAnimation animation);

        void onAnimationEnd(@NonNull LayerAnimation animation);

        //void onAnimationRepeat(LayerAnimation animation);
    }
}
