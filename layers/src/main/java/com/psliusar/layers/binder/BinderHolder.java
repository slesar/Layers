package com.psliusar.layers.binder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface BinderHolder {

    @Nullable
    ObjectBinder getObjectBinder();

    void setObjectBinder(@NonNull ObjectBinder objectBinder);
}
