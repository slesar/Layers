package com.psliusar.layers.sample.screen.track;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.psliusar.layers.Layer;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.sample.R;
import com.psliusar.layers.sample.screen.listener.ListenerModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TracksLayer extends Layer<TracksViewModel> {

    @Bind(R.id.track_sync_result) TextView syncResult;
    @Bind(R.id.track_async_progress) ProgressBar asyncProgressBar;
    @Bind(R.id.track_async_result) TextView asyncResult;

    @Nullable
    @Override
    protected TracksViewModel onCreateViewModel() {
        return new TracksViewModel();
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable Bundle savedState, @Nullable ViewGroup parent) {
        return inflate(R.layout.screen_tracks, parent);
    }

    @Override
    protected void onBindView(@Nullable Bundle savedState, @NonNull View view) {
        super.onBindView(savedState, view);

        getViewModel().getSyncResult(new ListenerModel.Updatable<CharSequence>() {
            @Override
            public void onUpdate(@Nullable CharSequence value) {
                syncResult.setText(value);
            }
        });
        getViewModel().getAsyncStatus(new ListenerModel.Updatable<TrackModel.AsyncTrackStatus>() {
            @Override
            public void onUpdate(@Nullable TrackModel.AsyncTrackStatus value) {
                asyncResult.setText(value.result);
                asyncProgressBar.setProgress(value.progress);
            }
        });
    }
}
