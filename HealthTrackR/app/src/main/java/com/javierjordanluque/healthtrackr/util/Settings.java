package com.javierjordanluque.healthtrackr.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    private static final String PREFS_NAME = "HealthTrackR_settings";
    private static final String PREFS_THEME = "theme";
    public static final String SYSTEM_DEFAULT = "system_default";
    public static final String LIGHT = "light";
    public static final String DARK = "dark";

    public static void saveTheme(Context context, String theme) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFS_THEME, theme);
        editor.apply();
    }

    public static void clearSettings(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREFS_THEME);
        editor.apply();
    }

    public static String[] getSettings(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String theme = sharedPreferences.getString(PREFS_THEME, null);

        return new String[]{theme};
    }
}
