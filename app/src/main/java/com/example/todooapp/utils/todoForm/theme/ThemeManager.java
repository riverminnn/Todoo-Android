package com.example.todooapp.utils.todoForm.theme;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.example.todooapp.R;

import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    // Theme constants
    public static final int THEME_DEFAULT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SEPIA = 2;
    public static final int THEME_BLUE = 3;
    public static final int THEME_PINK = 4;

    // Theme definition class
    public static class ThemeOption {
        public final String name;
        public final @ColorRes int backgroundColor;
        public final @ColorRes int textColor;
        public final @ColorRes int hintColor;
        public final @ColorRes int iconColor;
        public final @ColorRes int accentColor;

        public ThemeOption(String name, int backgroundColor, int textColor,
                           int hintColor, int iconColor, int accentColor) {
            this.name = name;
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
            this.hintColor = hintColor;
            this.iconColor = iconColor;
            this.accentColor = accentColor;
        }
    }

    // List of available themes
    private static List<ThemeOption> themes = null;

    // Get all available themes
    public static List<ThemeOption> getThemes() {
        if (themes == null) {
            themes = new ArrayList<>();
            themes.add(new ThemeOption("Light", R.color.white, R.color.black,
                    R.color.gray_600, R.color.black, R.color.black));
            themes.add(new ThemeOption("Dark", R.color.dark_background, R.color.white,
                    R.color.gray_300, R.color.white, R.color.dark_background));
            themes.add(new ThemeOption("Sepia", R.color.sepia_background, R.color.sepia_text,
                    R.color.sepia_hint, R.color.sepia_text, R.color.sepia_accent));
            themes.add(new ThemeOption("Blue", R.color.blue_background, R.color.blue_text,
                    R.color.blue_hint, R.color.blue_text, R.color.blue_accent));
            themes.add(new ThemeOption("Pink", R.color.pink_background, R.color.pink_text,
                    R.color.pink_hint, R.color.pink_text, R.color.pink_accent));
        }
        return themes;
    }
    // Helper method to determine if a color is light or dark
    private static boolean isLightColor(int color) {
        double darkness = 1 - (0.299 * android.graphics.Color.red(color) +
                0.587 * android.graphics.Color.green(color) +
                0.114 * android.graphics.Color.blue(color)) / 255;
        return darkness < 0.5;
    }

    // Recursive function to find and apply theme to all text views
    private static void applyThemeToTextViews(Context context, View view, ThemeOption theme) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(ContextCompat.getColor(context, theme.textColor));

            // Special handling for EditText (hint color)
            if (view instanceof EditText) {
                ((EditText) view).setHintTextColor(ContextCompat.getColor(context, theme.hintColor));
            }
        } else if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup viewGroup = (android.view.ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                applyThemeToTextViews(context, viewGroup.getChildAt(i), theme);
            }
        }
    }

    // Add this new method to ThemeManager.java
    public static void forceCompleteThemeApplication(Activity activity, View rootView, int themeIndex) {
        ThemeOption theme = getThemes().get(themeIndex);
        int backgroundColor = ContextCompat.getColor(activity, theme.backgroundColor);

        // Force clear all main container backgrounds first
        View appBarLayout = rootView.findViewById(R.id.appBarLayout);
        androidx.appcompat.widget.Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        View bottomActionBar = rootView.findViewById(R.id.bottomActionBar);

        if (appBarLayout != null) {
            appBarLayout.setBackground(null);
            appBarLayout.setBackgroundColor(backgroundColor);
        }

        if (toolbar != null) {
            toolbar.setBackground(null);
            toolbar.setBackgroundColor(backgroundColor);
        }

        if (bottomActionBar != null) {
            bottomActionBar.setBackground(null);
            bottomActionBar.setBackgroundColor(backgroundColor);
        }

        // Set root view background
        rootView.setBackgroundColor(backgroundColor);

        // Apply theme to text elements
        applyThemeToTextViews(activity, rootView, theme);

        // Update system bars
        Window window = activity.getWindow();
        window.setStatusBarColor(backgroundColor);
        window.setNavigationBarColor(backgroundColor);

        // Adjust status bar icon colors
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int flags = rootView.getSystemUiVisibility();
            if (isLightColor(backgroundColor)) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            window.getDecorView().setSystemUiVisibility(flags);
        }

        // Force layout update
        rootView.invalidate();
        rootView.requestLayout();
    }
}