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
    protected Track<Integer, Integer> syncTrack;

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
        final TracksPresenter presenter = getPresenter();
        syncTrack.subscribe(presenter.getSyncTrackListener());
        syncTrack.start();

        asyncTrack.subscribe(presenter.getAsyncTrackListener());
        asyncTrack.start();
    }

    @Override
    protected void onDestroyView() {
        super.onDestroyView();
        syncTrack.unsubscribe();
        asyncTrack.unsubscribe();
    }

    void setSyncResult(@Nullable CharSequence text) {
        syncResult.setText(text);
    }

    void setAsyncResult(@Nullable CharSequence text) {
        asyncResult.setText(text);
    }

    void setAsyncProgress(int progress) {
        asyncProgressBar.setProgress(progress);
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
        protected Integer doInBackground() throws Throwable {
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
