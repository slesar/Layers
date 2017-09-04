package com.psliusar.layers.track;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class MockedListener implements Track.OnTrackListener<String, Integer> {

    private int onTrackFinishedCalled = 0;
    private int onTrackErrorCalled = 0;
    private int onTrackRestartCalled = 0;
    private int onTrackProgressCalled = 0;

    private String value;

    @Override
    public void onTrackFinished(@NonNull Track<String, Integer> track, @Nullable String value) {
        onTrackFinishedCalled++;
        this.value = value;
    }

    @Override
    public void onTrackError(@NonNull Track<String, Integer> track, @NonNull Throwable throwable) {
        throwable.printStackTrace();
        onTrackErrorCalled++;
    }

    @Override
    public void onTrackRestart(@NonNull Track<String, Integer> track) {
        onTrackRestartCalled++;
    }

    @Override
    public void onTrackProgress(@NonNull Track<String, Integer> track, @Nullable Integer progress) {
        onTrackProgressCalled++;
    }

    public boolean singleShot() {
        return onTrackFinishedCalled == 1
                && onTrackErrorCalled == 0
                && onTrackRestartCalled == 0;
    }

    public boolean shotTimes(int times) {
        return onTrackFinishedCalled == times;
    }

    public boolean restartTimes(int times) {
        return onTrackRestartCalled == times;
    }

    @Nullable
    public String getValue() {
        return value;
    }
}
