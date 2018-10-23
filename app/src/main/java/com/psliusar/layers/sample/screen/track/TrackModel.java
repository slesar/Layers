package com.psliusar.layers.sample.screen.track;

import com.psliusar.layers.Model;

import androidx.annotation.Nullable;

public class TrackModel implements Model {

    public static class AsyncTrackStatus {

        public final CharSequence result;

        public final int progress;

        public AsyncTrackStatus(@Nullable CharSequence result, int progress) {
            this.result = result;
            this.progress = progress;
        }

        public AsyncTrackStatus(@Nullable Integer result, int progress) {
            this(result == null ? null : result.toString(), progress);
        }
    }
}
