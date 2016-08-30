package com.psliusar.layers;

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

public abstract class Layer<P extends Presenter> implements LayersHost {

    LayersHost host;
    Layers layers;
    P presenter;
    View view;
    String name;
    Bundle arguments;
    // TODO ?
    boolean attached;
    private LayoutInflater layoutInflater;

    public Layer() {
        // Default constructor
    }

    public final P getPresenter() {
        if (presenter == null) {
            presenter = onCreatePresenter();
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

    void create(@NonNull LayersHost host, @Nullable Bundle arguments, @Nullable String name) {
        this.host = host;
        this.arguments = arguments;
        this.name = name;
    }

    protected void onCreate(@Nullable Bundle savedState) {
        layers = new Layers(this, savedState);
    }

    @Nullable
    protected abstract View onCreateView(@NonNull ViewGroup parent);

    protected void onBindView(@NonNull View view) {

    }

    void restoreViewState(@NonNull SparseArray<Parcelable> inState) {
        view.restoreHierarchyState(inState);
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

    protected void onDestroyView() {

    }

    protected void onDestroy(@Nullable Bundle outState) {
        if (layers != null) {
            layers.saveState(outState);
        }
    }

    void destroy() {
        presenter = null;
    }

    protected void _requestSaveState() {

    }

    public boolean isAttached() {
        return attached;
    }

    @Nullable
    public Bundle getArguments() {
        return arguments;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public Context getContext() {
        return host.getActivity().getApplicationContext();
    }

    public LayersHost getHost() {
        return host;
    }

    @NonNull
    protected LayoutInflater getLayoutInflater() {
        if (layoutInflater == null) {
            layoutInflater = host.getActivity().getLayoutInflater();
        }
        return layoutInflater;
    }

    protected <V extends View> V inflate(@LayoutRes int layoutRes, @NonNull ViewGroup parent) {
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
            // TODO
            throw new IllegalArgumentException("");
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
        return layers;
    }

    @NonNull
    @Override
    public Activity getActivity() {
        return host.getActivity();
    }

    public Layer<?> getParentLayer() {
        return host.getParentLayer();
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

    protected void onClick(@NonNull View.OnClickListener listener, View... views) {
        for (View view : views) {
            view.setOnClickListener(listener);
        }
    }

    protected void onClick(@NonNull View.OnClickListener listener, @IdRes int... ids) {
        for (int id : ids) {
            getView(id).setOnClickListener(listener);
        }
    }
}
