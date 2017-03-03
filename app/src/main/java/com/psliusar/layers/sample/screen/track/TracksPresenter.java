package com.psliusar.layers.sample.screen.track;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.Model;
import com.psliusar.layers.Presenter;
import com.psliusar.layers.track.Track;

public class TracksPresenter extends Presenter<Model, TracksLayer> {

    Track.OnTrackListener<Integer, Integer> getSyncTrackListener() {
        return new Track.OnTrackListener<Integer, Integer>() {
            @Override
            public void onTrackFinished(@NonNull Track<Integer, Integer> track, @Nullable Integer value) {
                getLayer().setSyncResult(value == null ? null : value.toString());
            }

            @Override
            public void onTrackError(@NonNull Track<Integer, Integer> track, @NonNull Throwable throwable) {

            }

            @Override
            public void onTrackRestart(@NonNull Track<Integer, Integer> track) {
                getLayer().setSyncResult(null);
            }

            @Override
            public void onTrackProgress(@NonNull Track<Integer, Integer> track, @Nullable Integer progress) {

            }
        };
    }

    Track.OnTrackListener<Integer, Integer> getAsyncTrackListener() {
        return new Track.OnTrackListener<Integer, Integer>() {
            @Override
            public void onTrackFinished(@NonNull Track<Integer, Integer> track, @Nullable Integer value) {
                getLayer().setAsyncResult(value == null ? null : value.toString());
                getLayer().setAsyncProgress(100);
            }

            @Override
            public void onTrackError(@NonNull Track<Integer, Integer> track, @NonNull Throwable throwable) {

            }

            @Override
            public void onTrackRestart(@NonNull Track<Integer, Integer> track) {
                getLayer().setAsyncResult(null);
            }

            @Override
            public void onTrackProgress(@NonNull Track<Integer, Integer> track, @Nullable Integer progress) {
                getLayer().setAsyncProgress(progress == null ? 0 : progress);
            }
        };
    }
}
