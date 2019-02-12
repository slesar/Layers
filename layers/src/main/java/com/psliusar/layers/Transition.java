package com.psliusar.layers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.os.Bundle;

import com.psliusar.layers.animation.SimpleAnimation;

import java.util.HashSet;

import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Transition<LAYER extends Layer<?>> {

    public interface OnLayerTransition<L extends Layer<?>> {

        void onBeforeTransition(@NonNull L layer);
    }

    protected final Layers layers;
    protected final StackEntry stackEntry;
    protected final int index;

    @Nullable
    private AnimatorSet animatorSet;
    @Nullable
    private HashSet<Animator> toAnimate;
    private boolean committed = false;
    private boolean animationEnabled = true;

    private final Animator.AnimatorListener animationListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            finish();
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
        //noinspection unchecked
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
        return committed && toAnimate != null && toAnimate.size() > 0;
    }

    public void commit() {
        if (committed) {
            throw new IllegalStateException("Current transaction has been already committed");
        }
        committed = true;

        layers.addTransition(this);

        // TODO for specific index and non-empty transition queue - throw exception?

        onBeforeTransition();
    }

    void apply() {
        onTransition();

        if (toAnimate == null) {
            finish();
        } else {
            animatorSet = new AnimatorSet();
            animatorSet.addListener(animationListener);
            animatorSet.playTogether(toAnimate);
            animatorSet.start();
        }
    }

    /**
     * Called when the transition is added to the queue.
     */
    protected void onBeforeTransition() {

    }

    /**
     * A transition itself should be generally happening in this method. This is called right after th transition is taken from the queue.
     */
    protected void onTransition() {

    }

    /**
     * Any clean-up after transition should happen here.
     */
    protected void onAfterTransition() {

    }

    protected void finish() {
        onAfterTransition();

        layers.nextTransition();
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
            if (toAnimate == null) {
                toAnimate = new HashSet<>();
            }
            toAnimate.add(animation);
        }
        return animation;
    }

    protected boolean setTransitionState(int fromIndex) {
        if (fromIndex >= 0) {
            final int stackSize = layers.getStackSize();
            for (int i = fromIndex; i < stackSize; i++) {
                final StackEntry entry = layers.getStackEntryAt(i);
                if (i != fromIndex) {
                    // do not touch the lowest one, because it will make getLowestVisibleLayer return different value later
                    entry.inTransition = true;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    protected boolean resetTransitionState(int fromIndex) {
        if (fromIndex >= 0) {
            final int stackSize = layers.getStackSize();
            for (int i = fromIndex; i < stackSize; i++) {
                layers.getStackEntryAt(i).inTransition = false;
            }
            return true;
        } else {
            return false;
        }
    }
}
