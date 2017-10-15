package com.psliusar.layers.sample.screen.home;

import android.support.annotation.NonNull;

import com.psliusar.layers.Model;
import com.psliusar.layers.Presenter;
import com.psliusar.layers.sample.MainActivity;

public class HomePresenter extends Presenter<Model, HomeLayer> {

    public HomePresenter(@NonNull HomeLayer layer) {
        super(layer);
    }

    @NonNull
    protected MainActivity getMainActivity() {
        return (MainActivity) getHost().getActivity();
    }

    void stackClick() {
        getMainActivity().addToStack("Level", 1, true);
    }

    void childrenClick() {
        getMainActivity().showChildrenLayers();
    }

    void dialogClick() {
        getMainActivity().showDialogLayers();
    }

    void listenerClick() {
        getMainActivity().showActivityListener();
    }

    void saveClick() {
        getMainActivity().showSaveState();
    }

    void tasksClick() {
        getMainActivity().showTracks();
    }
}
