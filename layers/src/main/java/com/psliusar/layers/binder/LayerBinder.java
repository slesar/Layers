package com.psliusar.layers.binder;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;

import com.psliusar.layers.track.Track;
import com.psliusar.layers.track.TrackWrapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class LayerBinder {

    @NonNull
    protected static View find(@NonNull View container, @IdRes int viewResId) {
        final View view = container.findViewById(viewResId);
        if (view == null) {
            final String viewResName = resolveResourceName(container.getResources(), viewResId);
            throw new IllegalArgumentException("View with ID 0x" + Integer.toHexString(viewResId)
                    + (viewResName != null ? " (" + viewResName + ")" : "") + " not found!");
        }
        return view;
    }

    @Nullable
    private static String resolveResourceName(@NonNull Resources res, int resId) {
        try {
            return res.getResourceName(resId);
        } catch (Resources.NotFoundException ex) {
            return null;
        }
    }

    protected void restore(@NonNull Object object, @NonNull Bundle state) {

    }

    protected void save(@NonNull Object object, @NonNull Bundle state) {

    }

    protected void bind(@NonNull View.OnClickListener listener, @NonNull View view) {
        copyParcelableArray(new Parcelable[0], Bundle[].class);
    }

    protected void unbind(@NonNull View.OnClickListener listener) {

    }

    protected static void initClassLoader(@NonNull Bundle state, @NonNull Object obj) {
        state.setClassLoader(obj.getClass().getClassLoader());
    }

    protected static Parcelable[] copyParcelableArray(@Nullable Parcelable[] array, @NonNull Class<? extends Object[]> targetClass) {
        return array == null ? null : (Parcelable[]) Arrays.copyOf(array, array.length, targetClass);
    }

    protected static Serializable[] copySerializableArray(@Nullable Serializable[] array, @NonNull Class<? extends Object[]> targetClass) {
        return array == null ? null : (Serializable[]) Arrays.copyOf(array, array.length, targetClass);
    }

    /* Put primitives and their boxed versions */

    protected static void putBoolean(@NonNull String key, boolean value, @NonNull Bundle state) {
        state.putBoolean(key, value);
    }

    protected static void putBooleanBoxed(@NonNull String key, @Nullable Boolean value, @NonNull Bundle state) {
        if (value != null) state.putBoolean(key, value);
    }

    protected static void putByte(@NonNull String key, byte value, @NonNull Bundle state) {
        state.putByte(key, value);
    }

    protected static void putByteBoxed(@NonNull String key, @Nullable Byte value, @NonNull Bundle state) {
        if (value != null) state.putByte(key, value);
    }

    protected static void putChar(@NonNull String key, char value, @NonNull Bundle state) {
        state.putChar(key, value);
    }

    protected static void putCharBoxed(@NonNull String key, @Nullable Character value, @NonNull Bundle state) {
        if (value != null) state.putChar(key, value);
    }

    protected static void putShort(@NonNull String key, short value, @NonNull Bundle state) {
        state.putShort(key, value);
    }

    protected static void putShortBoxed(@NonNull String key, @Nullable Short value, @NonNull Bundle state) {
        if (value != null) state.putShort(key, value);
    }

    protected static void putInt(@NonNull String key, int value, @NonNull Bundle state) {
        state.putInt(key, value);
    }

    protected static void putIntBoxed(@NonNull String key, @Nullable Integer value, @NonNull Bundle state) {
        if (value != null) state.putInt(key, value);
    }

    protected static void putLong(@NonNull String key, long value, @NonNull Bundle state) {
        state.putLong(key, value);
    }

    protected static void putLongBoxed(@NonNull String key, @Nullable Long value, @NonNull Bundle state) {
        if (value != null) state.putLong(key, value);
    }

    protected static void putFloat(@NonNull String key, float value, @NonNull Bundle state) {
        state.putFloat(key, value);
    }

    protected static void putFloatBoxed(@NonNull String key, @Nullable Float value, @NonNull Bundle state) {
        if (value != null) state.putFloat(key, value);
    }

    protected static void putDouble(@NonNull String key, double value, @NonNull Bundle state) {
        state.putDouble(key, value);
    }

    protected static void putDoubleBoxed(@NonNull String key, @Nullable Double value, @NonNull Bundle state) {
        if (value != null) state.putDouble(key, value);
    }

    protected static void putString(@NonNull String key, @Nullable String value, @NonNull Bundle state) {
        if (value != null) state.putString(key, value);
    }

    protected static void putCharSequence(@NonNull String key, @Nullable CharSequence value, @NonNull Bundle state) {
        if (value != null) state.putCharSequence(key, value);
    }

    /* Put Parcelable elements */

    protected static void putParcelable(@NonNull String key, @Nullable Parcelable value, @NonNull Bundle state) {
        if (value != null) state.putParcelable(key, value);
    }

    protected static void putParcelableArray(@NonNull String key, @Nullable Parcelable[] value, @NonNull Bundle state) {
        if (value != null) state.putParcelableArray(key, value);
    }

    protected static void putParcelableArrayList(@NonNull String key, @Nullable ArrayList<? extends Parcelable> value, @NonNull Bundle state) {
        if (value != null) state.putParcelableArrayList(key, value);
    }

    protected static void putSparseParcelableArray(@NonNull String key, @Nullable SparseArray<? extends Parcelable> value, @NonNull Bundle state) {
        if (value != null) state.putSparseParcelableArray(key, value);
    }

    /* Put ArrayLists */

    protected static void putIntegerArrayList(@NonNull String key, @Nullable ArrayList<Integer> value, @NonNull Bundle state) {
        if (value != null) state.putIntegerArrayList(key, value);
    }

    protected static void putStringArrayList(@NonNull String key, @Nullable ArrayList<String> value, @NonNull Bundle state) {
        if (value != null) state.putStringArrayList(key, value);
    }

    protected static void putCharSequenceArrayList(@NonNull String key, @Nullable ArrayList<CharSequence> value, @NonNull Bundle state) {
        if (value != null) state.putCharSequenceArrayList(key, value);
    }

    /* Put arrays of primitives */

    protected static void putBooleanArray(@NonNull String key, @Nullable boolean[] value, @NonNull Bundle state) {
        if (value != null) state.putBooleanArray(key, value);
    }

    protected static void putByteArray(@NonNull String key, @Nullable byte[] value, @NonNull Bundle state) {
        if (value != null) state.putByteArray(key, value);
    }

    protected static void putShortArray(@NonNull String key, @Nullable short[] value, @NonNull Bundle state) {
        if (value != null) state.putShortArray(key, value);
    }

    protected static void putCharArray(@NonNull String key, @Nullable char[] value, @NonNull Bundle state) {
        if (value != null) state.putCharArray(key, value);
    }

    protected static void putIntArray(@NonNull String key, @Nullable int[] value, @NonNull Bundle state) {
        if (value != null) state.putIntArray(key, value);
    }

    protected static void putLongArray(@NonNull String key, @Nullable long[] value, @NonNull Bundle state) {
        if (value != null) state.putLongArray(key, value);
    }

    protected static void putFloatArray(@NonNull String key, @Nullable float[] value, @NonNull Bundle state) {
        if (value != null) state.putFloatArray(key, value);
    }

    protected static void putDoubleArray(@NonNull String key, @Nullable double[] value, @NonNull Bundle state) {
        if (value != null) state.putDoubleArray(key, value);
    }

    protected static void putStringArray(@NonNull String key, @Nullable String[] value, @NonNull Bundle state) {
        if (value != null) state.putStringArray(key, value);
    }

    protected static void putCharSequenceArray(@NonNull String key, @Nullable CharSequence[] value, @NonNull Bundle state) {
        if (value != null) state.putCharSequenceArray(key, value);
    }

    /* Put other types */

    protected static void putBundle(@NonNull String key, @Nullable Bundle value, @NonNull Bundle state) {
        if (value != null) state.putBundle(key, value);
    }

    protected static void putSerializable(@NonNull String key, @Nullable Serializable value, @NonNull Bundle state) {
        if (value != null) state.putSerializable(key, value);
    }

    protected static void putSerializableArray(@NonNull String key, @Nullable Serializable[] value, @NonNull Bundle state) {
        if (value != null) state.putSerializable(key, value);
    }

    /* Tracks */

    protected static void putTrack(@NonNull String key, @Nullable Track value, @NonNull Bundle state) {
        if (value == null || value.isDisposed()) {
            return;
        }
        value.unsubscribe();
        final TrackWrapper wrapper = new TrackWrapper(value);
        state.putParcelable(key, wrapper);
    }

    // TODO arrays of boxed primitives

    /* Get primitives and their boxed versions */

    protected static boolean getBoolean(@NonNull String key, @NonNull Bundle state) {
        return state.getBoolean(key);
    }

    @Nullable
    protected static Boolean getBooleanBoxed(@NonNull String key, @NonNull Bundle state) {
        return state.containsKey(key) ? state.getBoolean(key) : null;
    }

    protected static byte getByte(@NonNull String key, @NonNull Bundle state) {
        return state.getByte(key);
    }

    @Nullable
    protected static Byte getByteBoxed(@NonNull String key, @NonNull Bundle state) {
        return state.containsKey(key) ? state.getByte(key) : null;
    }

    protected static char getChar(@NonNull String key, @NonNull Bundle state) {
        return state.getChar(key);
    }

    @Nullable
    protected static Character getCharBoxed(@NonNull String key, @NonNull Bundle state) {
        return state.containsKey(key) ? state.getChar(key) : null;
    }

    protected static short getShort(@NonNull String key, @NonNull Bundle state) {
        return state.getShort(key);
    }

    @Nullable
    protected static Short getShortBoxed(@NonNull String key, @NonNull Bundle state) {
        return state.containsKey(key) ? state.getShort(key) : null;
    }

    protected static int getInt(@NonNull String key, @NonNull Bundle state) {
        return state.getInt(key);
    }

    @Nullable
    protected static Integer getIntBoxed(@NonNull String key, @NonNull Bundle state) {
        return state.containsKey(key) ? state.getInt(key) : null;
    }

    protected static long getLong(@NonNull String key, @NonNull Bundle state) {
        return state.getLong(key);
    }

    @Nullable
    protected static Long getLongBoxed(@NonNull String key, @NonNull Bundle state) {
        return state.containsKey(key) ? state.getLong(key) : null;
    }

    protected static float getFloat(@NonNull String key, @NonNull Bundle state) {
        return state.getFloat(key);
    }

    @Nullable
    protected static Float getFloatBoxed(@NonNull String key, @NonNull Bundle state) {
        return state.containsKey(key) ? state.getFloat(key) : null;
    }

    protected static double getDouble(@NonNull String key, @NonNull Bundle state) {
        return state.getDouble(key);
    }

    @Nullable
    protected static Double getDoubleBoxed(@NonNull String key, @NonNull Bundle state) {
        return state.containsKey(key) ? state.getDouble(key) : null;
    }

    @Nullable
    protected static String getString(@NonNull String key, @NonNull Bundle state) {
        return state.getString(key);
    }

    @Nullable
    protected static CharSequence getCharSequence(@NonNull String key, @NonNull Bundle state) {
        return state.getCharSequence(key);
    }

    /* Get Parcelable elements */

    @Nullable
    protected static Parcelable getParcelable(@NonNull String key, @NonNull Bundle state) {
        return state.getParcelable(key);
    }

    @Nullable
    protected static Parcelable[] getParcelableArray(@NonNull String key, @NonNull Bundle state) {
        return state.getParcelableArray(key);
    }

    @Nullable
    protected static ArrayList<Parcelable> getParcelableArrayList(@NonNull String key, @NonNull Bundle state) {
        return state.getParcelableArrayList(key);
    }

    @Nullable
    protected static SparseArray<Parcelable> getSparseParcelableArray(@NonNull String key, @NonNull Bundle state) {
        return state.getSparseParcelableArray(key);
    }

    /* Get ArrayLists */

    @Nullable
    protected static ArrayList<Integer> getIntegerArrayList(@NonNull String key, @NonNull Bundle state) {
        return state.getIntegerArrayList(key);
    }

    @Nullable
    protected static ArrayList<String> getStringArrayList(@NonNull String key, @NonNull Bundle state) {
        return state.getStringArrayList(key);
    }

    @Nullable
    protected static ArrayList<CharSequence> getCharSequenceArrayList(@NonNull String key, @NonNull Bundle state) {
        return state.getCharSequenceArrayList(key);
    }

    /* Get arrays of primitives */

    @Nullable
    protected static boolean[] getBooleanArray(@NonNull String key, @NonNull Bundle state) {
        return state.getBooleanArray(key);
    }

    @Nullable
    protected static byte[] getByteArray(@NonNull String key, @NonNull Bundle state) {
        return state.getByteArray(key);
    }

    @Nullable
    protected static short[] getShortArray(@NonNull String key, @NonNull Bundle state) {
        return state.getShortArray(key);
    }

    @Nullable
    protected static char[] getCharArray(@NonNull String key, @NonNull Bundle state) {
        return state.getCharArray(key);
    }

    @Nullable
    protected static int[] getIntArray(@NonNull String key, @NonNull Bundle state) {
        return state.getIntArray(key);
    }

    @Nullable
    protected static long[] getLongArray(@NonNull String key, @NonNull Bundle state) {
        return state.getLongArray(key);
    }

    @Nullable
    protected static float[] getFloatArray(@NonNull String key, @NonNull Bundle state) {
        return state.getFloatArray(key);
    }

    @Nullable
    protected static double[] getDoubleArray(@NonNull String key, @NonNull Bundle state) {
        return state.getDoubleArray(key);
    }

    @Nullable
    protected static String[] getStringArray(@NonNull String key, @NonNull Bundle state) {
        return state.getStringArray(key);
    }

    @Nullable
    protected static CharSequence[] getCharSequenceArray(@NonNull String key, @NonNull Bundle state) {
        return state.getCharSequenceArray(key);
    }

    /* Get other types */

    @Nullable
    protected static Bundle getBundle(@NonNull String key, @NonNull Bundle state) {
        return state.getBundle(key);
    }

    @Nullable
    protected static Serializable getSerializable(@NonNull String key, @NonNull Bundle state) {
        return state.getSerializable(key);
    }

    @Nullable
    protected static Serializable[] getSerializableArray(@NonNull String key, @NonNull Bundle state) {
        return (Serializable[]) state.getSerializable(key);
    }

    /* Tracks */

    @Nullable
    protected Track getTrack(@NonNull String key, @NonNull Bundle state) {
        final TrackWrapper wrapper = state.getParcelable(key);
        return wrapper == null ? null : wrapper.getTrack();
    }

    // TODO arrays of boxed primitives
    //state.get(key);
}
