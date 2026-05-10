package com.example.to_dolist.presentation.unlock;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.to_dolist.R;
import com.example.to_dolist.core.util.AppPreferences;
import com.example.to_dolist.core.util.AppSession;
import com.example.to_dolist.core.util.SecureAppSettings;
import com.example.to_dolist.databinding.ActivityUnlockBinding;
import com.example.to_dolist.presentation.BaseActivity;
import com.example.to_dolist.presentation.home.HomeActivity;

import java.util.concurrent.Executor;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UnlockActivity extends BaseActivity {

    private ActivityUnlockBinding binding;
    private Executor executor;
    private BiometricPrompt biometricPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUnlockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        executor = ContextCompat.getMainExecutor(this);

        binding.buttonUnlock.setOnClickListener(v -> tryPin());

        if (AppPreferences.isBiometricEnabled(this)) {
            binding.buttonBiometric.setVisibility(View.VISIBLE);
            binding.buttonBiometric.setOnClickListener(v -> showBiometric());
            showBiometric();
        } else {
            binding.buttonBiometric.setVisibility(View.GONE);
        }
    }

    private void tryPin() {
        String pin = binding.editPin.getText() != null ? binding.editPin.getText().toString() : "";
        String expected = SecureAppSettings.prefs(this).getString(SecureAppSettings.KEY_PIN, "");
        if (TextUtils.isEmpty(expected)) {
            Toast.makeText(this, R.string.unlock_no_pin_set, Toast.LENGTH_LONG).show();
            return;
        }
        if (expected.equals(pin)) {
            enterApp();
        } else {
            Toast.makeText(this, R.string.unlock_wrong_pin, Toast.LENGTH_SHORT).show();
        }
    }

    private void showBiometric() {
        biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        enterApp();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence err) {
                        // fall back to PIN
                    }
                });
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.unlock_title))
                .setSubtitle(getString(R.string.unlock_subtitle))
                .setNegativeButtonText(getString(R.string.cancel))
                .build();
        biometricPrompt.authenticate(info);
    }

    private void enterApp() {
        AppSession.setUnlocked(true);
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
