package com.example.todooapp.utils.settings;

public enum ThemeOption {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System");

    private final String displayName;

    ThemeOption(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}