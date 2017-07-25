package com.psliusar.layers;

public class MockedPresenter extends Presenter<MockedModel, MockedLayer> {

    @Override
    protected MockedModel onCreateModel() {
        return new MockedModel();
    }
}
