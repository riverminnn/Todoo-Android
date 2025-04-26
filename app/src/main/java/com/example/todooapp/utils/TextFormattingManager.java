package com.example.todooapp.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.todooapp.R;

public class TextFormattingManager {
    private final Context context;
    private Typeface fontAwesome;

    public TextFormattingManager(Context context) {
        this.context = context;
        fontAwesome = ResourcesCompat.getFont(context, R.font.fa_free_regular_400);

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
            // Remove any existing checkbox spans (and associated spans)
            CheckboxSpan[] existingCheckboxes = editable.getSpans(start, end, CheckboxSpan.class);
            if (existingCheckboxes.length > 0) {
                for (CheckboxSpan span : existingCheckboxes) {
                    editable.removeSpan(span);
                }
                CheckboxClickSpan[] clickSpans = editable.getSpans(start, end, CheckboxClickSpan.class);
                for (CheckboxClickSpan span : clickSpans) {
                    editable.removeSpan(span);
                }
                NonEditableSpan[] nonEditableSpans = editable.getSpans(start, end, NonEditableSpan.class);
                for (NonEditableSpan span : nonEditableSpans) {
                    editable.removeSpan(span);
                }
                // Remove the placeholder space if it exists
                if (editable.toString().startsWith(" ", start)) {
                    editable.delete(start, start + 1);
                    end--; // Adjust the end position after deletion
                }
            }

            // Remove any existing strikethrough spans
            android.text.style.StrikethroughSpan[] strikeSpans = editable.getSpans(
                    start, end, android.text.style.StrikethroughSpan.class);
            for (android.text.style.StrikethroughSpan span : strikeSpans) {
                editable.removeSpan(span);
            }

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

    public boolean toggleStrikeThrough(Editable editable, int start, int end) {
        android.text.style.StrikethroughSpan[] strikeSpans = editable.getSpans(
                start, end, android.text.style.StrikethroughSpan.class);
        boolean spanRemoved = false;

        if (strikeSpans.length > 0) {
            for (android.text.style.StrikethroughSpan span : strikeSpans) {
                editable.removeSpan(span);
            }
            spanRemoved = true;
        }

        if (!spanRemoved) {
            editable.setSpan(new android.text.style.StrikethroughSpan(),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return !spanRemoved;
    }

    public boolean toggleCheckbox(Editable editable, int start, int end) {
        // Find the start of the line
        String text = editable.toString();
        int lineStart = start;
        while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
            lineStart--;
        }

        // Find the end of the paragraph
        int lineEnd = start;
        while (lineEnd < text.length() && text.charAt(lineEnd) != '\n') {
            lineEnd++;
        }

        // Check for existing checkbox spans
        CheckboxSpan[] existingSpans = editable.getSpans(lineStart, lineEnd, CheckboxSpan.class);

        if (existingSpans.length > 0) {
            // Checkbox already exists, remove it
            for (CheckboxSpan span : existingSpans) {
                editable.removeSpan(span);
            }
            CheckboxClickSpan[] clickSpans = editable.getSpans(lineStart, lineStart + 1, CheckboxClickSpan.class);
            for (CheckboxClickSpan span : clickSpans) {
                editable.removeSpan(span);
            }
            NonEditableSpan[] nonEditableSpans = editable.getSpans(lineStart, lineStart + 1, NonEditableSpan.class);
            for (NonEditableSpan span : nonEditableSpans) {
                editable.removeSpan(span);
            }

            // Remove any existing strikethrough spans from the paragraph
            android.text.style.StrikethroughSpan[] strikeSpans = editable.getSpans(lineStart, lineEnd, android.text.style.StrikethroughSpan.class);
            for (android.text.style.StrikethroughSpan span : strikeSpans) {
                editable.removeSpan(span);
            }

            // Remove the placeholder space
            if (text.startsWith(" ", lineStart)) {
                editable.delete(lineStart, lineStart + 1);
            }
            return false;
        } else {
            // Remove any existing bullet spans
            android.text.style.BulletSpan[] existingBullets = editable.getSpans(lineStart, lineEnd, android.text.style.BulletSpan.class);
            for (android.text.style.BulletSpan bulletSpan : existingBullets) {
                editable.removeSpan(bulletSpan);
            }

            // Insert a placeholder space for the checkbox
            editable.insert(lineStart, " ");

            // Apply new checkbox span
            int checkboxColor = ContextCompat.getColor(context, R.color.checkbox_selected);
            if (fontAwesome == null) {
                fontAwesome = ResourcesCompat.getFont(context, R.font.fa_free_regular_400);
            }

            CheckboxSpan checkboxSpan = new CheckboxSpan(this, fontAwesome, checkboxColor, false);
            CheckboxClickSpan clickSpan = new CheckboxClickSpan(checkboxSpan);
            NonEditableSpan nonEditableSpan = new NonEditableSpan();

            // Apply the spans: CheckboxSpan across the entire paragraph, ClickSpan and NonEditableSpan on the placeholder
            editable.setSpan(checkboxSpan, lineStart, lineEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            editable.setSpan(clickSpan, lineStart, lineStart + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            editable.setSpan(nonEditableSpan, lineStart, lineStart + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            return false;
        }
    }

    // Helper method to add or remove strikethrough without toggling
    protected void toggleStrikeThrough(Editable editable, int start, int end, boolean apply) {
        // Remove any existing strikethroughs first
        android.text.style.StrikethroughSpan[] strikeSpans = editable.getSpans(
                start, end, android.text.style.StrikethroughSpan.class);
        for (android.text.style.StrikethroughSpan span : strikeSpans) {
            editable.removeSpan(span);
        }

        // Apply new strikethrough if needed
        if (apply) {
            editable.setSpan(new android.text.style.StrikethroughSpan(),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}