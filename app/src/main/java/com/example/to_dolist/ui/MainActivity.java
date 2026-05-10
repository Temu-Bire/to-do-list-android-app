package com.example.to_dolist.ui;

import android.content.Intent;
import android.os.Bundle;

import com.example.to_dolist.core.util.AppPreferences;
import com.example.to_dolist.core.util.AppSession;
import com.example.to_dolist.presentation.BaseActivity;
import com.example.to_dolist.presentation.home.HomeActivity;
import com.example.to_dolist.presentation.unlock.UnlockActivity;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Entry point: routes to unlock flow or home. Replaces the deprecated empty {@link MainActivity} stub.
 */
@AndroidEntryPoint
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AppPreferences.isAppLockEnabled(this)) {
            goHome();
            return;
        }
        if (AppSession.isUnlocked()) {
            goHome();
            return;
        }
        startActivity(new Intent(this, UnlockActivity.class));
        finish();
    }

    private void goHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
