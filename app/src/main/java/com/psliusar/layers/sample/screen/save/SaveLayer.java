package com.psliusar.layers.sample.screen.save;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SaveLayer extends Layer<SaveViewModel> {

    @Bind(R.id.save_string_list) TextView stringListView;

    @Save ArrayList<String> stringList;
    @Save SparseArray<Parcelable> parcelables;
    @Save Rect[] parcelablesArray;
    @Save(stateManager = CustomFieldStateManager.class) String customManagerSample;

    @Nullable
    @Override
    protected SaveViewModel onCreateViewModel() {
        return new SaveViewModel();
    }

    @Nullable
    @Override
    protected View onCreateView(@Nullable Bundle savedState, @Nullable ViewGroup parent) {
        return inflate(R.layout.screen_save, parent);
    }

    @Override
    protected void onBindView(@Nullable Bundle savedState, @NonNull View view) {
        super.onBindView(savedState, view);

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
