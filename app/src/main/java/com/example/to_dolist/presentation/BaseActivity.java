package com.example.to_dolist.presentation;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.example.to_dolist.core.util.AppPreferences;

/**
 * Applies persisted font scaling without restarting the process.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppPreferences.wrapFontScale(newBase));
    }
}
