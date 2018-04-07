package com.psliusar.layers;

import android.support.annotation.NonNull;

public class MockedPresenter extends Presenter<MockedModel> {

    private final MockedLayer layer;

    public MockedPresenter(@NonNull MockedLayer layer) {
        this.layer = layer;
    }

    @Override
    protected MockedModel onCreateModel() {
        return new MockedModel();
    }
}
