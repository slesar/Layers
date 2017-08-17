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
     * Add new layer within transition
     */
    static final int ACTION_ADD = 1;

    /**
     * Replace top layer with a new one within transition
     */
    static final int ACTION_REPLACE = 2;

    /**
     * Remove layer from the stack within transition
     */
    static final int ACTION_REMOVE = 4;

    /**
     * All possible values of action type
     */
    @IntDef({ACTION_ADD, ACTION_REPLACE, ACTION_REMOVE})
    @Retention(RetentionPolicy.SOURCE)
    @interface ActionType {}

    private final Layers layers;
    @NonNull
    private final StackEntry stackEntry;
    private final Action<LAYER> action;
    private final int index;

    Transition(@NonNull Layers layers, @NonNull Class<LAYER> layerClass, @ActionType int actionType) {
        this.layers = layers;
        stackEntry = new StackEntry(layerClass);
        stackEntry.instantiateLayer(layers.getHost().getActivity().getApplicationContext());
        action = getAction(actionType);
        index = -1;
    }

    Transition(@NonNull Layers layers, int index, @ActionType int actionType) {
        this.layers = layers;
        this.index = index;
        stackEntry = layers.getStackEntryAt(index);
        action = getAction(actionType);
    }

    @NonNull
    private Action<LAYER> getAction(@ActionType int actionType) {
        final Action<LAYER> action;
        switch (actionType) {
            case ACTION_ADD:
                action = new AddAction<>();
                break;
            case ACTION_REPLACE:
                action = new ReplaceAction<>();
                break;
            case ACTION_REMOVE:
                action = new RemoveAction<>();
                break;
            default:
                throw new IllegalArgumentException("Invalid action type: " + actionType);
        }
        action.init(this, layers, stackEntry, index);
        return action;
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

    public Transition<LAYER> prepareLayer(@NonNull OnLayerTransition<LAYER> transitionAction) {
        transitionAction.onBeforeTransition((LAYER) stackEntry.layerInstance);
        return this;
    }

    public Transition<LAYER> setAnimationsEnabled(boolean enabled) {
        // TODO
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
    public Transition<LAYER> setOutAnimation(@AnimRes int outAnimId, @AnimRes int inAnimId) {
        if (stackEntry.animations == null) {
            stackEntry.animations = new int[4];
        }
        stackEntry.animations[AnimationType.ANIMATION_UPPER_OUT] = outAnimId;
        stackEntry.animations[AnimationType.ANIMATION_LOWER_IN] = inAnimId;
        return this;
    }

    public boolean isFinished() {
        return action.isFinished();
    }

    public boolean hasAnimations() {
        return action.hasAnimations();
    }

    public void cancelAnimations() {
        action.cancelAnimations();
    }

    @NonNull
    public LAYER commit() {
        return action.commit();
    }

    private abstract static class Action<LAYER extends Layer<?>> {

        private final Animator.AnimatorListener animationListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animatorSet = null;
                cleanUp();
            }
        };

        @Nullable
        private AnimatorSet animatorSet;
        private final HashSet<Animator> toAnimate = new HashSet<>();

        private Transition<LAYER> transition;
        protected Layers layers;
        protected StackEntry stackEntry;
        protected int index;

        protected int initialStackSize;
        protected int lowestVisibleLayer;
        private boolean committed = false;

        public void init(
                @NonNull Transition<LAYER> transition,
                @NonNull Layers layers,
                @NonNull StackEntry stackEntry,
                int index) {
            this.transition = transition;
            this.layers = layers;
            this.stackEntry = stackEntry;
            this.index = index;
        }

        public boolean isFinished() {
            return committed && animatorSet == null;
        }

        public boolean hasAnimations() {
            return committed && toAnimate.size() > 0;
        }

        @NonNull
        public LAYER commit() {
            if (committed) {
                // TODO
                throw new IllegalStateException("!!");
            }
            committed = true;

            initialStackSize = layers.getStackSize();
            lowestVisibleLayer = layers.getLowestVisibleLayer();
            final int transparentCount = getMinTransparentLayersCount();
            layers.startTransition(transition, transparentCount);

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

        protected void cancelAnimations() {
            if (animatorSet != null) {
                animatorSet.end();
            }
        }

        @Nullable
        protected Animator animateLayer(@NonNull Layer<?> layer, @AnimationType int animationType) {
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

        protected int getMinTransparentLayersCount() {
            return 0;
        }
    }

    private static class AddAction<LAYER extends Layer<?>> extends Action<LAYER> {

        @NonNull
        @Override
        protected LAYER performOperation() {
            // TODO index
            for (int i = lowestVisibleLayer; i < initialStackSize; i++) {
                animateLayer(layers.getStackEntryAt(i).layerInstance, AnimationType.ANIMATION_LOWER_OUT);
            }

            final LAYER layer = (LAYER) layers.commitStackEntry(stackEntry);
            animateLayer(layer, AnimationType.ANIMATION_UPPER_IN);

            return layer;
        }
    }

    private static class ReplaceAction<LAYER extends Layer<?>> extends Action<LAYER> {

        @NonNull
        @Override
        protected LAYER performOperation() {
            // TODO index
            for (int i = lowestVisibleLayer; i < initialStackSize; i++) {
                animateLayer(layers.getStackEntryAt(i).layerInstance, AnimationType.ANIMATION_LOWER_OUT);
            }

            // Add a new layer first
            final LAYER layer = (LAYER) layers.commitStackEntry(stackEntry);
            layers.getStackEntryAt(initialStackSize).layerTypeAnimated = StackEntry.TYPE_TRANSPARENT;

            // Then invalidate layer beneath and then remove it (after animation)
            if (initialStackSize > 0) {
                layers.getStackEntryAt(initialStackSize - 1).valid = false;
            }

            animateLayer(layer, AnimationType.ANIMATION_UPPER_IN);

            return layer;
        }

        @Override
        public void cleanUp() {
            if (initialStackSize > 0) {
                layers.removeLayerAt(initialStackSize - 1);
            }
            super.cleanUp();
        }
    }

    private static class RemoveAction<LAYER extends Layer<?>> extends Action<LAYER> {

        @Override
        protected int getMinTransparentLayersCount() {
            // TODO
            return index < lowestVisibleLayer ? 0 : initialStackSize - index;
        }

        @NonNull
        @Override
        protected LAYER performOperation() {
            if (index < 0 || index >= initialStackSize) {
                // TODO
                throw new IllegalArgumentException("!!");
            }

            for (int i = lowestVisibleLayer; i < initialStackSize; i++) {
                animateLayer(layers.getStackEntryAt(i).layerInstance, i == index ? AnimationType.ANIMATION_UPPER_OUT : AnimationType.ANIMATION_LOWER_IN);
            }

            layers.getStackEntryAt(index).valid = false;
            return layers.removeLayerAt(index);
        }

        @Override
        protected void cleanUp() {
            super.cleanUp();
        }
    }
}
