package com.psliusar.layers.sample.screen.track;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.psliusar.layers.Layer;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.sample.R;
import com.psliusar.layers.sample.screen.listener.ListenerModel;

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
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_tracks, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        getViewModel().getSyncResult(this, new ListenerModel.Updatable<CharSequence>() {
            @Override
            public void onUpdate(@Nullable CharSequence value) {
                syncResult.setText(value);
            }
        });
        getViewModel().getAsyncStatus(this, new ListenerModel.Updatable<TrackModel.AsyncTrackStatus>() {
            @Override
            public void onUpdate(@Nullable TrackModel.AsyncTrackStatus value) {
                asyncResult.setText(value.result);
                asyncProgressBar.setProgress(value.progress);
            }
        });
    }
}
