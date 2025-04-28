package com.example.todooapp.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class LocationSpan extends ReplacementSpan {
    private static final String TAG = "LocationSpan";
    private final Context context;
    private final double latitude;
    private final double longitude;
    private final String locationName;
    private final int iconSize;
    private final Drawable locationIcon;

    public LocationSpan(Context context, double latitude, double longitude, String locationName) {
        this.context = context;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.iconSize = (int) (24 * context.getResources().getDisplayMetrics().density);
        this.locationIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation);

        if (this.locationIcon != null) {
            this.locationIcon.setBounds(0, 0, iconSize, iconSize);
        }
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end,
                       @Nullable Paint.FontMetricsInt fm) {
        return (int) (paint.measureText(text, start, end) + iconSize + 8);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top,
                     int y, int bottom, @NonNull Paint paint) {
        // Draw the location icon
        canvas.save();
        int iconY = y - iconSize + (bottom - top) / 4;

        if (locationIcon != null) {
            locationIcon.setBounds((int) x, iconY, (int) x + iconSize, iconY + iconSize);
            locationIcon.draw(canvas);
        }

        // Draw the text after the icon
        canvas.drawText(text, start, end, x + iconSize + 8, y, paint);
        canvas.restore();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public void onClick(View v) {
        try {
            // Create a URI for Google Maps with the location coordinates
            Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude +
                    "?q=" + latitude + "," + longitude + "(" + locationName + ")");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            } else {
                // Fallback to browser if Google Maps isn't installed
                Uri browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" +
                        latitude + "," + longitude);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                context.startActivity(browserIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening map", e);
        }
    }
}