package com.example.to_dolist.core.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

/**
 * Central keys for {@link SharedPreferences} and helpers for persisted locale/theme.
 */
public final class AppPreferences {

    public static final String PREFS_NAME = "todo_settings";
    public static final String KEY_DARK_MODE = "dark_mode";
    public static final String KEY_LANGUAGE = "language";

    public static final String LANG_EN = "en";
    public static final String LANG_AM = "am";

    private AppPreferences() {}

    public static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
