package com.psliusar.layers.sample.screen.home;

import com.psliusar.layers.ViewModel;
import com.psliusar.layers.sample.MainActivity;

import androidx.annotation.NonNull;

public class HomeViewModel extends ViewModel {

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
