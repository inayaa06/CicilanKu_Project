package com.example.cicilanku;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    private static final String PREF_NAME = "cicilanku_preferences";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_REMINDER = "reminder_enabled";
    private final SharedPreferences preferences;

    public PrefsManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isDarkMode() {
        return preferences.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean enabled) {
        preferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public boolean isReminderEnabled() {
        return preferences.getBoolean(KEY_REMINDER, true);
    }

    public void setReminderEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_REMINDER, enabled).apply();
    }
}
