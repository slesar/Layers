package com.psliusar.layers.binder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface BinderHolder {

    @Nullable
    ObjectBinder getObjectBinder();

    void setObjectBinder(@NonNull ObjectBinder objectBinder);
}
