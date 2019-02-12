package com.psliusar.layers;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class StackEntry implements Parcelable {

    static final int LAYER_STATE_EMPTY = 0;
    static final int LAYER_STATE_CREATED = 1;
    static final int LAYER_STATE_VIEW_CREATED = 2;
    static final int LAYER_STATE_VIEW_DESTROYED = 3;
    static final int LAYER_STATE_DESTROYED = 4;

    static final int TYPE_TRANSPARENT = 0;
    static final int TYPE_OPAQUE = 1;

    private static final String VIEW_STATE = "STACK_ENTRY.VIEW_STATE";

    //region Retained properties

    private final String className;
    @Nullable
    String name;
    @Nullable
    Bundle arguments;
    @Nullable
    Bundle layerState;
    @Nullable
    SparseArray<Parcelable> viewState;
    int layerType = TYPE_OPAQUE;
    @Nullable
    int[] animations;

    //endregion

    private Class<? extends Layer<?>> layerClass;
    Layer<?> layerInstance;
    int state = LAYER_STATE_EMPTY;
    boolean valid = true;
    boolean inTransition = false;

    StackEntry(@NonNull Class<? extends Layer<?>> layerClass) {
        this.layerClass = layerClass;
        this.className = layerClass.getName();
    }

    @NonNull
    private Class<? extends Layer<?>> getLayerClass(@NonNull Context context) {
        if (layerClass == null) {
            try {
                //noinspection unchecked
                layerClass = (Class<? extends Layer<?>>) context.getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to load class " + className);
            }
        }
        return layerClass;
    }

    @NonNull
    Layer<?> instantiateLayer(@NonNull Context context) {
        if (layerInstance != null) {
            return layerInstance;
        }
        try {
            layerInstance = getLayerClass(context).newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Unable to instantiate layer " + className
                    + ": make sure class exists, is public, and has an empty constructor", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to instantiate layer " + className
                    + ": make sure class has an empty constructor that is public", e);
        }
        return layerInstance;
    }

    StackEntry(Parcel in) {
        final ClassLoader classLoader = Layers.class.getClassLoader();
        className = in.readString();
        name = in.readString();
        arguments = in.readBundle(classLoader);
        layerState = in.readBundle(classLoader);
        final Bundle viewBundle = in.readBundle(classLoader);
        if (viewBundle != null) {
            viewState = viewBundle.getSparseParcelableArray(VIEW_STATE);
        }
        layerType = in.readInt();
        if (in.readInt() > 0) {
            animations = new int[4];
            in.readIntArray(animations);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(className);
        dest.writeString(name);
        dest.writeBundle(arguments);
        dest.writeBundle(layerState);
        final Bundle viewBundle = new Bundle();
        viewBundle.putSparseParcelableArray(VIEW_STATE, viewState);
        dest.writeBundle(viewBundle);
        dest.writeInt(layerType);
        if (animations == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            dest.writeIntArray(animations);
        }
    }

    public static final Creator<StackEntry> CREATOR = new Creator<StackEntry>() {

        @Override
        public StackEntry createFromParcel(Parcel in) {
            return new StackEntry(in);
        }

        @Override
        public StackEntry[] newArray(int size) {
            return new StackEntry[size];
        }
    };
}
