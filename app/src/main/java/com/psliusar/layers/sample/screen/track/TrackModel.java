package com.psliusar.layers.sample.screen.track;

import android.support.annotation.NonNull;

import com.psliusar.layers.Model;

public class TrackModel implements Model {

    public static class AsyncTrackStatus {

        public final CharSequence result;

        public final int progress;

        public AsyncTrackStatus(@NonNull CharSequence result, int progress) {
            this.result = result;
            this.progress = progress;
        }
    }
}
