package com.psliusar.layers;

import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.animation.LayerAnimation;
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

    @IntDef({ACTION_ADD, ACTION_REPLACE, ACTION_POP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionType {}

    private final Layers layers;
    private final Class<LAYER> layerClass;
    private final int action;
    private Bundle arguments;
    private String name;
    private boolean opaque = true;
    private final int[] animations = new int[4];

    private int stackSize;
    private boolean committed = false;
    final HashSet<LayerAnimation> animationSet = new HashSet<>();

    private LayerAnimation.OnLayerAnimationListener animationListener = new LayerAnimation.OnLayerAnimationListener() {
        @Override
        public void onAnimationEnd(@NonNull LayerAnimation animation) {
            animationSet.remove(animation);
            if (animationSet.size()== 0) {
                finish();
            }
        }
    };

    Transition(@NonNull Layers layers, @Nullable Class<LAYER> layerClass, @ActionType int action) {
        this.layers = layers;
        this.layerClass = layerClass;
        this.action = action;
    }

    public Transition<LAYER> setArguments(@NonNull Bundle arguments) {
        this.arguments = arguments;
        return this;
    }

    public Transition<LAYER> setName(@NonNull String name) {
        this.name = name;
        return this;
    }

    public Transition<LAYER> setOpaque(boolean opaque) {
        this.opaque = opaque;
        return this;
    }

    /**
     * Direct animation
     *
     * @param outAnimId layer(s) that comes out
     * @param inAnimId layer that comes in
     * @return this transaction
     */
    public Transition<LAYER> setInAnimation(@AnimRes int outAnimId, @AnimRes int inAnimId) {
        animations[ANIMATION_LOWER_OUT] = outAnimId;
        animations[ANIMATION_UPPER_IN] = inAnimId;
        return this;
    }

    /**
     * Reverse animation
     *
     * @param inAnimId layer(s) that returns back from the stack
     * @param outAnimId layer that pops out from the top of the stack
     * @return this transaction
     */
    public Transition<LAYER> setOutAnimation(@AnimRes int inAnimId, @AnimRes int outAnimId) {
        animations[ANIMATION_LOWER_IN] = inAnimId;
        animations[ANIMATION_UPPER_OUT] = outAnimId;
        return this;
    }

    public boolean isFinished() {
        return animationSet.isEmpty();
    }

    private boolean hasInAnimations() {
        return animations[ANIMATION_LOWER_OUT] != 0 || animations[ANIMATION_UPPER_IN] != 0;
    }

    private boolean hasOutAnimations() {
        return animations[ANIMATION_LOWER_IN] != 0 || animations[ANIMATION_UPPER_OUT] != 0;
    }

    private void cancelAnimations() {
        for (LayerAnimation animation : animationSet) {
            animation.stop();
        }
        finish();
    }

    void finish() {
        switch (action) {
            case ACTION_REPLACE:
                if (stackSize > 0) {
                    layers.removeLayerAt(stackSize - 1);
                }
                break;
            case ACTION_POP:
                layers.popLayer();
                break;
        }
        layers.finishTransition();
    }

    void onPrepareLayer(@NonNull Layer<?> layer, int index) {
        switch (action) {
            case ACTION_ADD:
                animateLayer(layer, ANIMATION_LOWER_OUT);
                break;
            case ACTION_REPLACE:
                animateLayer(layer, ANIMATION_LOWER_OUT);
                break;
            case ACTION_POP:
                animateLayer(layer, index < stackSize - 1 ? ANIMATION_UPPER_OUT : ANIMATION_LOWER_IN);
                break;
        }
    }

    @Nullable
    public LAYER commit() {
        if (committed) {
            throw new IllegalStateException("!!");
        }
        /*final Transition<?> currentTransition = layers.getCurrentTransition();
        if (layers.hasRunningTransition() && currentTransition != null) {
            currentTransition.cancelAnimations();
        }*/
        committed = true;
        stackSize = layers.getStackSize();
        switch (action) {
            case ACTION_ADD:
                return commitAdd();
            case ACTION_REPLACE:
                return commitReplace();
            case ACTION_POP:
                return commitPop();
            default:
                throw new IllegalArgumentException("Invalid action ID: " + action);
        }
    }

    private LAYER commitAdd() {
        layers.startTransition(this, 0);

        final LAYER layer = layers.add(layerClass, arguments, name, opaque);
        animateLayer(layer, ANIMATION_UPPER_IN);

        final StackEntry entry = layers.getStackEntryAt(stackSize);
        entry.setAnimations(animations[0], animations[1], animations[2], animations[3]);

        if (animationSet.size() == 0) {
            finish();
        }
        return layer;
    }

    private LAYER commitReplace() {
        layers.startTransition(this, 0);


        final LAYER layer;
        /*LayerAnimation animation = null;
        if (stackSize > 0) {
            final int lowest = layers.getLowestVisibleEntry();
            for (int i = lowest; i < stackSize; i++) {
                animation = animateLayer(layers.get(i), ANIMATION_LOWER_OUT);
            }
        }*/
        if (animationSet.size() == 0) {
            // TODO detect ALL animations
            // No animation, just replace
            layer = layers.replace(layerClass, arguments, name, opaque);
        } else {
            // Add a new layer first
            layer = layers.add(layerClass, arguments, name, false);
            layers.getStackEntryAt(stackSize).layerType = opaque ? StackEntry.TYPE_OPAQUE : StackEntry.TYPE_TRANSPARENT;
            layers.getStackEntryAt(stackSize).layerTypeAnimated = StackEntry.TYPE_TRANSPARENT;
            // Then remove layer beneath
            // TODO when animation ends
            //layers.removeAt(stackSize - 1);
            if (stackSize > 0) {
                layers.getStackEntryAt(stackSize - 1).valid = false;
            }
        }
        animateLayer(layer, ANIMATION_UPPER_IN);

        final StackEntry entry = layers.getStackEntryAt(stackSize);
        entry.setAnimations(animations[0], animations[1], animations[2], animations[3]);

        if (animationSet.size() == 0) {
            finish();
        }
        return layer;
    }

    private LAYER commitPop() {
        // TODO index bounds check

        layers.startTransition(this, 1);

        final LAYER layer = layers.peek();

        if (animationSet.size() == 0) {
            finish();
        }
        return layer;
    }

    @Nullable
    private LayerAnimation animateLayer(@NonNull Layer<?> layer, @AnimationType int animationType) {
        if (layers.isViewPaused() || !layer.isViewInLayout() || layer.getView() == null) {
            return null;
        }
        LayerAnimation animation = layer.getAnimation(animationType);
        if (animation == null && animations[animationType] != 0) {
            animation = new SimpleAnimation(layers, animations[animationType]);
        }
        if (animation != null) {
            animation.start(layer);
            animation.setOnAnimationListener(animationListener);

            animationSet.add(animation);
        }
        return animation;
    }
}
