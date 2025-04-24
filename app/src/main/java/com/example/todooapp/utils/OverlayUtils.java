package com.example.todooapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;

public class OverlayUtils {
    private static View backgroundOverlay; // Track the overlay to avoid duplicates

    /**
     * Shows a semi-transparent overlay over the entire activity.
     *
     * @param context The context (preferably from an Activity or Fragment).
     * @param onOverlayClickListener Optional click listener for the overlay.
     * @return The overlay view that was added.
     */
    public static View showOverlay(Context context, View.OnClickListener onOverlayClickListener) {
        if (backgroundOverlay != null) {
            // Overlay already exists, return it
            return backgroundOverlay;
        }

        // Get the activity's root view
        ViewGroup rootView = (ViewGroup) ((android.app.Activity) context)
                .getWindow()
                .getDecorView()
                .findViewById(android.R.id.content);
        if (rootView == null) {
            return null;
        }

        // Create the overlay view
        backgroundOverlay = new View(context);
        backgroundOverlay.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        backgroundOverlay.setBackground(new ColorDrawable(Color.parseColor("#80000000"))); // Semi-transparent black
        backgroundOverlay.setClickable(true);
        backgroundOverlay.setFocusable(true);
        backgroundOverlay.setElevation(8f); // Ensure it appears above other views
        backgroundOverlay.setContentDescription("Background overlay, tap to dismiss");

        // Add the overlay to the root view
        rootView.addView(backgroundOverlay);

        // Set the click listener if provided
        if (onOverlayClickListener != null) {
            backgroundOverlay.setOnClickListener(onOverlayClickListener);
        }

        return backgroundOverlay;
    }

    /**
     * Hides the overlay if it is currently shown.
     */
    public static void hideOverlay(Context context) {
        if (backgroundOverlay == null) {
            return;
        }

        // Get the activity's root view
        ViewGroup rootView = (ViewGroup) ((android.app.Activity) context)
                .getWindow()
                .getDecorView()
                .findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.removeView(backgroundOverlay);
            backgroundOverlay = null;
        }
    }

    /**
     * Checks if the overlay is currently visible.
     *
     * @return True if the overlay is visible, false otherwise.
     */
    public static boolean isOverlayVisible() {
        return backgroundOverlay != null;
    }
}