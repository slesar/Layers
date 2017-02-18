package com.psliusar.layers.sample.screen.track;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.psliusar.layers.Layer;
import com.psliusar.layers.binder.Save;
import com.psliusar.layers.track.AsyncTrack;
import com.psliusar.layers.track.Track;
import com.psliusar.layers.sample.R;

public class TracksLayer extends Layer<TracksPresenter> {

    @Save
    protected ProgressTrack progressTrack;

    @Override
    protected TracksPresenter onCreatePresenter() {
        return new TracksPresenter();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        if (progressTrack == null) {
            progressTrack = new ProgressTrack();
        }
        progressTrack.subscribe(new Track.OnTrackListener<Integer>() {
            @Override
            public void onTrackFinished(@NonNull Track<Integer> track, @Nullable Integer value) {

            }

            @Override
            public void onTrackRestart(@NonNull Track<Integer> track) {

            }
        });
        progressTrack.start();
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_tracks, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

    }

    protected static class ProgressTrack extends AsyncTrack<Integer> {

        ProgressTrack() {
        }

        @Override
        protected void doInBackground() {

        }
    }
}
