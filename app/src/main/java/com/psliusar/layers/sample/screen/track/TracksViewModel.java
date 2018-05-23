package com.psliusar.layers.sample.screen.track;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.Model;
import com.psliusar.layers.ViewModel;
import com.psliusar.layers.sample.screen.listener.ListenerModel;
import com.psliusar.layers.track.AsyncTrack;
import com.psliusar.layers.track.SimpleTrackCallbacks;
import com.psliusar.layers.track.Track;

public class TracksViewModel extends ViewModel<Model> {

    private static final int TRACK_SYNC = 1;
    private static final int TRACK_ASYNC = 2;

    public TracksViewModel() {
        super(null);
    }

    void getSyncResult(@NonNull final ListenerModel.Updatable<CharSequence> updatable) {
        final SimpleTrackCallbacks<Integer, Integer> syncTaskCallbacks = new SimpleTrackCallbacks<Integer, Integer>() {
            @NonNull
            @Override
            public Track<Integer, Integer> createTrack(int trackId) {
                return new SampleSyncTrack();
            }

            @Override
            public void onTrackFinished(int trackId, @NonNull Track<Integer, Integer> track, @Nullable Integer value) {
                updatable.onUpdate(value == null ? null : value.toString());
            }

            @Override
            public void onTrackRestart(int trackId, @NonNull Track<Integer, Integer> track) {
                updatable.onUpdate(null);
            }
        };
        getTrackManager().registerTrackCallbacks(TRACK_SYNC, syncTaskCallbacks).start();
    }

    void getAsyncStatus(@NonNull final ListenerModel.Updatable<TrackModel.AsyncTrackStatus> updatable) {
        final SimpleTrackCallbacks<Integer, Integer> asyncTaskCallbacks = new SimpleTrackCallbacks<Integer, Integer>() {

            private Integer result = null;
            private int progress = 0;

            @NonNull
            @Override
            public Track<Integer, Integer> createTrack(int trackId) {
                return new SampleAsyncTrack();
            }

            @Override
            public void onTrackFinished(int trackId, @NonNull Track<Integer, Integer> track, @Nullable Integer value) {
                result = value;
                progress = 100;
                updatable.onUpdate(new TrackModel.AsyncTrackStatus(result, progress));
            }

            @Override
            public void onTrackRestart(int trackId, @NonNull Track<Integer, Integer> track) {
                result = null;
                progress = 0;
                updatable.onUpdate(new TrackModel.AsyncTrackStatus(result, progress));
            }

            @Override
            public void onTrackProgress(int trackId, @NonNull Track<Integer, Integer> track, @Nullable Integer progress) {
                this.progress = progress;
                updatable.onUpdate(new TrackModel.AsyncTrackStatus(result, progress));
            }
        };
        getTrackManager().registerTrackCallbacks(TRACK_ASYNC, asyncTaskCallbacks).start();
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
