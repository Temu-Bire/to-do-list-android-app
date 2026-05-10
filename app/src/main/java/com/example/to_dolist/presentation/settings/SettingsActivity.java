package com.example.to_dolist.presentation.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.to_dolist.R;
import com.example.to_dolist.core.util.AppPreferences;
import com.example.to_dolist.core.util.AppSession;
import com.example.to_dolist.core.util.BackupHelper;
import com.example.to_dolist.core.util.DataExportHelper;
import com.example.to_dolist.core.util.NotificationHelper;
import com.example.to_dolist.core.util.SecureAppSettings;
import com.example.to_dolist.databinding.ActivitySettingsBinding;
import com.example.to_dolist.domain.model.Category;
import com.example.to_dolist.domain.usecase.InsertCategoryUseCase;
import com.example.to_dolist.presentation.BaseActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding binding;

    @Inject
    InsertCategoryUseCase insertCategoryUseCase;

    private final ActivityResultLauncher<String> createCsvLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("text/csv"),
                    this::onCsvCreated);

    private final ActivityResultLauncher<String> createDbLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/x-sqlite3"),
                    this::onDbBackupCreated);

    private final ActivityResultLauncher<String[]> openDbLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    this::onDbPickedForRestore);

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
        binding.switchDarkMode.setOnCheckedChangeListener(null);
        binding.switchDarkMode.setChecked(isDarkMode);
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppPreferences.prefs(this).edit().putBoolean(AppPreferences.KEY_DARK_MODE, isChecked).apply();
            applyTheme(isChecked);
        });

        setupLanguageRadios();
        setupFontRadios();

        binding.buttonNotificationSettings.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, NotificationHelper.CHANNEL_ID);
            try {
                startActivity(intent);
            } catch (Exception e) {
                startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()));
            }
        });

        binding.switchAppLock.setOnCheckedChangeListener(null);
        binding.switchAppLock.setChecked(AppPreferences.isAppLockEnabled(this));
        binding.switchAppLock.setOnCheckedChangeListener((v, checked) -> {
            if (checked) {
                promptSetPin();
            } else {
                AppPreferences.prefs(this).edit().putBoolean(AppPreferences.KEY_APP_LOCK, false).apply();
                SecureAppSettings.prefs(this).edit().remove(SecureAppSettings.KEY_PIN).apply();
                AppSession.clearUnlock();
            }
        });

        binding.switchBiometric.setOnCheckedChangeListener(null);
        binding.switchBiometric.setChecked(AppPreferences.isBiometricEnabled(this));
        binding.switchBiometric.setOnCheckedChangeListener((v, checked) ->
                AppPreferences.prefs(this).edit().putBoolean(AppPreferences.KEY_BIOMETRIC, checked).apply());

        binding.switchCloudSync.setChecked(
                AppPreferences.prefs(this).getBoolean(AppPreferences.KEY_CLOUD_SYNC, false));

        binding.buttonExportCsv.setOnClickListener(v ->
                createCsvLauncher.launch("tasks_export.csv"));

        binding.buttonBackupDb.setOnClickListener(v ->
                createDbLauncher.launch("todo_backup.db"));

        binding.buttonRestoreDb.setOnClickListener(v ->
                openDbLauncher.launch(new String[]{"application/octet-stream", "application/x-sqlite3", "*/*"}));

        binding.buttonAddCategory.setOnClickListener(v -> {
            String name = binding.editNewCategory.getText() != null
                    ? binding.editNewCategory.getText().toString().trim() : "";
            if (name.isEmpty()) return;
            try {
                insertCategoryUseCase.execute(new Category(0, name, "#5C6BC0"));
                binding.editNewCategory.setText("");
                Toast.makeText(this, R.string.category_added, Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void promptSetPin() {
        final TextInputEditText input = new TextInputEditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.set_pin)
                .setView(input)
                .setPositiveButton(R.string.save_task, (d, w) -> {
                    String pin = input.getText() != null ? input.getText().toString() : "";
                    if (TextUtils.isEmpty(pin) || pin.length() < 4) {
                        binding.switchAppLock.setChecked(false);
                        Toast.makeText(this, R.string.title_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SecureAppSettings.prefs(this).edit().putString(SecureAppSettings.KEY_PIN, pin).apply();
                    AppPreferences.prefs(this).edit().putBoolean(AppPreferences.KEY_APP_LOCK, true).apply();
                    AppSession.clearUnlock();
                })
                .setNegativeButton(R.string.cancel, (d, w) -> binding.switchAppLock.setChecked(false))
                .show();
    }

    private void onCsvCreated(Uri uri) {
        if (uri == null) return;
        DataExportHelper.exportTasksCsv(this, uri, ok ->
                Toast.makeText(this, ok ? R.string.export_done : R.string.export_failed, Toast.LENGTH_SHORT).show());
    }

    private void onDbBackupCreated(Uri uri) {
        if (uri == null) return;
        BackupHelper.exportDatabase(this, uri, ok ->
                Toast.makeText(this, ok ? R.string.backup_done : R.string.backup_failed, Toast.LENGTH_SHORT).show());
    }

    private void onDbPickedForRestore(Uri uri) {
        if (uri == null) return;
        BackupHelper.importDatabase(this, uri, ok ->
                Toast.makeText(this, ok ? R.string.restore_done : R.string.restore_failed, Toast.LENGTH_LONG).show());
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

    private void setupFontRadios() {
        String font = AppPreferences.prefs(this).getString(AppPreferences.KEY_FONT_SIZE, AppPreferences.FONT_DEFAULT);
        binding.radioGroupFont.setOnCheckedChangeListener(null);
        if (AppPreferences.FONT_SMALL.equals(font)) {
            binding.radioFontSmall.setChecked(true);
        } else if (AppPreferences.FONT_LARGE.equals(font)) {
            binding.radioFontLarge.setChecked(true);
        } else {
            binding.radioFontDefault.setChecked(true);
        }
        binding.radioGroupFont.setOnCheckedChangeListener((g, id) -> {
            String v = AppPreferences.FONT_DEFAULT;
            if (id == binding.radioFontSmall.getId()) v = AppPreferences.FONT_SMALL;
            else if (id == binding.radioFontLarge.getId()) v = AppPreferences.FONT_LARGE;
            AppPreferences.prefs(this).edit().putString(AppPreferences.KEY_FONT_SIZE, v).apply();
            recreate();
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
