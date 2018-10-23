package com.psliusar.layers.track;

import androidx.annotation.NonNull;

public class SimpleSyncTrack extends Track<String, Integer> {

    private final String[] array;

    public SimpleSyncTrack(@NonNull String[] array) {
        this.array = array;
    }

    @Override
    protected void doBlocking() {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(array[i]);
        }
        done(builder.toString());
    }
}
