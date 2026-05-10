package com.example.to_dolist.presentation.settings;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.to_dolist.core.util.AppPreferences;
import com.example.to_dolist.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        boolean isDarkMode = AppPreferences.prefs(this).getBoolean(AppPreferences.KEY_DARK_MODE, false);
        binding.switchDarkMode.setChecked(isDarkMode);
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppPreferences.prefs(this).edit().putBoolean(AppPreferences.KEY_DARK_MODE, isChecked).apply();
            applyTheme(isChecked);
        });

        setupLanguageRadios();
    }

    private void setupLanguageRadios() {
        String stored = AppPreferences.getLanguageCode(this);
        binding.radioGroupLanguage.setOnCheckedChangeListener(null);
        if (AppPreferences.LANG_AM.equals(stored)) {
            binding.radioAmharic.setChecked(true);
        } else {
            binding.radioEnglish.setChecked(true);
        }
        binding.radioGroupLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String code = checkedId == binding.radioAmharic.getId()
                    ? AppPreferences.LANG_AM
                    : AppPreferences.LANG_EN;
            String current = AppPreferences.getLanguageCode(this);
            if (code.equals(current)) return;
            AppPreferences.prefs(this).edit().putString(AppPreferences.KEY_LANGUAGE, code).apply();
            AppPreferences.applyStoredLocale(this);
        });
    }

    private void applyTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
