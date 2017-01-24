package com.psliusar.layers;

/**
 * Created by slesar on 8/6/16.
 */
public class MockedPresenter extends Presenter<MockedModel, MockedLayer> {

    @Override
    protected MockedModel onCreateModel() {
        return new MockedModel();
    }
}
