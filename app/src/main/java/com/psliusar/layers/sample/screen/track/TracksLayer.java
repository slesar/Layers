package com.psliusar.layers.sample.screen.track;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.psliusar.layers.Layer;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.binder.Save;
import com.psliusar.layers.sample.R;
import com.psliusar.layers.track.TrackManager;

public class TracksLayer extends Layer<TracksPresenter> {

    @Bind(R.id.track_sync_result) TextView syncResult;
    @Bind(R.id.track_async_progress) ProgressBar asyncProgressBar;
    @Bind(R.id.track_async_result) TextView asyncResult;

    @Save TrackManager trackManager;

    @Override
    protected TracksPresenter onCreatePresenter() {
        return new TracksPresenter(this);
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_tracks, parent);
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
}
