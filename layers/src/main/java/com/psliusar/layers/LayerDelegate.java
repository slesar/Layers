package com.psliusar.layers;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface LayerDelegate {

    void onCreate(@Nullable Bundle savedState);

    void onAttach();

    void onBindView(@Nullable Bundle savedState, @NonNull View view);

    void restoreViewState(@Nullable SparseArray<Parcelable> inState);

    void saveViewState(@NonNull SparseArray<Parcelable> outState);

    void onDetach();

    void onDestroyView();

    void onDismiss();

    void saveLayerState(@NonNull Bundle outState);

    boolean isViewInLayout();

    @Nullable
    LayoutInflater getLayoutInflater();
}
