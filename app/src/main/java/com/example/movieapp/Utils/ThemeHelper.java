package com.example.movieapp.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {
    private static final String THEME_PREFS = "theme_prefs";
    private static final String IS_DARK_MODE = "is_dark_mode";

    public static void applyTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static void saveThemeMode(Context context, boolean isDarkMode) {
        SharedPreferences prefs = context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(IS_DARK_MODE, isDarkMode).apply();
    }

    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(IS_DARK_MODE, false);
    }
} 