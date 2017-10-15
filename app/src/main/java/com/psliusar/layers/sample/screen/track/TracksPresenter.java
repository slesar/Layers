package com.psliusar.layers.sample.screen.track;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.Model;
import com.psliusar.layers.Presenter;
import com.psliusar.layers.track.AsyncTrack;
import com.psliusar.layers.track.SimpleTrackCallbacks;
import com.psliusar.layers.track.Track;
import com.psliusar.layers.track.TrackManager;

public class TracksPresenter extends Presenter<Model, TracksLayer> {

    private static final int TRACK_SYNC = 1;
    private static final int TRACK_ASYNC = 2;

    public TracksPresenter(@NonNull TracksLayer layer) {
        super(layer);
    }

    private final SimpleTrackCallbacks<Integer, Integer> syncTaskCallbacks = new SimpleTrackCallbacks<Integer, Integer>() {
        @NonNull
        @Override
        public Track<Integer, Integer> createTrack(int trackId) {
            return new SampleSyncTrack();
        }

        @Override
        public void onTrackFinished(int trackId, @NonNull Track<Integer, Integer> track, @Nullable Integer value) {
            getLayer().setSyncResult(value == null ? null : value.toString());
        }

        @Override
        public void onTrackRestart(int trackId, @NonNull Track<Integer, Integer> track) {
            getLayer().setSyncResult(null);
        }
    };

    private final SimpleTrackCallbacks<Integer, Integer> asyncTaskCallbacks = new SimpleTrackCallbacks<Integer, Integer>() {
        @NonNull
        @Override
        public Track<Integer, Integer> createTrack(int trackId) {
            return new SampleAsyncTrack();
        }

        @Override
        public void onTrackFinished(int trackId, @NonNull Track<Integer, Integer> track, @Nullable Integer value) {
            getLayer().setAsyncResult(value == null ? null : value.toString());
            getLayer().setAsyncProgress(100);
        }

        @Override
        public void onTrackRestart(int trackId, @NonNull Track<Integer, Integer> track) {
            getLayer().setAsyncResult(null);
        }

        @Override
        public void onTrackProgress(int trackId, @NonNull Track<Integer, Integer> track, @Nullable Integer progress) {
            getLayer().setAsyncProgress(progress == null ? 0 : progress);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        final TracksLayer layer = getLayer();
        if (layer.trackManager == null) {
            layer.trackManager = new TrackManager();
        }

        final TrackManager trackManager = layer.trackManager;

        trackManager.registerTrackCallbacks(TRACK_SYNC, syncTaskCallbacks).start();
        trackManager.registerTrackCallbacks(TRACK_ASYNC, asyncTaskCallbacks).start();
    }

    private static class SampleSyncTrack extends Track<Integer, Integer> {

        @Override
        protected void doBlocking() {
            final Integer result = (int) (Math.random() * 255);
            done(result);
        }
    }

    private static class SampleAsyncTrack extends AsyncTrack<Integer, Integer> {

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
