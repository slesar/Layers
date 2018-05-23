package com.psliusar.layers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

public class MockedViewGroup extends ViewGroup {

    private int childCount = 0;

    public MockedViewGroup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) { }

    @Override
    public void setSaveFromParentEnabled(boolean enabled) { }

    @Override
    public int getChildCount() {
        return childCount;
    }

    @Override
    public void addView(@NonNull View child, int index) {
        childCount++;
    }

    @Override
    public void removeView(@NonNull View view) {
        childCount--;
    }
}
