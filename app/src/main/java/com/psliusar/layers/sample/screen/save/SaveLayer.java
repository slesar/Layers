package com.psliusar.layers.sample.screen.save;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.psliusar.layers.Layer;
import com.psliusar.layers.binder.Bind;
import com.psliusar.layers.binder.FieldStateManager;
import com.psliusar.layers.binder.Save;
import com.psliusar.layers.sample.R;

import java.util.ArrayList;
import java.util.Collections;

public class SaveLayer extends Layer<SavePresenter> {

    @Bind(R.id.save_string_list)
    protected TextView stringListView;

    @Save
    protected ArrayList<String> stringList;

    @Save
    protected SparseArray<Parcelable> parcelables;

    @Save(stateManager = CustomFieldStateManager.class)
    protected String customManagerSample;

    @Override
    protected SavePresenter onCreatePresenter() {
        return new SavePresenter();
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable ViewGroup parent) {
        return inflate(R.layout.screen_save, parent);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        final StringBuilder joinStrings = new StringBuilder();
        for (String s : stringList) {
            if (joinStrings.length() > 0) {
                joinStrings.append(", ");
            }
            joinStrings.append(s);
        }
        stringListView.setText(joinStrings.toString());
    }

    public void setParameters(String... items) {
        stringList = new ArrayList<>();
        Collections.addAll(stringList, items);
    }

    protected static class CustomFieldStateManager implements FieldStateManager<String> {

        @Override
        public void put(@NonNull String key, String value, @NonNull Bundle state) {
            state.putString(key, value);
        }

        @Override
        public String get(@NonNull String key, @NonNull Bundle state) {
            return state.getString(key);
        }
    }
}
