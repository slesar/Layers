package com.psliusar.layers;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Layers {

    private static final String STATE_STACK = "LAYERS.STATE_STACK";
    private static final String STATE_LAYERS = "LAYERS.STATE_LAYERS";

    private final LayersHost host;
    private final int containerId;
    private ViewGroup container;
    private SparseArray<Layers> layerGroup;
    private ArrayList<StackEntry> layerStack;
    private boolean viewPaused = false;
    private boolean stateSaved = false;
    private Transition<?> transition;

    public Layers(@NonNull LayersHost host, @Nullable Bundle savedState) {
        this(host, View.NO_ID, savedState);
    }

    Layers(@NonNull LayersHost host, @IdRes int containerId, @Nullable Bundle savedState) {
        this.host = host;
        this.containerId = containerId;

        viewPaused = savedState != null;
        if (savedState != null) {
            layerStack = savedState.getParcelableArrayList(STATE_STACK);
            if (layerStack != null) {
                for (int i = 0, size = layerStack.size(); i < size; i++) {
                    final StackEntry entry = layerStack.get(i);
                    entry.layerInstance = null;
                    entry.state = StackEntry.LAYER_STATE_EMPTY;
                    moveToState(entry, i, StackEntry.LAYER_STATE_CREATED, false);
                }
            }

            final SparseArray<Bundle> layersArray = savedState.getSparseParcelableArray(STATE_LAYERS);
            final int layersCount;
            if (layersArray != null && (layersCount = layersArray.size()) > 0) {
                for (int i = 0; i < layersCount; i++) {
                    int key = layersArray.keyAt(i);
                    final Bundle state = layersArray.get(key);
                    at(key, state);
                }
            }
        }
        if (layerStack == null) {
            layerStack = new ArrayList<>();
        }
    }

    public Layers at(@IdRes int containerId) {
        if (this.containerId == containerId) {
            return this;
        }
        return at(containerId, null);
    }

    private Layers at(int containerId, @Nullable Bundle state) {
        if (layerGroup == null) {
            layerGroup = new SparseArray<>();
        }
        Layers layers = layerGroup.get(containerId);
        if (layers == null) {
            layers = new Layers(host, containerId, state);
            layerGroup.put(containerId, layers);
        }
        return layers;
    }

    @NonNull
    public LayersHost getHost() {
        return host;
    }

    /**
     * Un-pause View creation. View on the top of the stack will be created if wasn't created already.
     */
    public void resumeView() {
        if (!viewPaused) {
            return;
        }
        viewPaused = false;

        ensureViews();

        if (layerGroup != null) {
            for (int i = 0, count = layerGroup.size(); i < count; i++) {
                int key = layerGroup.keyAt(i);
                layerGroup.get(key).resumeView();
            }
        }
    }

    /**
     * Set View creation to pause. Views will not be created until {@link Layers#resumeView} is called.
     */
    public void pauseView() {
        viewPaused = true;
    }


    /* === Public layer operations === */

    @NonNull
    public <L extends Layer<?>> Transition<L> add(@NonNull Class<L> layerClass) {
        finishTransition();
        return new AddTransition<>(this, layerClass);
    }

    @NonNull
    public <L extends Layer<?>> Transition<L> replace(@NonNull Class<L> layerClass) {
        finishTransition();
        return new ReplaceTransition<>(this, layerClass);
    }

    @NonNull
    public <L extends Layer<?>> Transition<L> remove(int index) {
        finishTransition();
        return new RemoveTransition<>(this, index);
    }

    @NonNull
    public <L extends Layer<?>> Transition<L> remove(@NonNull L layer) {
        final int size = layerStack.size();
        for (int i = size - 1; i >= 0; i--) {
            final StackEntry entry = layerStack.get(i);
            if (entry.layerInstance == layer) {
                return remove(i);
            }
        }
        throw new IllegalArgumentException("Layer not found: " + layer);
    }

    @Nullable
    public <L extends Layer<?>> L pop() {
        finishTransition();
        final int size = layerStack.size();
        if (size == 0) {
            return null;
        }

        //noinspection unchecked
        return (L) new RemoveTransition<>(this, size - 1).commit();
    }

    public boolean onBackPressed() {
        final int size = layerStack.size();
        if (size == 0) {
            return false;
        }
        for (int i = size - 1; i >= 0; i--) {
            final StackEntry entry = layerStack.get(i);
            if (!entry.valid) {
                continue;
            }
            final Layer<?> topLayer = entry.layerInstance;
            return topLayer != null && topLayer.onBackPressed();
        }
        return false;
    }

    @Nullable
    public <L extends Layer<?>> L popLayersTo(@Nullable String name, boolean inclusive) {
        final int size = layerStack.size();
        if (size == 0) {
            return null;
        }

        // If name is not defined - any layer matches
        if (name == null) {
            // Remove all
            if (inclusive) {
                final StackEntry entry = layerStack.get(0);
                clear();
                //noinspection unchecked
                return (L) entry.layerInstance;
            }
        } else {
            boolean found = false;

            for (int i = size - 1; i >= 0; i--) {
                final StackEntry entry = layerStack.get(i);
                if (name.equals(entry.name)) {
                    found = true;
                    break;
                }
            }

            // Nothing found, do not touch the stack
            if (!found) {
                return null;
            }
        }

        pauseView();

        StackEntry lastEntry = null;
        for (int i = size - 1; i >= 0; i--) {
            final StackEntry entry = layerStack.get(i);
            if ((name == null && i > 0) || (name != null && !name.equals(entry.name)) || inclusive) {
                moveToState(entry, i, StackEntry.LAYER_STATE_DESTROYED, false);
                layerStack.remove(i);
                lastEntry = entry;
                if (!(name == null || !name.equals(entry.name))) {
                    break;
                }
            } else {
                break;
            }
        }

        resumeView();

        //noinspection unchecked
        return lastEntry == null ? null : (L) lastEntry.layerInstance;
    }

    public int clear() {
        final int size = layerStack.size();
        if (size == 0) {
            return 0;
        }

        pauseView();

        for (int i = size - 1; i >= 0; i--) {
            moveToState(layerStack.get(i), i, StackEntry.LAYER_STATE_DESTROYED, false);
        }

        layerStack.clear();

        resumeView();

        return size;
    }


    /* === Layer getters === */

    @Nullable
    public <L extends Layer<?>> L peek() {
        final int size = layerStack.size();
        return size == 0 ? null : this.<L>get(size - 1);
    }

    @Nullable
    public <L extends Layer<?>> L get(int index) {
        final int size = layerStack.size();
        if (index < 0 || index >= size) {
            return null;
        }

        //noinspection unchecked
        return (L) layerStack.get(index).layerInstance;
    }

    @Nullable
    public <L extends Layer<?>> L find(@Nullable String name) {
        final int size = layerStack.size();
        if (size == 0) {
            return null;
        }

        for (int i = size - 1; i >= 0; i--) {
            final StackEntry entry = layerStack.get(i);
            if (name == entry.name // == is the same object or null ("cheap" version of equals)
                    || (name != null && name.equals(entry.name))) {
                //noinspection unchecked
                return (L) entry.layerInstance;
            }
        }

        return null;
    }


    /* === Indicators === */

    public boolean hasRunningTransition() {
        return transition != null/* && !transition.isFinished()*/;
    }

    public boolean isViewPaused() {
        return viewPaused;
    }

    public boolean isInSavedState() {
        return stateSaved;
    }

    public int getStackSize() {
        return layerStack.size();
    }


    /* === Lifecycle === */

    void restoreState() {
        stateSaved = false;
        final int size = layerStack.size();
        for (int i = size - 1; i >= 0; i--) {
            layerStack.get(i).layerInstance.restoreLayerState();
        }

        // Restore state of layers which are at other containers
        final int layersCount = layerGroup == null ? 0 : layerGroup.size();
        for (int i = 0; i < layersCount; i++) {
            final int key = layerGroup.keyAt(i);
            layerGroup.get(key).restoreState();
        }
    }

    @Nullable
    Bundle saveState() {
        final Bundle outState = new Bundle();
        final int size = layerStack.size();
        if (size != 0) {
            final ArrayList<StackEntry> toSave = new ArrayList<>(size);
            for (int i = size - 1; i >= 0; i--) {
                final StackEntry entry = layerStack.get(i);
                if (!entry.valid) {
                    continue;
                }
                toSave.add(entry);
                if (entry.layerInstance.view != null) {
                    saveViewState(entry);
                }
                saveLayerState(entry);
            }
            Collections.reverse(toSave);
            outState.putParcelableArrayList(STATE_STACK, toSave);
        }

        // Save state of layers which are at other containers
        final int layersCount = layerGroup == null ? 0 : layerGroup.size();
        if (layersCount > 0) {
            final SparseArray<Bundle> layersArray = new SparseArray<>();
            for (int i = 0; i < layersCount; i++) {
                final int key = layerGroup.keyAt(i);
                final Bundle state = layerGroup.get(key).saveState();
                if (state != null) {
                    layersArray.put(key, state);
                }
            }
            outState.putSparseParcelableArray(STATE_LAYERS, layersArray);
        }
        stateSaved = true;
        return outState.size() > 0 ? outState : null;
    }

    public void destroy() {
        final int size = layerStack.size();
        for (int i = size - 1; i >= 0; i--) {
            final StackEntry entry = layerStack.get(i);
            moveToState(entry, i, StackEntry.LAYER_STATE_DESTROYED, false);
        }

        // Layers at other containers
        final int layersCount = (layerGroup == null ? 0 : layerGroup.size());
        for (int i = 0; i < layersCount; i++) {
            layerGroup.get(layerGroup.keyAt(i)).destroy();
        }
    }


    /* === Layer management === */

    private void moveToState(@NonNull StackEntry entry, int index, int targetState, boolean saveState) {
        if (targetState <= StackEntry.LAYER_STATE_VIEW_CREATED) {
            int state = entry.state;
            while (state < targetState) {
                switch (state) {
                    case StackEntry.LAYER_STATE_EMPTY:
                        createLayer(entry);
                        entry.state = StackEntry.LAYER_STATE_CREATED;
                        break;
                    case StackEntry.LAYER_STATE_CREATED:
                        if (!viewPaused) {
                            createView(entry, index);
                            entry.state = StackEntry.LAYER_STATE_VIEW_CREATED;
                        }
                        break;
                    case StackEntry.LAYER_STATE_VIEW_CREATED:
                        // nothing to do
                        break;
                    default:
                        return;
                }
                state++;
            }
        } else {
            switch (targetState) {
                case StackEntry.LAYER_STATE_VIEW_DESTROYED:
                    targetState = StackEntry.LAYER_STATE_CREATED;
                    break;
                case StackEntry.LAYER_STATE_DESTROYED:
                    targetState = StackEntry.LAYER_STATE_EMPTY;
                    break;
            }
            int state = entry.state;
            while (state > targetState) {
                switch (state) {
                    case StackEntry.LAYER_STATE_EMPTY:
                        // nothing to do
                        return;
                    case StackEntry.LAYER_STATE_CREATED:
                        destroyLayer(entry, saveState);
                        entry.state = StackEntry.LAYER_STATE_EMPTY;
                        break;
                    case StackEntry.LAYER_STATE_VIEW_CREATED:
                        destroyView(entry, saveState);
                        entry.state = StackEntry.LAYER_STATE_CREATED;
                        break;
                    default:
                        return;
                }
                state--;
            }
        }
    }

    private void createLayer(@NonNull StackEntry entry) {
        final Layer<?> layer = entry.instantiateLayer(host.getActivity().getApplicationContext());
        layer.create(host, entry.arguments, entry.name, entry.pickLayerSavedState());
    }

    private void createView(@NonNull StackEntry entry, int index) {
        final ViewGroup container = getContainer();
        final Layer<?> layer = entry.layerInstance;
        final View layerView = layer.onCreateView(layer.isViewInLayout() ? container : null);
        layer.view = layerView;
        if (layerView != null && layer.isViewInLayout()) {
            int fromEnd = 0;
            for (int i = layerStack.size() - 1; i > index ; i--) {
                final StackEntry item = layerStack.get(i);
                if (item.layerInstance != null && item.layerInstance.view != null) {
                    fromEnd++;
                }
            }

            final int position = container.getChildCount() - fromEnd;
            container.addView(layerView, position);
        }

        layer.attached = true;

        layer.onAttach();

        if (layerView != null) {
            layer.onBindView(layerView);
            restoreViewState(entry);
        }
    }

    private void restoreViewState(@NonNull StackEntry entry) {
        final SparseArray<Parcelable> savedState = entry.pickViewSavedState();
        entry.layerInstance.restoreViewState(savedState);
    }

    private void saveViewState(@NonNull StackEntry entry) {
        final SparseArray<Parcelable> viewState = new SparseArray<>();
        entry.layerInstance.saveViewState(viewState);
        if (viewState.size() > 0) {
            entry.setViewSavedState(viewState);
        }
    }

    private void saveLayerState(@NonNull StackEntry entry) {
        final Bundle bundle = new Bundle();
        entry.layerInstance.saveLayerState(bundle);
        if (bundle.size() > 0) {
            entry.setLayerSavedState(bundle);
        }
    }

    private void destroyView(@NonNull StackEntry entry, boolean saveState) {
        final Layer<?> layer = entry.layerInstance;

        if (saveState && layer.view != null) {
            saveViewState(entry);
        }

        layer.onDetach();

        layer.attached = false;

        layer.destroyView();
        if (layer.isViewInLayout() && layer.view != null) {
            getContainer().removeView(layer.view);
        }
        layer.view = null;
    }

    private void destroyLayer(@NonNull StackEntry entry, boolean saveState) {
        if (saveState) {
            saveLayerState(entry);
        }
        entry.layerInstance.destroy(!saveState);
    }


    /* === Transition === */

    void startTransition(@NonNull Transition<?> t) {
        transition = t;
        if (viewPaused) {
            return;
        }
        ensureViews();
    }

    void finishTransition() {
        // TODO check all the places that depend on stack size
        if (transition != null) {
            // The only reason why it's not null - animations.
            final Transition<?> t = transition;
            transition = null;
            t.cancelAnimations();
            ensureViews();
        }
    }

    @Nullable
    Transition<?> getCurrentTransition() {
        return transition;
    }


    /* === Internal tools === */

    Layer<?> commitStackEntry(@NonNull StackEntry entry) {
        layerStack.add(entry);
        ensureViews();

        return entry.layerInstance;
    }

    @NonNull
    StackEntry getStackEntryAt(int index) {
        return layerStack.get(index);
    }

    /**
     * TODO
     *
     * @param index
     * @param <L>
     * @return
     */
    @Nullable
    <L extends Layer<?>> L removeLayerAt(int index) {
        final int size = layerStack.size();
        if (index < 0 || index >= size) {
            return null;
        }
        final StackEntry entry = layerStack.get(index);
        moveToState(entry, index, StackEntry.LAYER_STATE_DESTROYED, false);

        //noinspection unchecked
        final L layer = (L) layerStack.remove(index).layerInstance;

        ensureViews();

        return layer;
    }

    /**
     * All "transparent" from top till ground or "opaque", including it.
     *
     * @param more minimum amount of the layers at the end of the stack to be considered "transparent"
     * @return the lowest visible layer index
     */
    int getLowestVisibleLayer(int more) {
        final int size = layerStack.size();
        int lowest = size - 1;
        if (more > 0) {
            lowest -= more;
        }
        if (lowest < 0) {
            return 0;
        }
        // from end to beginning, search for opaque layer
        int i = lowest;
        while (i >= 0) {
            lowest = i;
            if (layerStack.get(i).layerType == StackEntry.TYPE_OPAQUE) {
                break;
            }
            i--;
        }
        return lowest;
    }


    /* === Views === */

    private void ensureViews() {
        final int size;
        if (viewPaused || (size = layerStack.size()) == 0) {
            return;
        }

        final int lowest = hasRunningTransition() ? transition.getLowestVisibleLayer() : getLowestVisibleLayer(0);
        for (int i = 0; i < size; i++) {
            final StackEntry entry = layerStack.get(i);
            if (i < lowest) {
                moveToState(entry, i, StackEntry.LAYER_STATE_VIEW_DESTROYED, true);
            } else {
                moveToState(entry, i, StackEntry.LAYER_STATE_VIEW_CREATED, false);
            }
        }
    }

    @NonNull
    private ViewGroup getContainer() {
        if (container == null) {
            container = containerId == View.NO_ID
                    ? host.getDefaultContainer() : (ViewGroup) host.getView(containerId);
            container.setSaveFromParentEnabled(false);
        }
        return container;
    }
}
