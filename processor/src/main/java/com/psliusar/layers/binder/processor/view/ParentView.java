package com.psliusar.layers.binder.processor.view;

import android.support.annotation.NonNull;

public class ParentView {

    private final Integer resId;
    private final String varName;

    public ParentView(@NonNull Integer resId, @NonNull String varName) {
        this.resId = resId;
        this.varName = varName;
    }

    @NonNull
    public Integer getResId() {
        return resId;
    }

    @NonNull
    public String getVarName() {
        return varName;
    }
}
