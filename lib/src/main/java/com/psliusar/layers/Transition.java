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
    @Nullable
    private int[] animations;

    private int initialStackSize;
    private int lowestVisibleLayer;
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

    Transition(@NonNull Layers layers, @NonNull Class<LAYER> layerClass, @ActionType int action) {
        this.layers = layers;
        this.layerClass = layerClass;
        this.action = action;
        if (action == ACTION_POP) {
            final int size = layers.getStackSize();
            final StackEntry entry = layers.getStackEntryAt(size - 1);
            if (entry.animations != null) {
                setAnimations(entry.animations);
            }
        }
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
        if (animations == null) {
            animations = new int[4];
        }
        animations[ANIMATION_LOWER_OUT] = outAnimId;
        animations[ANIMATION_UPPER_IN] = inAnimId;
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
        if (animations == null) {
            animations = new int[4];
        }
        animations[ANIMATION_UPPER_OUT] = outAnimId;
        animations[ANIMATION_LOWER_IN] = inAnimId;
        return this;
    }

    public boolean isFinished() {
        return animationSet.isEmpty();
    }

    private void setAnimations(@NonNull int[] src) {
        if (animations == null) {
            animations = new int[4];
        }
        System.arraycopy(src, 0, animations, 0, src.length);
    }

    private boolean hasAnimations() {
        return animations != null
                && (animations[ANIMATION_LOWER_OUT] != 0 || animations[ANIMATION_UPPER_IN] != 0
                || animations[ANIMATION_LOWER_IN] != 0 || animations[ANIMATION_UPPER_OUT] != 0);
    }

    private void cancelAnimations() {
        for (LayerAnimation animation : animationSet) {
            animation.stop();
        }
        finish();
    }

    void start(int skip) {
        initialStackSize = layers.getStackSize();
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
    }

    @Nullable
    public LAYER commit() {
        if (committed) {
            throw new IllegalStateException("!!");
        }
        start(action == ACTION_POP ? 1 : 0);
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
            default:
                throw new IllegalArgumentException("Invalid action ID: " + action);
        }
        if (animationSet.size() == 0) {
            finish();
        }
        return layer;
    }

    private LAYER commitAdd() {
        for (int i = lowestVisibleLayer; i < initialStackSize; i++) {
            animateLayer(layers.getStackEntryAt(i).layerInstance, ANIMATION_LOWER_OUT);
        }

        final LAYER layer = layers.add(layerClass, arguments, name, opaque);
        animateLayer(layer, ANIMATION_UPPER_IN);

        setEntryAnimations(layers.getStackEntryAt(initialStackSize));

        return layer;
    }

    private LAYER commitReplace() {
        for (int i = lowestVisibleLayer; i < initialStackSize; i++) {
            animateLayer(layers.getStackEntryAt(i).layerInstance, ANIMATION_LOWER_OUT);
        }

        final LAYER layer;

        if (animationSet.size() == 0) {
            // TODO detect ALL animations
            // No animation, just replace
            layer = layers.replace(layerClass, arguments, name, opaque);
        } else {
            // Add a new layer first
            layer = layers.add(layerClass, arguments, name, false);
            //layers.getStackEntryAt(initialStackSize).layerType = opaque ? StackEntry.TYPE_OPAQUE : StackEntry.TYPE_TRANSPARENT;
            layers.getStackEntryAt(initialStackSize).layerTypeAnimated = StackEntry.TYPE_TRANSPARENT;
            // Then invalidate layer beneath and then remove
            if (initialStackSize > 0) {
                layers.getStackEntryAt(initialStackSize - 1).valid = false;
            }
            setEntryAnimations(layers.getStackEntryAt(initialStackSize));
        }
        animateLayer(layer, ANIMATION_UPPER_IN);

        return layer;
    }

    private LAYER commitPop() {
        for (int i = lowestVisibleLayer; i < initialStackSize; i++) {
            animateLayer(layers.getStackEntryAt(i).layerInstance, i < initialStackSize - 1 ? ANIMATION_LOWER_IN : ANIMATION_UPPER_OUT);
        }

        final LAYER layer = layers.peek();

        return layer;
    }

    @Nullable
    private LayerAnimation animateLayer(@NonNull Layer<?> layer, @AnimationType int animationType) {
        if (layers.isViewPaused()
                || !layer.isViewInLayout()
                || layer.getView() == null) {
            return null;
        }
        LayerAnimation animation = layer.getAnimation(animationType);
        if (animation == null && animations != null && animations[animationType] != 0) {
            animation = new SimpleAnimation(layers, animations[animationType]);
        }
        if (animation != null) {
            animation.start(layer);
            animation.setOnAnimationListener(animationListener);

            animationSet.add(animation);
        }
        return animation;
    }

    private void setEntryAnimations(@NonNull StackEntry entry) {
        if (animations != null
                && (animations[ANIMATION_LOWER_OUT] != 0
                || animations[ANIMATION_UPPER_IN] != 0
                || animations[ANIMATION_LOWER_IN] != 0
                || animations[ANIMATION_UPPER_OUT] != 0)
                ) {
            entry.setAnimations(animations);
        }
    }
}
