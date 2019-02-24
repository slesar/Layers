package com.psliusar.layers;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.psliusar.layers.binder.Binder;
import com.psliusar.layers.binder.BinderHolder;
import com.psliusar.layers.binder.ObjectBinder;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Layer<VM extends ViewModel> implements LayersHost, View.OnClickListener, BinderHolder {

    private static final String SAVED_STATE_CHILD_LAYERS = "LAYER.SAVED_STATE_CHILD_LAYERS";
    private static final String SAVED_STATE_CUSTOM = "LAYER.SAVED_STATE_CUSTOM";

    private LayersHost host;

    @Nullable
    private Layers layers;

    @Nullable
    VM viewModel;

    View view;

    @Nullable
    private String name;

    @Nullable
    private Bundle arguments;

    boolean attached;

    private boolean fromSavedState;

    private boolean finishing;

    private ObjectBinder layerBinder;

    private LayerDelegate delegate;

    public Layer() {
        // Default constructor
    }

    @NonNull
    public final VM getViewModel() {
        if (viewModel == null) {
            viewModel = onCreateViewModel();
        }
        if (viewModel == null) {
            throw new NullPointerException("ViewModel was not provided");
        }
        return viewModel;
    }

    @Nullable
    protected abstract VM onCreateViewModel();

    public boolean onBackPressed() {
        return layers != null && layers.onBackPressed();
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
        if (delegate != null) {
            delegate.onCreate(savedState);
        }
    }

    @Nullable
    protected abstract View onCreateView(@Nullable Bundle savedState, @Nullable ViewGroup parent);

    protected void onBindView(@Nullable Bundle savedState, @NonNull View view) {
        Binder.bind(this, view);
        if (delegate != null) {
            delegate.onBindView(savedState, view);
        }
    }

    void restoreViewState(@Nullable SparseArray<Parcelable> inState) {
        if (inState != null) {
            view.restoreHierarchyState(inState);
        }
        if (layers != null) {
            layers.resumeView();
        }
        if (delegate != null) {
            delegate.restoreViewState(inState);
        }
    }

    /**
     * After attach
     */
    protected void onAttach() {
        if (delegate != null) {
            delegate.onAttach();
        }
    }

    /**
     * Before detach
     */
    protected void onDetach() {
        if (delegate != null) {
            delegate.onDetach();
        }
    }

    void saveViewState(@NonNull SparseArray<Parcelable> outState) {
        view.saveHierarchyState(outState);
        if (layers != null) {
            layers.pauseView();
        }
        if (delegate != null) {
            delegate.saveViewState(outState);
        }
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
        if (delegate != null) {
            delegate.saveLayerState(outState);
        }
    }

    protected void onSaveLayerState(@NonNull Bundle outState) {
        Binder.save(this, outState);
    }

    void destroyView() {
        onDestroyView();
        if (viewModel != null) {
            viewModel.onUnSubscribe();
        }
        if (layers != null) {
            layers.destroy();
        }
        Binder.unbind(this);
        if (delegate != null) {
            delegate.onDestroyView();
        }
    }

    protected void onDestroyView() {

    }

    void destroy(boolean finish) {
        finishing = finish;
        onDestroy();
        if (finish && viewModel != null) {
            viewModel.onDestroy();
            viewModel = null;
        }
    }

    protected void onDestroy() {

    }

    public boolean isAttached() {
        return attached;
    }

    public boolean isViewInLayout() {
        if (delegate != null) {
            return delegate.isViewInLayout();
        }
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

    public void setDelegate(@Nullable LayerDelegate delegate) {
        this.delegate = delegate;
    }

    @Nullable
    public <D extends LayerDelegate> D getDelegate() {
        return (D) delegate;
    }

    /**
     * Removes Layer from the stack.
     */
    public void dismiss() {
        if (isAttached()) {
            if (delegate != null) {
                delegate.onDismiss();
            } else {
                host.getLayers().remove(this).commit();
            }
        }
    }

    @NonNull
    protected LayoutInflater getLayoutInflater() {
        LayoutInflater inflater = null;
        if (delegate != null) {
            inflater = delegate.getLayoutInflater();
        }
        if (inflater == null) {
            inflater = host.getActivity().getLayoutInflater();
        }
        return inflater;
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
    public Activity getActivity() {
        return host.getActivity();
    }

    @Nullable
    public Layer<?> getParentLayer() {
        return (host instanceof Layer) ? (Layer) host : null;
    }

    @Nullable
    public <T> T optParent(@NonNull Class<T> parentClass) {
        final Layer<?> parent = getParentLayer();
        if (parent == null) {
            final Activity activity = host.getActivity();
            return parentClass.isInstance(activity) ? parentClass.cast(activity) : null;
        } else if (parentClass.isInstance(parent)) {
            return parentClass.cast(parent);
        } else {
            return parent.optParent(parentClass);
        }
    }

    @NonNull
    public <T> T getParent(@NonNull Class<T> parentClass) {
        final T parent = optParent(parentClass);
        if (parent == null) {
            throw new NullPointerException("A parent implementing " + parentClass + " not found");
        }
        return parent;
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
    public void onClick(@NonNull View v) {
        
    }

    @Nullable
    public Animator getAnimation(@AnimationType int animationType) {
        return null;
    }

    @Nullable
    public ObjectBinder getObjectBinder() {
        return layerBinder;
    }

    public void setObjectBinder(@NonNull ObjectBinder objectBinder) {
        layerBinder = objectBinder;
    }

    @NonNull
    @Override
    public String toString() {
        return "Layer{" +
                "name='" + name + '\'' +
                ", arguments=" + arguments +
                ", attached=" + attached +
                ", fromSavedState=" + fromSavedState +
                ", finishing=" + finishing +
                '}';
    }
}
