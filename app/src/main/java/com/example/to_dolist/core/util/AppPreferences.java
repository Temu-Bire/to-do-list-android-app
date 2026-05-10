package com.example.to_dolist.core.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

/**
 * Central keys for {@link SharedPreferences} and helpers for persisted locale/theme.
 */
public final class AppPreferences {

    public static final String PREFS_NAME = "todo_settings";
    public static final String KEY_DARK_MODE = "dark_mode";
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_APP_LOCK = "app_lock_enabled";
    public static final String KEY_BIOMETRIC = "biometric_unlock";
    public static final String KEY_FONT_SIZE = "font_size";
    public static final String KEY_CLOUD_SYNC = "cloud_sync_enabled";

    public static final String FONT_DEFAULT = "default";
    public static final String FONT_SMALL = "small";
    public static final String FONT_LARGE = "large";

    public static final String LANG_EN = "en";
    public static final String LANG_AM = "am";

    private AppPreferences() {}

    public static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static Context wrapFontScale(Context context) {
        String size = prefs(context).getString(KEY_FONT_SIZE, FONT_DEFAULT);
        float scale = 1f;
        if (FONT_SMALL.equals(size)) {
            scale = 0.92f;
        } else if (FONT_LARGE.equals(size)) {
            scale = 1.12f;
        }
        Configuration cfg = new Configuration(context.getResources().getConfiguration());
        cfg.fontScale = scale;
        return context.createConfigurationContext(cfg);
    }

    public static boolean isAppLockEnabled(Context context) {
        return prefs(context).getBoolean(KEY_APP_LOCK, false);
    }

    public static boolean isBiometricEnabled(Context context) {
        return prefs(context).getBoolean(KEY_BIOMETRIC, false);
    }

    public static void applyStoredLocale(Context context) {
        String lang = prefs(context).getString(KEY_LANGUAGE, LANG_EN);
        LocaleListCompat locales = LANG_AM.equals(lang)
                ? LocaleListCompat.forLanguageTags(LANG_AM)
                : LocaleListCompat.forLanguageTags(LANG_EN);
        AppCompatDelegate.setApplicationLocales(locales);
    }

    public static String getLanguageCode(Context context) {
        String lang = prefs(context).getString(KEY_LANGUAGE, LANG_EN);
        return LANG_AM.equals(lang) ? LANG_AM : LANG_EN;
    }
}
