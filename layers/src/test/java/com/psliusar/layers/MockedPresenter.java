package com.psliusar.layers;

import android.support.annotation.NonNull;

public class MockedPresenter extends Presenter<MockedModel, MockedLayer> {

    public MockedPresenter(@NonNull MockedLayer layer) {
        super(layer);
    }

    @Override
    protected MockedModel onCreateModel() {
        return new MockedModel();
    }
}
