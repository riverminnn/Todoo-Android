package com.example.todooapp.utils;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import androidx.core.content.ContextCompat;

import com.example.todooapp.R;

public class TextFormattingManager {
    private final Context context;

    public TextFormattingManager(Context context) {
        this.context = context;
    }

    public boolean toggleBold(Editable editable, int start, int end) {
        boolean spanRemoved = false;
        // Check for existing bold spans
        StyleSpan[] boldSpans = editable.getSpans(start, end, StyleSpan.class);
        for (StyleSpan span : boldSpans) {
            if (span.getStyle() == android.graphics.Typeface.BOLD) {
                editable.removeSpan(span);
                spanRemoved = true;
            }
        }

        // Add a new bold span if none was removed
        if (!spanRemoved) {
            editable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return !spanRemoved; // Return true if span was applied, false if removed
    }

    public boolean toggleItalic(Editable editable, int start, int end) {
        boolean spanRemoved = false;
        // Check for existing italic spans
        StyleSpan[] italicSpans = editable.getSpans(start, end, StyleSpan.class);
        for (StyleSpan span : italicSpans) {
            if (span.getStyle() == android.graphics.Typeface.ITALIC) {
                editable.removeSpan(span);
                spanRemoved = true;
            }
        }

        // Add a new italic span if none was removed
        if (!spanRemoved) {
            editable.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return !spanRemoved;
    }

    public boolean toggleUnderline(Editable editable, int start, int end) {
        UnderlineSpan[] underlineSpans = editable.getSpans(start, end, UnderlineSpan.class);
        boolean spanRemoved = false;

        if (underlineSpans.length > 0) {
            for (UnderlineSpan span : underlineSpans) {
                editable.removeSpan(span);
            }
            spanRemoved = true;
        }

        if (!spanRemoved) {
            editable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return !spanRemoved;
    }

    public boolean toggleHighlight(Editable editable, int start, int end) {
        BackgroundColorSpan[] highlightSpans = editable.getSpans(start, end, BackgroundColorSpan.class);
        boolean spanRemoved = false;

        if (highlightSpans.length > 0) {
            for (BackgroundColorSpan span : highlightSpans) {
                editable.removeSpan(span);
            }
            spanRemoved = true;
        }

        if (!spanRemoved) {
            editable.setSpan(new BackgroundColorSpan(
                            ContextCompat.getColor(context, R.color.highlightColor)),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return !spanRemoved;
    }

    public boolean toggleBullet(Editable editable, int start, int end) {
        android.text.style.BulletSpan[] existingBullets = editable.getSpans(
                start, end, android.text.style.BulletSpan.class);
        boolean spanRemoved = false;

        if (existingBullets != null && existingBullets.length > 0) {
            // Remove existing bullet spans
            for (android.text.style.BulletSpan bulletSpan : existingBullets) {
                editable.removeSpan(bulletSpan);
            }
            spanRemoved = true;
        }

        if (!spanRemoved) {
            // Add a bullet span since none exists
            int gapWidth = (int) (8 * context.getResources().getDisplayMetrics().density);
            editable.setSpan(new android.text.style.BulletSpan(gapWidth),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return !spanRemoved;
    }

    public int[] getLineExtents(String text, int cursorPosition) {
        int lineStart = cursorPosition;
        while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
            lineStart--;
        }

        int lineEnd = cursorPosition;
        while (lineEnd < text.length() && text.charAt(lineEnd) != '\n') {
            lineEnd++;
        }

        return new int[] {lineStart, lineEnd};
    }
}