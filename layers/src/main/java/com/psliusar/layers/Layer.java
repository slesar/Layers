package com.psliusar.layers;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.psliusar.layers.binder.Binder;
import com.psliusar.layers.binder.BinderHolder;
import com.psliusar.layers.binder.ObjectBinder;

public abstract class Layer<P extends Presenter> implements LayersHost, View.OnClickListener, BinderHolder {

    private static final String SAVED_STATE_CHILD_LAYERS = "LAYER.SAVED_STATE_CHILD_LAYERS";
    private static final String SAVED_STATE_CUSTOM = "LAYER.SAVED_STATE_CUSTOM";

    private LayersHost host;

    @Nullable
    private Layers layers;

    @Nullable
    private P presenter;

    View view;

    @Nullable
    private String name;

    @Nullable
    private Bundle arguments;

    boolean attached;

    private boolean fromSavedState;

    private boolean finishing;

    private ObjectBinder layerBinder;

    public Layer() {
        // Default constructor
    }

    public final P getPresenter() {
        if (presenter == null) {
            presenter = onCreatePresenter();
            if (presenter != null) {
                //noinspection unchecked
                presenter.create(host, this);
            }
        }
        return presenter;
    }

    protected abstract P onCreatePresenter();

    public boolean onBackPressed() {
        if (layers != null && layers.getStackSize() > 1) {
            final Layer<?> topLayer = layers.peek();
            if (topLayer != null && topLayer.onBackPressed()) {
                return true;
            }
        }
        return false;
    }

    void create(@NonNull LayersHost host, @Nullable Bundle arguments, @Nullable String name, @Nullable Bundle savedState) {
        this.host = host;
        this.arguments = arguments;
        this.name = name;
        fromSavedState = savedState != null;

        if (savedState != null) {
            final Bundle layersState = savedState.getBundle(SAVED_STATE_CHILD_LAYERS);
            if (layersState != null) {
                layers = new Layers(this, layersState);
            }
        }
        onCreate(savedState == null ? null : savedState.getBundle(SAVED_STATE_CUSTOM));
    }

    protected void onCreate(@Nullable Bundle savedState) {
        if (savedState != null) {
            Binder.restore(this, savedState);
        }
    }

    @Nullable
    protected abstract View onCreateView(@Nullable ViewGroup parent);

    protected void onBindView(@NonNull View view) {
        Binder.bind(this, view);
    }

    void restoreLayerState() {
        if (layers != null) {
            layers.restoreState();
        }
    }

    void restoreViewState(@Nullable SparseArray<Parcelable> inState) {
        if (inState != null) {
            view.restoreHierarchyState(inState);
        }
        if (layers != null) {
            layers.resumeView();
        }
    }

    /**
     * After attach
     */
    protected void onAttach() {

    }

    /**
     * Before detach
     */
    protected void onDetach() {

    }

    void saveViewState(@NonNull SparseArray<Parcelable> outState) {
        view.saveHierarchyState(outState);
    }

    void saveLayerState(@NonNull Bundle outState) {
        if (layers != null) {
            final Bundle layersState = layers.saveState();
            if (layersState != null) {
                outState.putBundle(SAVED_STATE_CHILD_LAYERS, layersState);
            }
        }
        final Bundle customState = new Bundle();
        onSaveLayerState(customState);
        if (customState.size() > 0) {
            outState.putBundle(SAVED_STATE_CUSTOM, customState);
        }
    }

    protected void onSaveLayerState(@NonNull Bundle outState) {
        Binder.save(this, outState);
    }

    void destroyView() {
        if (layers != null) {
            layers.destroy();
        }
        onDestroyView();
        Binder.unbind(this);
    }

    protected void onDestroyView() {

    }

    void destroy(boolean finish) {
        finishing = finish;
        onDestroy();
        if (presenter != null) {
            presenter.destroy();
            presenter = null;
        }
    }

    protected void onDestroy() {

    }

    public boolean isAttached() {
        return attached;
    }

    public boolean isViewInLayout() {
        return true;
    }

    public boolean isFromSavedState() {
        return fromSavedState;
    }

    public boolean isFinishing() {
        return finishing;
    }

    @Nullable
    public Bundle getArguments() {
        return arguments;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @NonNull
    public Context getContext() {
        return host.getActivity().getApplicationContext();
    }

    @NonNull
    public LayersHost getHost() {
        return host;
    }

    @NonNull
    protected LayoutInflater getLayoutInflater() {
        return host.getActivity().getLayoutInflater();
    }

    @NonNull
    protected <V extends View> V inflate(@LayoutRes int layoutRes, @Nullable ViewGroup parent) {
        //noinspection unchecked
        return (V) getLayoutInflater().inflate(layoutRes, parent, false);
    }

    @Nullable
    public View getView() {
        return view;
    }

    @NonNull
    public <T extends View> T getView(@IdRes int id) {
        final T view = findView(id);
        if (view == null) {
            throw new IllegalArgumentException("Failed to find View with ID 0x" + Integer.toHexString(id));
        }
        return view;
    }

    @Nullable
    public <T extends View> T findView(@IdRes int id) {
        if (view == null) {
            return null;
        }
        //noinspection unchecked
        return (T) view.findViewById(id);
    }

    @NonNull
    public ViewGroup getDefaultContainer() {
        return (ViewGroup) view;
    }

    @NonNull
    public Layers getLayers() {
        if (layers == null) {
            layers = new Layers(this, null);
        }
        return layers;
    }

    @NonNull
    @Override
    public Layers getLayers(@IdRes int viewId) {
        return getLayers().at(viewId);
    }

    @NonNull
    @Override
    public Activity getActivity() {
        return host.getActivity();
    }

    @Nullable
    public Layer<?> getParentLayer() {
        return (host instanceof Layer) ? (Layer) host : null;
    }

    @Nullable
    public <T> T getParent(@NonNull Class<T> parentClass) {
        final Layer<?> parent = getParentLayer();
        if (parent == null) {
            final Activity activity = host.getActivity();
            return parentClass.isInstance(activity) ? parentClass.cast(activity) : null;
        } else if (parentClass.isInstance(parent)) {
            return parentClass.cast(parent);
        } else {
            return parent.getParent(parentClass);
        }
    }

    protected void bindClickListener(@NonNull View.OnClickListener listener, View... views) {
        final int size = views.length;
        for (int i = 0; i < size; i++) {
            views[i].setOnClickListener(listener);
        }
    }

    protected void bindClickListener(@NonNull View.OnClickListener listener, @IdRes int... ids) {
        final int size = ids.length;
        for (int i = 0; i < size; i++) {
            getView(ids[i]).setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View v) {
        
    }

    @Nullable
    public Animator getAnimation(@Transition.AnimationType int animationType) {
        return null;
    }

    @Nullable
    public ObjectBinder getObjectBinder() {
        return layerBinder;
    }

    public void setObjectBinder(@NonNull ObjectBinder objectBinder) {
        layerBinder = objectBinder;
    }
}
