package com.psliusar.layers.sample.screen.home;

import android.support.annotation.NonNull;

import com.psliusar.layers.Model;
import com.psliusar.layers.Presenter;
import com.psliusar.layers.sample.MainActivity;

public class HomePresenter extends Presenter<Model> {

    private final MainActivity activity;

    public HomePresenter(@NonNull MainActivity activity) {
        this.activity = activity;
    }

    void stackClick() {
        activity.addToStack("Level", 1, true);
    }

    void childrenClick() {
        activity.showChildrenLayers();
    }

    void dialogClick() {
        activity.showDialogLayers();
    }

    void listenerClick() {
        activity.showActivityListener();
    }

    void saveClick() {
        activity.showSaveState();
    }

    void tasksClick() {
        activity.showTracks();
    }
}
