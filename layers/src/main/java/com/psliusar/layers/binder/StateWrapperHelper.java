package com.psliusar.layers.binder;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StateWrapperHelper {

    private StateWrapperHelper() {
        // no instances
    }

    @Nullable
    public static <T> T getValue(@NonNull Bundle bundle, @NonNull Class<T> type, @NonNull String key) {
        //return state.getParcelableArray(key);
        //return state.getParcelableArrayList(key);
        //return state.getSparseParcelableArray(key);
        //return state.getIntegerArrayList(key);
        //return state.getStringArrayList(key);
        //return state.getCharSequenceArrayList(key);
        //return state.getBooleanArray(key);
        //return state.getByteArray(key);
        //return state.getShortArray(key);
        //return state.getCharArray(key);
        //return state.getIntArray(key);
        //return state.getLongArray(key);
        //return state.getFloatArray(key);
        //return state.getDoubleArray(key);
        //return state.getStringArray(key);
        //return state.getCharSequenceArray(key);
        //return state.getBundle(key);
        //return state.getSerializable(key);
        //return (Serializable[]) state.getSerializable(key);
        throw new UnsupportedOperationException("Unable to retrieve value of type " + type);
    }

    public static <T> void saveValue(@NonNull Bundle bundle, @NonNull Class<T> type, @NonNull String key, @Nullable T value) {
        //if (value != null) state.putParcelableArray(key, value);
        //if (value != null) state.putParcelableArrayList(key, value);
        //if (value != null) state.putSparseParcelableArray(key, value);
        //if (value != null) state.putIntegerArrayList(key, value);
        //if (value != null) state.putStringArrayList(key, value);
        //if (value != null) state.putCharSequenceArrayList(key, value);
        //if (value != null) state.putBooleanArray(key, value);
        //if (value != null) state.putByteArray(key, value);
        //if (value != null) state.putShortArray(key, value);
        //if (value != null) state.putCharArray(key, value);
        //if (value != null) state.putIntArray(key, value);
        //if (value != null) state.putLongArray(key, value);
        //if (value != null) state.putFloatArray(key, value);
        //if (value != null) state.putDoubleArray(key, value);
        //if (value != null) state.putStringArray(key, value);
        //if (value != null) state.putCharSequenceArray(key, value);
        //if (value != null) state.putBundle(key, value);
        //if (value != null) state.putSerializable(key, value);
        //if (value != null) state.putSerializable(key, value); // array
        throw new UnsupportedOperationException("Unable to save value of type " + type);
    }
}
