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

public class Transition<LAYER extends Layer<?>> {

    /**
     * Layer goes deeper to the stack, will be covered with a new layer
     */
    public static final int ANIMATION_LOWER_OUT = 0;

    /**
     * Layer appears at the top of the stack
     */
    public static final int ANIMATION_UPPER_IN = 1;

    /**
     * Layer pops out of the stack
     */
    public static final int ANIMATION_UPPER_OUT = 2;

    /**
     * Layer goes up to the top of the stack
     */
    public static final int ANIMATION_LOWER_IN = 3;

    /**
     * All possible values of animation type
     */
    @IntDef({ANIMATION_LOWER_OUT, ANIMATION_UPPER_IN, ANIMATION_UPPER_OUT, ANIMATION_LOWER_IN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {}

    /**
     * Add new layer within transition
     */
    static final int ACTION_ADD = 1;

    /**
     * Replace top layer with a new one within transition
     */
    static final int ACTION_REPLACE = 2;

    /**
     * Pop top layer from the stack within transition
     */
    static final int ACTION_POP = 3;

    /**
     * Remove layer from the stack within transition
     */
    static final int ACTION_REMOVE = 4;

    /**
     * All possible values of action type
     */
    @IntDef({ACTION_ADD, ACTION_REPLACE, ACTION_POP, ACTION_REMOVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionType {}

    private final Layers layers;
    private final int action;

    private int initialStackSize;
    private int lowestVisibleLayer;
    private boolean committed = false;
    private final HashSet<Animator> toAnimate = new HashSet<>();

    @Nullable
    private AnimatorSet animatorSet;

    @NonNull
    private final StackEntry stackEntry;

    private final Animator.AnimatorListener animationListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            finish();
        }
    };

    Transition(@NonNull Layers layers, @NonNull Class<LAYER> layerClass, @ActionType int action) {
        this.layers = layers;
        this.action = action;
        stackEntry = new StackEntry(layerClass);
        stackEntry.instantiateLayer(layers.getHost().getActivity().getApplicationContext());
    }

    Transition(@NonNull Layers layers, @NonNull StackEntry entry, @ActionType int action) {
        this.layers = layers;
        this.stackEntry = entry;
        this.action = action;
    }

    public Transition<LAYER> setArguments(@NonNull Bundle arguments) {
        stackEntry.arguments = arguments;
        return this;
    }

    public Transition<LAYER> setName(@NonNull String name) {
        stackEntry.name = name;
        return this;
    }

    public Transition<LAYER> setOpaque(boolean opaque) {
        stackEntry.layerType = opaque ? StackEntry.TYPE_OPAQUE : StackEntry.TYPE_TRANSPARENT;
        return this;
    }

    public Transition<LAYER> prepareLayer(@NonNull OnLayerTransition<LAYER> action) {
        action.onBeforeTransition((LAYER) stackEntry.layerInstance);
        return this;
    }

    public interface OnLayerTransition<L extends Layer<?>> {

        void onBeforeTransition(@NonNull L layer);
    }

    /**
     * Direct animation
     *
     * @param outAnimId layer(s) that comes out
     * @param inAnimId layer that comes in
     * @return this transaction
     */
    public Transition<LAYER> setInAnimation(@AnimRes int outAnimId, @AnimRes int inAnimId) {
        if (stackEntry.animations == null) {
            stackEntry.animations = new int[4];
        }
        stackEntry.animations[ANIMATION_LOWER_OUT] = outAnimId;
        stackEntry.animations[ANIMATION_UPPER_IN] = inAnimId;
        return this;
    }

    /**
     * Reverse animation
     *
     * @param outAnimId layer that pops out from the top of the stack
     * @param inAnimId layer(s) that returns back from the stack
     * @return this transaction
     */
    public Transition<LAYER> setOutAnimation(@AnimRes int outAnimId, @AnimRes int inAnimId) {
        if (stackEntry.animations == null) {
            stackEntry.animations = new int[4];
        }
        stackEntry.animations[ANIMATION_UPPER_OUT] = outAnimId;
        stackEntry.animations[ANIMATION_LOWER_IN] = inAnimId;
        return this;
    }

    public boolean isFinished() {
        return committed && animatorSet == null;
    }

    public boolean hasAnimations() {
        return committed && animatorSet != null;
    }

    private void cancelAnimations() {
        if (animatorSet != null) {
            animatorSet.end();
        }
    }

    void start() {
        initialStackSize = layers.getStackSize();
        final int skip;
        if (action == ACTION_POP) {
            if (initialStackSize == 0) {
                throw new IllegalStateException("Cannot pop a Layer from empty stack");
            }
            skip = 1;
        } else {
            skip = 0;
        }
        lowestVisibleLayer = layers.startTransition(this, skip);
    }

    void finish() {
        switch (action) {
            case ACTION_REPLACE:
                if (initialStackSize > 0) {
                    layers.removeLayerAt(initialStackSize - 1);
                }
                break;
            case ACTION_POP:
                layers.popLayer();
                break;
        }
        layers.finishTransition();
        animatorSet = null;
    }

    @Nullable
    public LAYER commit() {
        if (committed) {
            throw new IllegalStateException("!!");
        }
        start();
        committed = true;
        final LAYER layer;
        switch (action) {
            case ACTION_ADD:
                layer = commitAdd();
                break;
            case ACTION_REPLACE:
                layer = commitReplace();
                break;
            case ACTION_POP:
                layer = commitPop();
                break;
            case ACTION_REMOVE:
                // TODO
                throw new IllegalArgumentException("Not yet implemented");
            default:
                throw new IllegalArgumentException("Invalid action ID: " + action);
        }
        if (toAnimate.size() == 0) {
            finish();
        } else {
            animatorSet = new AnimatorSet();
            animatorSet.addListener(animationListener);
            animatorSet.playTogether(toAnimate);
            animatorSet.start();
        }
        return layer;
    }

    private LAYER commitAdd() {
        for (int i = lowestVisibleLayer; i < initialStackSize; i++) {
            animateLayer(layers.getStackEntryAt(i).layerInstance, ANIMATION_LOWER_OUT);
        }

        final LAYER layer = (LAYER) layers.commitStackEntry(stackEntry);
        animateLayer(layer, ANIMATION_UPPER_IN);

        return layer;
    }

    private LAYER commitReplace() {
        for (int i = lowestVisibleLayer; i < initialStackSize; i++) {
            animateLayer(layers.getStackEntryAt(i).layerInstance, ANIMATION_LOWER_OUT);
        }

        final LAYER layer;

        if (toAnimate.size() == 0) {
            // TODO detect ALL animations
            // No animation, just replace
            //layer = layers.replace(layerClass, arguments, name, opaque);
            layer = (LAYER) layers.commitStackEntry(stackEntry);
        } else {
            // Add a new layer first
            layer = (LAYER) layers.commitStackEntry(stackEntry);
            //layers.getStackEntryAt(initialStackSize).layerType = opaque ? StackEntry.TYPE_OPAQUE : StackEntry.TYPE_TRANSPARENT;
            layers.getStackEntryAt(initialStackSize).layerTypeAnimated = StackEntry.TYPE_TRANSPARENT;
            // Then invalidate layer beneath and then remove
            if (initialStackSize > 0) {
                layers.getStackEntryAt(initialStackSize - 1).valid = false;
            }
        }
        animateLayer(layer, ANIMATION_UPPER_IN);

        return layer;
    }

    private LAYER commitPop() {
        for (int i = lowestVisibleLayer; i < initialStackSize; i++) {
            animateLayer(layers.getStackEntryAt(i).layerInstance, i < initialStackSize - 1 ? ANIMATION_LOWER_IN : ANIMATION_UPPER_OUT);
        }

        layers.getStackEntryAt(initialStackSize - 1).valid = false;

        final LAYER layer = layers.peek();

        return layer;
    }

    @Nullable
    private Animator animateLayer(@NonNull Layer<?> layer, @AnimationType int animationType) {
        if (layers.isViewPaused()
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
}
