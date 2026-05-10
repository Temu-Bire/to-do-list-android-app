package com.example.to_dolist;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.to_dolist.core.util.AppPreferences;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Application-level DI container.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation.
 */
@HiltAndroidApp
public class ToDoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppPreferences.applyStoredLocale(this);
        applyUserTheme();
    }

    private void applyUserTheme() {
        boolean isDarkMode = AppPreferences.prefs(this).getBoolean(AppPreferences.KEY_DARK_MODE, false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
