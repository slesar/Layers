package com.psliusar.layers.callbacks;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class BaseActivityListener implements OnActivityListener {

    @Override
    public void onCreate(@Nullable Bundle state) {
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle state) {
    }

    @Override
    public void onRestart() {
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onPostCreate(@Nullable Bundle state) {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPostResume() {
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onNewIntent(@NonNull Intent intent) {
    }

    @Override
    public void onConfigurationChanged(@Nullable Configuration newConfig) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    }

    @Override
    public void onTrimMemory(int level) {
    }

    @Override
    public void onLowMemory() {
    }
}
