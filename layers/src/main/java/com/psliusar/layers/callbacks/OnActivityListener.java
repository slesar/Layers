package com.psliusar.layers.callbacks;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface OnActivityListener {

    void onCreate(@Nullable Bundle state);

    void onRestoreInstanceState(@NonNull Bundle state);

    void onRestart();

    void onStart();

    void onPostCreate(@Nullable Bundle state);

    void onResume();

    void onPostResume();

    void onSaveInstanceState(@NonNull Bundle outState);

    void onPause();

    void onStop();

    void onDestroy();

    void onNewIntent(@NonNull Intent intent);

    void onConfigurationChanged(@Nullable Configuration newConfig);

    void onActivityResult(int requestCode, int resultCode, Intent intent);

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

    void onTrimMemory(int level);

    void onLowMemory();
}
