package com.psliusar.layers.sample.screen.home;

import android.support.annotation.NonNull;

import com.psliusar.layers.Model;
import com.psliusar.layers.ViewModel;
import com.psliusar.layers.sample.MainActivity;

public class HomeViewModel extends ViewModel<Model> {

    public HomeViewModel() {
        super(null);
    }

    void stackClick(@NonNull MainActivity activity) {
        activity.addToStack("Level", 1, true);
    }

    void childrenClick(@NonNull MainActivity activity) {
        activity.showChildrenLayers();
    }

    void dialogClick(@NonNull MainActivity activity) {
        activity.showDialogLayers();
    }

    void listenerClick(@NonNull MainActivity activity) {
        activity.showActivityListener();
    }

    void saveClick(@NonNull MainActivity activity) {
        activity.showSaveState();
    }

    void tasksClick(@NonNull MainActivity activity) {
        activity.showTracks();
    }
}
