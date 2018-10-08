package com.psliusar.layers;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;

public interface LayerDelegate {

    void onCreate(@Nullable Bundle savedState);

    void onAttach();

    void restoreViewState(@Nullable SparseArray<Parcelable> inState);

    void saveViewState(@NonNull SparseArray<Parcelable> outState);

    void onDetach();

    void onDestroyView();

    void saveLayerState(@NonNull Bundle outState);

    boolean isViewInLayout();

    @Nullable
    LayoutInflater getLayoutInflater();
}
