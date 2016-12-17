package com.psliusar.layers;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

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
                    // TODO honor existing state ?
                    entry.layerInstance = null;
                    entry.state = StackEntry.LAYER_STATE_EMPTY;
                    moveToState(entry, StackEntry.LAYER_STATE_CREATED, false);
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

    public boolean hasRunningTransition() {
        return transition != null/* && !transition.isFinished()*/;
    }

    int startTransition(@NonNull Transition<?> transition, int skip) {
        // TODO finish current transition

        this.transition = transition;
        if (viewPaused) {
            return 0;
        }
        // Prepare for transition
        final int size = layerStack.size();
        if (size == 0) {
            return 0;
        }
        // Reset state
        for (int i = 0; i < size; i++) {
            final StackEntry entry = layerStack.get(i);
            entry.layerTypeAnimated = i < size - skip ? entry.layerType : StackEntry.TYPE_TRANSPARENT;
        }
        return ensureViews();
    }

    void finishTransition() {
        transition = null;
        ensureViews();
    }

    @Nullable
    Transition<?> getCurrentTransition() {
        return transition;
    }

    @NonNull
    public LayersHost getHost() {
        return host;
    }

    @Nullable
    Bundle saveState() {
        final Bundle outState = new Bundle();
        final int size = layerStack.size();
        if (size != 0) {
            for (int i = size - 1; i >= 0; i--) {
                final StackEntry entry = layerStack.get(i);
                if (!entry.valid) {
                    continue;
                }
                if (entry.layerInstance.view != null) {
                    saveViewState(entry);
                }
                saveLayerState(entry);
            }
            outState.putParcelableArrayList(STATE_STACK, layerStack);
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
            moveToState(entry, StackEntry.LAYER_STATE_DESTROYED, false);
        }

        // Layers at other containers
        final int layersCount = (layerGroup == null ? 0 : layerGroup.size());
        for (int i = 0; i < layersCount; i++) {
            layerGroup.get(layerGroup.keyAt(i)).destroy();
        }
    }

    private <L extends Layer<?>> L createLayer(@NonNull StackEntry entry) {
        final L layer;
        try {
            //noinspection unchecked
            layer = (L) entry.getLayerClass(host.getActivity().getApplicationContext()).newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Unable to instantiate layer " + entry.getLayerClassName()
                    + ": make sure class exists, is public, and has an empty constructor", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to instantiate layer " + entry.getLayerClassName()
                    + ": make sure class has an empty constructor that is public", e);
        }

        layer.create(host, entry.arguments, entry.name);
        final Presenter presenter = layer.getPresenter();
        if (presenter != null) {
            //noinspection unchecked
            presenter.create(host, layer);
        }

        layer.onCreate(entry.pickLayerSavedState());

        return layer;
    }

    private void createView(@NonNull StackEntry entry) {
        final Layer<?> layer = entry.layerInstance;
        final View layerView = layer.onCreateView(layer.isViewInLayout() ? getContainer() : null);
        layer.view = layerView;
        if (layerView != null && layer.isViewInLayout()) {
            getContainer().addView(layerView);
        }

        layer.attached = true;

        layer.onAttach();

        if (layerView != null) {
            layer.onBindView(layerView);
            restoreViewState(entry);
        }
    }

    private void restoreLayerState(@NonNull StackEntry entry) {

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

        layer.onDestroyView();
        if (layer.isViewInLayout() && layer.view != null) {
            getContainer().removeView(layer.view);
        }
        layer.view = null;
    }

    private void destroyLayer(@NonNull StackEntry entry, boolean saveState) {
        if (saveState) {
            saveLayerState(entry);
        }
        entry.layerInstance.onDestroy();
    }

    private void moveToState(@NonNull StackEntry entry, int targetState, boolean saveState) {
        if (targetState <= StackEntry.LAYER_STATE_VIEW_CREATED) {
            int state = entry.state;
            while (state < targetState) {
                switch (state) {
                case StackEntry.LAYER_STATE_EMPTY:
                    entry.layerInstance = createLayer(entry);
                    entry.state = StackEntry.LAYER_STATE_CREATED;
                    break;
                case StackEntry.LAYER_STATE_CREATED:
                    if (!viewPaused) {
                        createView(entry);
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

    /**
     * All "transparent" from top till ground or "opaque", including it
     *
     * @return the lowest visible layer index
     */
    int getLowestVisibleEntry() {
        final int upper = layerStack.size() - 1;
        if (upper < 0) {
            return 0;
        }
        int lower = upper;
        final boolean inTransition = hasRunningTransition();
        // from end to beginning, search for opaque layer
        for (int i = upper; i >= 0; i--) {
            final StackEntry entry = layerStack.get(i);
            final int layerType = inTransition ? entry.layerTypeAnimated : entry.layerType;
            if (layerType == StackEntry.TYPE_TRANSPARENT
                    || layerType == StackEntry.TYPE_OPAQUE
                    || i == 0) {
                lower = i;
                if (layerType == StackEntry.TYPE_OPAQUE) {
                    break;
                }
            }
        }
        return lower;
    }

    private int ensureViews() {
        final int size;
        if (viewPaused || (size = layerStack.size()) == 0) {
            return 0;
        }

        final int lowest = getLowestVisibleEntry();
        for (int i = 0; i < size; i++) {
            final StackEntry entry = layerStack.get(i);
            if (i < lowest) {
                moveToState(entry, StackEntry.LAYER_STATE_VIEW_DESTROYED, true);
            } else {
                moveToState(entry, StackEntry.LAYER_STATE_VIEW_CREATED, false);
            }
        }
        return lowest;
    }

    public boolean isViewPaused() {
        return viewPaused;
    }

    public boolean isInSavedState() {
        return stateSaved;
    }

    @NonNull
    public <L extends Layer<?>> Transition<L> add(@NonNull Class<L> layerClass) {
        return new Transition<>(this, layerClass, Transition.ACTION_ADD);
    }

    @NonNull
    public <L extends Layer<?>> Transition<L> replace(@NonNull Class<L> layerClass) {
        return new Transition<>(this, layerClass, Transition.ACTION_REPLACE);
    }

    /**
     * Add layer and add View
     *
     * @param layerClass
     * @param name
     * @param <L>
     * @return
     */
    @NonNull
    public <L extends Layer<?>> L add(@NonNull Class<L> layerClass, @Nullable Bundle arguments, @Nullable String name, boolean opaque) {
        final StackEntry entry = new StackEntry(layerClass, arguments, name, opaque ? StackEntry.TYPE_OPAQUE : StackEntry.TYPE_TRANSPARENT);
        layerStack.add(entry);
        ensureViews();

        //noinspection unchecked
        return (L) entry.layerInstance;
    }

    /**
     * Remove layer and View
     *
     * @param layer
     * @return
     */
    @Nullable
    public <L extends Layer<?>> L remove(@NonNull L layer) {
        final int size = layerStack.size();
        if (size == 0) {
            return null;
        }

        pauseView();

        L result = null;
        for (int i = size - 1; i >= 0; i--) {
            final StackEntry entry = layerStack.get(i);
            if (entry.layerInstance == layer) {
                moveToState(entry, StackEntry.LAYER_STATE_DESTROYED, false);
                layerStack.remove(i);
                result = layer;
                break;
            }
        }

        resumeView();

        return result;
    }

    /**
     * TODO
     *
     * @param index
     * @param <L>
     * @return
     */
    @Nullable
    public <L extends Layer<?>> L removeLayerAt(int index) {
        final int size = layerStack.size();
        if (index < 0 || index >= size) {
            return null;
        }
        final StackEntry entry = layerStack.get(index);
        moveToState(entry, StackEntry.LAYER_STATE_DESTROYED, false);

        return (L) layerStack.remove(index).layerInstance;
    }

    /**
     * Replace layer
     *
     * @return
     */
    @NonNull
    public <L extends Layer<?>> L replace(@NonNull Class<L> layerClass, @Nullable Bundle arguments, @Nullable String name, boolean opaque) {
        final int size = layerStack.size();
        if (size > 0) {
            moveToState(removeLast(), StackEntry.LAYER_STATE_DESTROYED, false);
        }

        return add(layerClass, arguments, name, opaque);
    }

    @Nullable
    public <L extends Layer<?>> L pop() {
        final int size = layerStack.size();
        if (size == 0) {
            return null;
        }
        final StackEntry entry = layerStack.get(size - 1);
        return (L) new Transition<>(this, entry.layerInstance.getClass(), Transition.ACTION_POP).commit();
    }

    @Nullable
    <L extends Layer<?>> L popLayer() {
        if (layerStack.size() == 0) {
            return null;
        }

        final StackEntry entry = removeLast();
        moveToState(entry, StackEntry.LAYER_STATE_DESTROYED, false);

        ensureViews();

        //noinspection unchecked
        return (L) entry.layerInstance;
    }

    @Nullable
    public <L extends Layer<?>> L popLayersTo(@Nullable String name, boolean inclusive) {
        final int size = layerStack.size();
        if (size == 0) {
            return null;
        }

        // If name not defined - any layer matches
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
                moveToState(entry, StackEntry.LAYER_STATE_DESTROYED, false);
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

    @Nullable
    public <L extends Layer<?>> L peek() {
        final int size = layerStack.size();
        if (size == 0) {
            return null;
        }

        //noinspection unchecked
        return (L) layerStack.get(size - 1).layerInstance;
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
            if (name == entry.name // == is the same object or nulls
                    || (name != null && name.equals(entry.name))) {
                //noinspection unchecked
                return (L) entry.layerInstance;
            }
        }

        return null;
    }

    public int getStackSize() {
        return layerStack.size();
    }

    @NonNull
    StackEntry getStackEntryAt(int index) {
        return layerStack.get(index);
    }

    public int clear() {
        final int size = layerStack.size();
        if (size == 0) {
            return 0;
        }

        pauseView();

        for (int i = size - 1; i >= 0; i--) {
            moveToState(layerStack.get(i), StackEntry.LAYER_STATE_DESTROYED, false);
        }

        layerStack.clear();

        resumeView();

        return size;
    }

    @NonNull
    private StackEntry removeLast() {
        final int index = layerStack.size() - 1;
        StackEntry entry = layerStack.get(index);
        layerStack.remove(index);
        return entry;
    }

    private ViewGroup getContainer() {
        if (container == null) {
            final ViewGroup viewGroup = containerId == View.NO_ID
                    ? host.getDefaultContainer() : (ViewGroup) host.getView(containerId);
            if (Build.VERSION.SDK_INT == 0 // local unit tests
                    || Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                container = getContainerHc(viewGroup);
            } else {
                container = getContainerPreHc(viewGroup);
            }
        }
        return container;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static ViewGroup getContainerHc(ViewGroup viewGroup) {
        viewGroup.setSaveFromParentEnabled(false);
        return viewGroup;
    }

    private static ViewGroup getContainerPreHc(ViewGroup viewGroup) {
        return WrapperLayout.addTo(viewGroup);
    }
}
