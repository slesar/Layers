package com.psliusar.layers.sample.screen.track;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.psliusar.layers.Layer;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.binder.Save;
import com.psliusar.layers.track.AsyncTrack;
import com.psliusar.layers.track.Track;
import com.psliusar.layers.sample.R;

public class TracksLayer extends Layer<TracksPresenter> {

    @Save
    protected SampleSyncTrack syncTrack;

    @Bind(R.id.track_sync_result)
    protected TextView syncResult;

    @Save
    protected SampleAsyncTrack asyncTrack;

    @Bind(R.id.track_async_progress)
    protected ProgressBar asyncProgressBar;

    @Bind(R.id.track_async_result)
    protected TextView asyncResult;

    @Override
    protected TracksPresenter onCreatePresenter() {
        return new TracksPresenter();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        if (syncTrack == null) {
            syncTrack = new SampleSyncTrack();
        }
        if (asyncTrack == null) {
            asyncTrack = new SampleAsyncTrack();
        }
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_tracks, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        syncTrack.subscribe(new Track.OnTrackListener<Integer, Integer>() {
            @Override
            public void onTrackFinished(@NonNull Track<Integer, Integer> track, @Nullable Integer value) {
                syncResult.setText(value == null ? null : value.toString());
            }

            @Override
            public void onTrackError(@NonNull Track<Integer, Integer> track, @NonNull Throwable throwable) {

            }

            @Override
            public void onTrackRestart(@NonNull Track<Integer, Integer> track) {
                syncResult.setText(null);
            }

            @Override
            public void onTrackProgress(@NonNull Track<Integer, Integer> track, @Nullable Integer progress) {

            }
        });
        syncTrack.start();

        asyncTrack.subscribe(new Track.OnTrackListener<Integer, Integer>() {
            @Override
            public void onTrackFinished(@NonNull Track<Integer, Integer> track, @Nullable Integer value) {
                asyncResult.setText(value == null ? null : value.toString());
                asyncProgressBar.setProgress(100);
            }

            @Override
            public void onTrackError(@NonNull Track<Integer, Integer> track, @NonNull Throwable throwable) {

            }

            @Override
            public void onTrackRestart(@NonNull Track<Integer, Integer> track) {
                asyncResult.setText(null);
            }

            @Override
            public void onTrackProgress(@NonNull Track<Integer, Integer> track, @Nullable Integer progress) {
                asyncProgressBar.setProgress(progress == null ? 0 : progress);
            }
        });
        asyncTrack.start();
    }

    protected static class SampleSyncTrack extends Track<Integer, Integer> {

        SampleSyncTrack() {
        }

        @Override
        protected void doBlocking() {
            final Integer result = (int) (Math.random() * 255);
            done(result);
        }
    }

    protected static class SampleAsyncTrack extends AsyncTrack<Integer, Integer> {

        SampleAsyncTrack() {
        }

        @Override
        protected Integer doInBackground() {
            for (int i = 0 ; i < 100; i++) {
                if (i % 5 == 0) {
                    postProgress(i);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return i;
                }
            }
            return 100;
        }
    }
}
