package com.psliusar.layers.binder;

import android.os.Bundle;
import android.support.annotation.NonNull;

public interface FieldStateManager<T> {

    void put(@NonNull String key, T value, @NonNull Bundle state);

    T get(@NonNull String key, @NonNull Bundle state);
}
