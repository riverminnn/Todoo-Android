package com.example.todooapp.utils.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class SettingsManager {
    private static final String PREF_NAME = "todoo_settings";

    // Setting keys
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_SORT_OPTION = "sort_option";
    private static final String KEY_LAYOUT_TYPE = "layout_type";

    // Default values
    private static final String DEFAULT_FONT_SIZE = "Medium";
    private static final String DEFAULT_SORT_OPTION = "CREATED_DESC";
    private static final String DEFAULT_LAYOUT_TYPE = "List view";

    // Add these fields
    private static final String KEY_THEME_OPTION = "theme_option";
    private static final String DEFAULT_THEME_OPTION = "SYSTEM";

    private SharedPreferences preferences;

    public SettingsManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Font size methods
    public String getFontSize() {
        return preferences.getString(KEY_FONT_SIZE, DEFAULT_FONT_SIZE);
    }

    public void setFontSize(String fontSize) {
        preferences.edit().putString(KEY_FONT_SIZE, fontSize).apply();
    }

    // Sort option methods
    public TodoSortOption getSortOption() {
        String sortName = preferences.getString(KEY_SORT_OPTION, DEFAULT_SORT_OPTION);
        try {
            return TodoSortOption.valueOf(sortName);
        } catch (IllegalArgumentException e) {
            return TodoSortOption.CREATED_DESC;
        }
    }

    public void setSortOption(TodoSortOption sortOption) {
        preferences.edit().putString(KEY_SORT_OPTION, sortOption.name()).apply();
    }

    // Layout type methods
    public String getLayoutType() {
        return preferences.getString(KEY_LAYOUT_TYPE, DEFAULT_LAYOUT_TYPE);
    }

    public void setLayoutType(String layoutType) {
        preferences.edit().putString(KEY_LAYOUT_TYPE, layoutType).apply();
    }

    // Add these methods
    public ThemeOption getThemeOption() {
        String themeName = preferences.getString(KEY_THEME_OPTION, DEFAULT_THEME_OPTION);
        try {
            return ThemeOption.valueOf(themeName);
        } catch (IllegalArgumentException e) {
            return ThemeOption.SYSTEM;
        }
    }

    public void setThemeOption(ThemeOption themeOption) {
        preferences.edit().putString(KEY_THEME_OPTION, themeOption.name()).apply();
    }

    // Helper method to apply theme
    public void applyTheme() {
        ThemeOption option = getThemeOption();
        int nightMode;

        switch (option) {
            case LIGHT:
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case DARK:
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }

        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
}