package com.psliusar.layers;

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

    private static final String STATE_STACK = "STATE_STACK";
    private static final String STATE_LAYERS = "STATE_LAYERS";

    private final LayersHost host;
    private final int containerId;
    private ViewGroup container;
    private SparseArray<Layers> layerList;
    private ArrayList<StackEntry> layerStack;
    private boolean viewPaused = false; // TODO add checks and tests

    public Layers(@NonNull LayersHost host, @Nullable Bundle savedState) {
        this(host, View.NO_ID, savedState);
    }

    public Layers(@NonNull LayersHost host, @IdRes int containerId, @Nullable Bundle savedState) {
        this.host = host;
        this.containerId = containerId;

        viewPaused = savedState != null;
        if (savedState != null) {
            layerStack = savedState.getParcelableArrayList(STATE_STACK);
            if (layerStack != null) {
                int size = layerStack.size();
                for (int i = 0; i < size - 1; i++) {
                    moveToState(layerStack.get(i), StackEntry.LAYER_STATE_CREATED, false);
                }
            }

            SparseArray<Bundle> layersArray = savedState.getSparseParcelableArray(STATE_LAYERS);
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

        ensureViews();
    }

    public Layers at(@IdRes int containerId) {
        if (this.containerId == containerId) {
            return this;
        }
        return at(containerId, null);
    }

    private Layers at(int containerId, @Nullable Bundle state) {
        Layers layers = getLayersList().get(containerId);
        if (layers == null) {
            layers = new Layers(host, containerId, state);
            getLayersList().put(containerId, layers);
        }
        return layers;
    }

    /**
     * Set View creation to pause. Views will not be created until {@link Layers#resumeView} is called.
     */
    public void pauseView() {
        viewPaused = true;
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
    }

    public void saveState(@Nullable Bundle outState) {
        int size = layerStack.size();
        if (size == 0 || outState == null) {
            return;
        }
        for (int i = size - 1; i >= 0; i--) {
            StackEntry entry = layerStack.get(i);
            moveToState(entry, StackEntry.LAYER_STATE_DESTROYED, true);
        }
        outState.putParcelableArrayList(STATE_STACK, layerStack);

        final int layersCount = layerList == null ? -1 : layerList.size();
        final SparseArray<Bundle> layersArray;
        if (layersCount > 0) {
            layersArray = new SparseArray<>();
            for (int i = 0; i < layersCount; i++) {
                int key = layerList.keyAt(i);
                Bundle state = new Bundle();
                layerList.get(key).saveState(state);
                layersArray.put(key, state);
            }
        } else {
            layersArray = null;
        }
        outState.putSparseParcelableArray(STATE_LAYERS, layersArray);
    }

    private StackEntry createEntry(Class<? extends Layer<?>> layerClass, Bundle arguments, String name, int type) {
        return new StackEntry(layerClass, arguments, name, type);
    }

    private <L extends Layer<?>> L createLayer(StackEntry entry) {
        final L layer;
        try {
            //noinspection unchecked
            layer = (L) entry.getLayerClass().newInstance();
        } catch (InstantiationException e) {
            // TODO
            throw new RuntimeException("", e);
        } catch (IllegalAccessException e) {
            // TODO
            throw new RuntimeException("", e);
        }

        layer.create(host, entry.arguments, entry.name);
        final Presenter presenter = layer.getPresenter();
        if (presenter != null) {
            //noinspection unchecked
            presenter.create(host, layer);
        }

        layer.onCreate(entry.getLayerSavedState());

        return layer;
    }

    private void createView(StackEntry entry) {
        Layer<?> layer = entry.layerInstance;
        final View layerView = layer.onCreateView(getContainer());
        layer.view = layerView;
        if (layerView != null) {
            getContainer().addView(layerView);

            entry.attached = true;
            layer.attached = true;

            layer.onAttach();

            layer.getLayers().resumeView();

            layer.onBindView(layerView);

            restoreViewState(entry);
        }
    }

    private void restoreViewState(StackEntry entry) {
        SparseArray<Parcelable> savedState = entry.getViewSavedState();
        if (savedState != null) {
            entry.layerInstance.restoreViewState(savedState);
        }
    }

    private void saveViewState(StackEntry entry) {
        SparseArray<Parcelable> viewState = new SparseArray<>();
        entry.layerInstance.saveViewState(viewState);
        if (viewState.size() > 0) {
            entry.setViewSavedState(viewState);
        }
    }

    private void destroyView(StackEntry entry, boolean saveState) {
        Layer<?> layer = entry.layerInstance;

        if (saveState && entry.layerInstance.view != null) {
            saveViewState(entry);
        }

        layer.onDetach();

        entry.attached = false;
        layer.attached = false;

        getContainer().removeView(layer.view);

        layer.onDestroyView();
        layer.view = null;
    }

    private void destroyLayer(StackEntry entry, boolean saveState) {
        Layer<?> layer = entry.layerInstance;
        if (saveState) {
            Bundle bundle = new Bundle();
            layer.onDestroy(bundle);
            if (bundle.size() > 0) {
                entry.setLayerSavedState(bundle);
            }
        } else {
            layer.onDestroy(null);
        }
    }

    private void moveToState(StackEntry entry, int targetState, boolean saveState) {
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

    private void ensureViews() {
        int size = layerStack.size();
        if (viewPaused || size == 0) {
            return;
        }

        RevLink<StackEntry> pair = null;
        boolean found = false;

        // from end to beginning, search for opaque layer
        for (int i = size - 1; i >= 0; i--) {
            final StackEntry entry = layerStack.get(i);

            if (found) {
                // from found to beginning - remove views
                moveToState(entry, StackEntry.LAYER_STATE_VIEW_DESTROYED, true);
            } else {
                if (entry.type == StackEntry.TYPE_OPAQUE || i == 0) {
                    moveToState(entry, StackEntry.LAYER_STATE_VIEW_CREATED, false);
                    found = true;
                }

                if (pair == null) {
                    pair = new RevLink<>(entry);
                } else {
                    RevLink<StackEntry> p = new RevLink<>(entry);
                    p.prev = pair;
                    pair = p;
                }
            }
        }

        // from found to end - create views
        while (pair != null) {
            if (pair.prev != null) {
                moveToState(pair.prev.current, StackEntry.LAYER_STATE_VIEW_CREATED, false);
            }
            pair = pair.prev;
        }
    }

    public static class RevLink<T> {

        private RevLink<T> prev;
        private final T current;

        public RevLink(T current) {
            this.current = current;
        }
    }

    public boolean isViewPaused() {
        return viewPaused;
    }

    /**
     * Add layer and add View
     * @param layerClass
     * @param name
     * @param <L>
     * @return
     */
    @NonNull
    public <L extends Layer<?>> L add(@NonNull Class<L> layerClass, @Nullable Bundle arguments, @Nullable String name, boolean opaque) {
        final StackEntry entry = createEntry(layerClass, arguments, name, opaque ? StackEntry.TYPE_OPAQUE : StackEntry.TYPE_TRANSPARENT);
        layerStack.add(entry);
        ensureViews();

        //noinspection unchecked
        return (L) entry.layerInstance;
    }

    /**
     * Remove layer and View
     * @param layer
     * @return
     */
    @Nullable
    public <L extends Layer<?>> L remove(@NonNull L layer) {
        int size = layerStack.size();
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
     * Replace layer
     * @return
     */
    @NonNull
    public <L extends Layer<?>> L replace(@NonNull Class<L> layerClass, @Nullable Bundle arguments, @Nullable String name, boolean opaque) {
        int size = layerStack.size();
        if (size > 0) {
            moveToState(removeLast(), StackEntry.LAYER_STATE_DESTROYED, false);
        }

        return add(layerClass, arguments, name, opaque);
    }

    @Nullable
    public <L extends Layer<?>> L pop() {
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
    public <L extends Layer<?>> L popTo(@Nullable String name, boolean inclusive) {
        int size = layerStack.size();
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
        int size = layerStack.size();
        if (size == 0) {
            return null;
        }

        //noinspection unchecked
        return (L) layerStack.get(size - 1).layerInstance;
    }

    @Nullable
    public <L extends Layer<?>> L find(@Nullable String name) {
        int size = layerStack.size();
        if (size == 0) {
            return null;
        }

        for (int i = size - 1; i >= 0; i--) {
            final StackEntry entry = layerStack.get(i);
            if ((name == null && entry.name == null) || (name != null && name.equals(entry.name))) {
                //noinspection unchecked
                return (L) entry.layerInstance;
            }
        }

        return null;
    }

    public int getStackSize() {
        return layerStack.size();
    }

    public int clear() {
        int size = layerStack.size();
        if (layerStack.size() == 0) {
            return 0;
        }

        pauseView();

        for (int i = size - 1; i >= 0; i--) {
            final StackEntry entry = layerStack.get(i);
            moveToState(entry, StackEntry.LAYER_STATE_DESTROYED, false);
        }

        layerStack.clear();

        resumeView();

        return size;
    }

    private StackEntry removeLast() {
        int size = layerStack.size();
        StackEntry entry = layerStack.get(size - 1);
        layerStack.remove(size - 1);
        return entry;
    }

    @NonNull
    private SparseArray<Layers> getLayersList() {
        if (layerList == null) {
            layerList = new SparseArray<>();
        }
        return layerList;
    }

    private ViewGroup getContainer() {
        if (container == null) {
            container = containerId == View.NO_ID ? host.getDefaultContainer() : (ViewGroup) host.getView(containerId);
            // TODO for API < 11
            container.setSaveFromParentEnabled(false);
        }
        return container;
    }
}
