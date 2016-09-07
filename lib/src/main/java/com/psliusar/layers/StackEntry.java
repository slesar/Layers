package com.psliusar.layers;

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

    private static final String STATE_LAYER = "STATE_LAYER";
    private static final String STATE_VIEW = "STATE_VIEW";

    int state = LAYER_STATE_EMPTY;

    final String className;
    final String name;
    Bundle arguments;
    Bundle savedState;
    int type;

    Class<? extends Layer<?>> layerClass;
    Layer<?> layerInstance;

    StackEntry(@NonNull Class<? extends Layer<?>> layerClass, @Nullable Bundle arguments, @Nullable String name, int type) {
        this.layerClass = layerClass;
        this.className = layerClass.getName();
        this.arguments = arguments;
        this.name = name;
        this.type = type;
    }

    Class<? extends Layer<?>> getLayerClass() {
        try {
            if (layerClass == null) {
                //noinspection unchecked
                layerClass = (Class<? extends Layer<?>>) this.getClass().getClassLoader().loadClass(className);
            }
            return layerClass;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load class " + className);
        }
    }

    @Nullable
    Bundle pickLayerSavedState() {
        if (savedState == null) {
            return null;
        }
        final Bundle bundle = savedState.getBundle(STATE_LAYER);
        savedState.remove(STATE_LAYER);
        if (savedState.size() == 0) {
            savedState = null;
        }
        return bundle;
    }

    void setLayerSavedState(Bundle state) {
        if (savedState == null) {
            savedState = new Bundle();
        }
        savedState.putBundle(STATE_LAYER, state);
    }

    SparseArray<Parcelable> pickViewSavedState() {
        if (savedState == null) {
            return null;
        }
        final SparseArray<Parcelable> array = savedState.getSparseParcelableArray(STATE_VIEW);
        savedState.remove(STATE_VIEW);
        if (savedState.size() == 0) {
            savedState = null;
        }
        return array;
    }

    void setViewSavedState(SparseArray<Parcelable> viewState) {
        if (savedState == null) {
            savedState = new Bundle();
        }
        savedState.putSparseParcelableArray(STATE_VIEW, viewState);
    }

    private StackEntry(Parcel in) {
        final ClassLoader classLoader = getClass().getClassLoader();
        className = in.readString();
        name = in.readString();
        arguments = in.readBundle(classLoader);
        savedState = in.readBundle(classLoader);
        type = in.readInt();
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
        dest.writeBundle(savedState);
        dest.writeInt(type);
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
