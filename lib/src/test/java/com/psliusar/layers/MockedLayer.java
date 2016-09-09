package com.psliusar.layers;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

public class MockedLayer extends Layer<MockedPresenter> {

    private static final String STATE_STRING_KEY = "STATE_STRING_KEY";
    private static final String STATE_STRING_VALUE = "STATE_STRING_VALUE";

    private static final String ARGS_KEY = "ARGS_KEY";
    private static final String ARGS_VALUE = "ARGS_VALUE";

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

    private boolean valueRestored = false;
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

    public boolean isValueRestored() {
        return valueRestored;
    }

    public boolean isArgumentsPassed() {
        return argumentsPassed;
    }

    @Override
    void create(@NonNull LayersHost host, @Nullable Bundle arguments, @Nullable String name) {
        super.create(host, arguments, name);
        createCalled++;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        onCreateCalled++;
        valueRestored = savedState != null && STATE_STRING_VALUE.equals(savedState.getString(STATE_STRING_VALUE));
        argumentsPassed = getArguments() != null && ARGS_VALUE.equals(getArguments().getString(ARGS_KEY));
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        onCreateViewCalled++;
        return inflate(0, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        onBindViewCalled++;
    }

    @Override
    void restoreViewState(@NonNull SparseArray<Parcelable> inState) {
        super.restoreViewState(inState);
        restoreViewStateCalled++;
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
        // TODO counter
        outState.putString(STATE_STRING_KEY, STATE_STRING_VALUE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyCalled++;
    }

    @Override
    void destroy() {
        super.destroy();
        destroyCalled++;
    }

    @Override
    protected MockedPresenter onCreatePresenter() {
        onCreatePresenterCalled++;
        return new MockedPresenter();
    }
}
