package com.psliusar.layers;

import android.content.Context;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class WrapperLayout extends FrameLayout {

    static WrapperLayout addTo(ViewGroup container) {
        final WrapperLayout wrapper = new WrapperLayout(container.getContext());

        final ViewGroup.LayoutParams lp = container.getLayoutParams();
        final LayoutParams newParams = new LayoutParams(lp.width, lp.height);
        wrapper.setLayoutParams(newParams);
        return wrapper;
    }

    public WrapperLayout(Context context) {
        super(context);
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }
}
