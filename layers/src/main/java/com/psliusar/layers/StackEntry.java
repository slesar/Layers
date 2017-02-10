package com.psliusar.layers;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

class StackEntry implements Parcelable {

    static final int LAYER_STATE_EMPTY = 0;
    static final int LAYER_STATE_CREATED = 1;
    static final int LAYER_STATE_VIEW_CREATED = 2;
    static final int LAYER_STATE_VIEW_DESTROYED = 3;
    static final int LAYER_STATE_DESTROYED = 4;

    static final int TYPE_TRANSPARENT = 0;
    static final int TYPE_OPAQUE = 1;

    private static final String VIEW_STATE = "STACK_ENTRY.VIEW_STATE";

    final String className;
    String name;
    Bundle arguments;
    Bundle layerState;
    SparseArray<Parcelable> viewState;
    int layerType = TYPE_OPAQUE;
    int layerTypeAnimated;
    boolean valid = true;

    @Nullable
    int[] animations;

    Class<? extends Layer<?>> layerClass;
    Layer<?> layerInstance;
    int state = LAYER_STATE_EMPTY;

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

    @NonNull
    String getLayerClassName() {
        return className;
    }

    @Nullable
    Bundle pickLayerSavedState() {
        final Bundle bundle = layerState;
        layerState = null;
        return bundle;
    }

    void setLayerSavedState(@Nullable Bundle state) {
        layerState = state;
    }

    @Nullable
    SparseArray<Parcelable> pickViewSavedState() {
        final SparseArray<Parcelable> array = viewState;
        viewState = null;
        return array;
    }

    void setViewSavedState(@Nullable SparseArray<Parcelable> state) {
        viewState = state;
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
