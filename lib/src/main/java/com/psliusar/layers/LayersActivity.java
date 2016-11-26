package com.psliusar.layers;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

public abstract class LayersActivity extends AppCompatActivity implements LayersHost {

    private static final String SAVED_STATE_LAYERS = "LAYERS.SAVED_STATE_LAYERS";

    private ActivityCallbacks activityCallbacks;
    private Layers layers;
    private boolean layersStateRestored = false;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        activityCallbacks = new ActivityCallbacks();
        layersStateRestored = state != null;
        layers = new Layers(this, state != null ? state.getBundle(SAVED_STATE_LAYERS) : null);
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_CREATE, state);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if (!isFinishing()) {
            layers.restoreState();
        }
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_RESTORE_STATE, state);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_RESTART);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ensureLayerViews();
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_START);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle state) {
        super.onPostCreate(state);
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_POST_CREATE, state);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_RESUME);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_POST_RESUME);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!isFinishing()) {
            final Bundle layersState = layers.saveState();
            if (layersState != null) {
                outState.putBundle(SAVED_STATE_LAYERS, layersState);
            }
        }
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_SAVE_STATE, outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_PAUSE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_STOP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        layers.destroy();
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_DESTROY);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_NEW_INTENT, intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_CONFIGURATION_CHANGED, newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_ACTIVITY_RESULT, requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_REQUEST_PERMISSIONS_RESULT, requestCode, permissions, grantResults);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_TRIM_MEMORY, level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        activityCallbacks.fireEvent(ActivityCallbacks.EVENT_ON_LOW_MEMORY);
    }

    @Override
    public void onBackPressed() {
        if (layers.getStackSize() > 1) {
            final Layer<?> topLayer = layers.peek();
            if (topLayer != null && !topLayer.onBackPressed()) {
                layers.pop();
                return;
            }
        }
        super.onBackPressed();
    }

    @NonNull
    @Override
    public Layers getLayers() {
        ensureLayerViews();
        return layers;
    }

    @NonNull
    @Override
    public <T extends View> T getView(@IdRes int viewId) {
        final View view = findViewById(viewId);
        if (view == null) {
            throw new IllegalArgumentException("View not found");
        }
        //noinspection unchecked
        return (T) view;
    }

    @NonNull
    @Override
    public ViewGroup getDefaultContainer() {
        return getView(android.R.id.content);
    }

    @NonNull
    @Override
    public Activity getActivity() {
        return this;
    }

    @Nullable
    @Override
    public Layer<?> getParentLayer() {
        return null;
    }

    public ActivityCallbacks getActivityCallbacks() {
        return activityCallbacks;
    }

    private void ensureLayerViews() {
        if (layersStateRestored) {
            layers.resumeView();
            layersStateRestored = false;
        }
    }
}
