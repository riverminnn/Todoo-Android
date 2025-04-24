package com.example.todooapp.utils;

public enum TodoSortOption {
    MODIFIED_DESC("Last modified (newest first)"),
    MODIFIED_ASC("Last modified (oldest first)"),
    CREATED_DESC("Creation date (newest first)"),
    CREATED_ASC("Creation date (oldest first)"),
    ALPHABETICAL_ASC("Title (A-Z)"),
    ALPHABETICAL_DESC("Title (Z-A)");

    private final String displayName;

    TodoSortOption(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}