package com.psliusar.layers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.animation.SimpleAnimation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashSet;

public abstract class Transition<LAYER extends Layer<?>> {

    public interface OnLayerTransition<L extends Layer<?>> {

        void onBeforeTransition(@NonNull L layer);
    }

    protected final Layers layers;
    protected final StackEntry stackEntry;
    protected final int index;

    @Nullable
    private AnimatorSet animatorSet;
    private final HashSet<Animator> toAnimate = new HashSet<>();

    protected int initialStackSize;
    protected int lowestVisibleLayer;
    private boolean committed = false;
    private boolean animationEnabled = true;

    private final Animator.AnimatorListener animationListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            cleanUp();
        }
    };

    Transition(@NonNull Layers layers, @NonNull Class<LAYER> layerClass) {
        this.layers = layers;
        stackEntry = new StackEntry(layerClass);
        stackEntry.instantiateLayer(layers.getHost().getActivity().getApplicationContext());
        index = -1;
    }

    Transition(@NonNull Layers layers, int index) {
        this.layers = layers;
        this.index = index;
        stackEntry = layers.getStackEntryAt(index);
    }

    @NonNull
    public Transition<LAYER> setArguments(@NonNull Bundle arguments) {
        stackEntry.arguments = arguments;
        return this;
    }

    @NonNull
    public Transition<LAYER> setName(@NonNull String name) {
        stackEntry.name = name;
        return this;
    }

    @NonNull
    public Transition<LAYER> setOpaque(boolean opaque) {
        stackEntry.layerType = opaque ? StackEntry.TYPE_OPAQUE : StackEntry.TYPE_TRANSPARENT;
        return this;
    }

    @NonNull
    public Transition<LAYER> prepareLayer(@NonNull OnLayerTransition<LAYER> transitionAction) {
        transitionAction.onBeforeTransition((LAYER) stackEntry.layerInstance);
        return this;
    }

    @NonNull
    public Transition<LAYER> setAnimationEnabled(boolean enabled) {
        animationEnabled = enabled;
        return this;
    }

    /**
     * Direct animation
     *
     * @param outAnimId layer(s) that comes out
     * @param inAnimId layer that comes in
     * @return this transaction
     */
    @NonNull
    public Transition<LAYER> setInAnimation(@AnimRes int outAnimId, @AnimRes int inAnimId) {
        if (stackEntry.animations == null) {
            stackEntry.animations = new int[4];
        }
        stackEntry.animations[AnimationType.ANIMATION_LOWER_OUT] = outAnimId;
        stackEntry.animations[AnimationType.ANIMATION_UPPER_IN] = inAnimId;
        return this;
    }

    /**
     * Reverse animation
     *
     * @param outAnimId layer that pops out from the top of the stack
     * @param inAnimId layer(s) that returns back from the stack
     * @return this transaction
     */
    @NonNull
    public Transition<LAYER> setOutAnimation(@AnimRes int outAnimId, @AnimRes int inAnimId) {
        if (stackEntry.animations == null) {
            stackEntry.animations = new int[4];
        }
        stackEntry.animations[AnimationType.ANIMATION_UPPER_OUT] = outAnimId;
        stackEntry.animations[AnimationType.ANIMATION_LOWER_IN] = inAnimId;
        return this;
    }

    public boolean isFinished() {
        return committed && animatorSet == null;
    }

    public boolean hasAnimations() {
        return committed && toAnimate.size() > 0;
    }

    public void cancelAnimations() {
        if (animatorSet != null) {
            animatorSet.end();
        }
    }

    @NonNull
    public LAYER commit() {
        if (committed) {
            throw new IllegalStateException("Current transaction was already committed");
        }
        committed = true;

        initialStackSize = layers.getStackSize();
        lowestVisibleLayer = layers.getLowestVisibleLayer();
        final int transparentCount = getMinTransparentLayersCount();
        layers.startTransition(this, transparentCount);

        final LAYER layer = performOperation();
        if (toAnimate.size() == 0) {
            cleanUp();
        } else {
            animatorSet = new AnimatorSet();
            animatorSet.addListener(animationListener);
            animatorSet.playTogether(toAnimate);
            animatorSet.start();
        }
        return layer;
    }

    @NonNull
    protected abstract LAYER performOperation();

    protected void cleanUp() {
        layers.finishTransition();
        animatorSet = null;
    }

    @Nullable
    protected Animator animateLayer(@NonNull Layer<?> layer, @AnimationType int animationType) {
        if (!animationEnabled
                || layers.isViewPaused()
                || !layer.isViewInLayout()
                || layer.getView() == null) {
            return null;
        }
        Animator animation = layer.getAnimation(animationType);
        if (animation == null && stackEntry.animations != null && stackEntry.animations[animationType] != 0) {
            animation = new SimpleAnimation(layer.getView(), stackEntry.animations[animationType]);
            animation.setTarget(layer.getView());
        }
        if (animation != null) {
            toAnimate.add(animation);
        }
        return animation;
    }

    protected int getMinTransparentLayersCount() {
        return 0;
    }
}
