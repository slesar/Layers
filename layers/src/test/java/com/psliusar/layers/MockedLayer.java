package com.psliusar.layers;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MockedLayer extends Layer<MockedViewModel> {

    private static final String STATE_STRING_KEY = "STATE_STRING_KEY";
    private static final String STATE_STRING_VALUE = "STATE_STRING_VALUE";

    private static final String ARGS_KEY = "ARGS_KEY";
    private static final String ARGS_VALUE = "ARGS_VALUE";

    @NonNull
    public static Bundle createArguments() {
        Bundle bundle = new Bundle();
        bundle.putString(ARGS_KEY, ARGS_VALUE);
        return bundle;
    }

    private int createCalled = 0;
    private int onCreatePresenterCalled = 0;
    private int onCreateCalled = 0;
    private int onCreateViewCalled = 0;
    private int onBindViewCalled = 0;
    private int restoreViewStateCalled = 0;
    private int onAttachCalled = 0;
    private int onDetachCalled = 0;
    private int saveViewStateCalled = 0;
    private int onDestroyViewCalled = 0;
    private int onDestroyCalled = 0;
    private int destroyCalled = 0;

    private boolean stateRestored = false;
    private boolean viewStateRestored = false;
    private boolean argumentsPassed = false;

    public int getCreateCalled() {
        return createCalled;
    }

    public int getOnCreatePresenterCalled() {
        return onCreatePresenterCalled;
    }

    public int getOnCreateCalled() {
        return onCreateCalled;
    }

    public int getOnCreateViewCalled() {
        return onCreateViewCalled;
    }

    public int getOnBindViewCalled() {
        return onBindViewCalled;
    }

    public int getRestoreViewStateCalled() {
        return restoreViewStateCalled;
    }

    public int getOnAttachCalled() {
        return onAttachCalled;
    }

    public int getOnDetachCalled() {
        return onDetachCalled;
    }

    public int getSaveViewStateCalled() {
        return saveViewStateCalled;
    }

    public int getOnDestroyViewCalled() {
        return onDestroyViewCalled;
    }

    public int getOnDestroyCalled() {
        return onDestroyCalled;
    }

    public int getDestroyCalled() {
        return destroyCalled;
    }

    public boolean isStateRestored() {
        return stateRestored;
    }

    public boolean isViewStateRestored() {
        return viewStateRestored;
    }

    public boolean isArgumentsPassed() {
        return argumentsPassed;
    }

    @Override
    void create(@NonNull LayersHost host, @Nullable Bundle arguments, @Nullable String name, @Nullable Bundle savedState) {
        super.create(host, arguments, name, savedState);
        createCalled++;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        onCreateCalled++;
        stateRestored = savedState != null && STATE_STRING_VALUE.equals(savedState.getString(STATE_STRING_KEY));
        argumentsPassed = getArguments() != null && ARGS_VALUE.equals(getArguments().getString(ARGS_KEY));
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable Bundle savedState, @Nullable ViewGroup parent) {
        onCreateViewCalled++;
        return inflate(0, parent);
    }

    @Override
    protected void onBindView(@Nullable Bundle savedState, @NonNull View view) {
        super.onBindView(savedState, view);
        onBindViewCalled++;
    }

    @Override
    void restoreViewState(@Nullable SparseArray<Parcelable> inState) {
        super.restoreViewState(inState);
        restoreViewStateCalled++;
        viewStateRestored = inState != null;
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        onAttachCalled++;
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        onDetachCalled++;
    }

    @Override
    void saveViewState(@NonNull SparseArray<Parcelable> outState) {
        super.saveViewState(outState);
        saveViewStateCalled++;
    }

    @Override
    protected void onDestroyView() {
        super.onDestroyView();
        onDestroyViewCalled++;
    }

    @Override
    void saveLayerState(@NonNull Bundle outState) {
        super.saveLayerState(outState);
    }

    @Override
    protected void onSaveLayerState(@NonNull Bundle outState) {
        super.onSaveLayerState(outState);
        // TODO counter
        outState.putString(STATE_STRING_KEY, STATE_STRING_VALUE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyCalled++;
    }

    @Override
    void destroy(boolean finish) {
        super.destroy(finish);
        destroyCalled++;
    }

    @Override
    protected MockedViewModel onCreateViewModel() {
        onCreatePresenterCalled++;
        return new MockedViewModel();
    }
}
