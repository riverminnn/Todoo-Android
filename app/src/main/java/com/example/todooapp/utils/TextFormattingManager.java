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
        String text = editable.toString();
        boolean spanRemoved = false;

        int[] startLineExtents = getLineExtents(text, start);
        int[] endLineExtents = getLineExtents(text, end > start ? end - 1 : end);
        int adjustedStart = startLineExtents[0];
        int adjustedEnd = endLineExtents[1];

        String selectedText = text.substring(adjustedStart, adjustedEnd);
        String[] lines = selectedText.split("\n");

        int currentPosition = adjustedStart;
        int newEnd = adjustedEnd;

        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                int lineStart = currentPosition;
                int lineEnd = currentPosition + line.length();

                // Trim trailing whitespace from the line for span application
                String trimmedLine = line.trim();
                int trimmedLineEnd = lineStart + trimmedLine.length();

                android.text.style.BulletSpan[] existingBullets = editable.getSpans(
                        lineStart, lineEnd, android.text.style.BulletSpan.class);

                if (existingBullets.length > 0) {
                    for (android.text.style.BulletSpan bulletSpan : existingBullets) {
                        editable.removeSpan(bulletSpan);
                    }
                    spanRemoved = true;
                } else {
                    CheckboxSpan[] existingCheckboxes = editable.getSpans(lineStart, lineEnd, CheckboxSpan.class);
                    if (existingCheckboxes.length > 0) {
                        for (CheckboxSpan span : existingCheckboxes) {
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
                        if (editable.toString().startsWith(" ", lineStart)) {
                            editable.delete(lineStart, lineStart + 1);
                            newEnd--;
                            lineEnd--;
                            trimmedLineEnd--;
                        }
                    }

                    android.text.style.StrikethroughSpan[] strikeSpans = editable.getSpans(
                            lineStart, lineEnd, android.text.style.StrikethroughSpan.class);
                    for (android.text.style.StrikethroughSpan span : strikeSpans) {
                        editable.removeSpan(span);
                    }

                    int gapWidth = (int) (8 * context.getResources().getDisplayMetrics().density);
                    editable.setSpan(new android.text.style.BulletSpan(gapWidth),
                            lineStart, trimmedLineEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                currentPosition = lineEnd + 1;
            } else {
                currentPosition += line.length() + 1;
            }
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

        if ( strikeSpans.length > 0) {
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
        String text = editable.toString();
        boolean spanRemoved = false;

        // Adjust start and end to cover full lines
        int[] startLineExtents = getLineExtents(text, start);
        int[] endLineExtents = getLineExtents(text, end > start ? end - 1 : end);
        int adjustedStart = startLineExtents[0];
        int adjustedEnd = endLineExtents[1];

        // Split the text into lines, considering full lines
        String selectedText = text.substring(adjustedStart, adjustedEnd);
        String[] lines = selectedText.split("\n");

        int currentPosition = adjustedStart;
        int newEnd = adjustedEnd;

        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                int lineStart = currentPosition;
                int lineEnd = currentPosition + line.length();

                // Check for existing checkbox spans in this line
                CheckboxSpan[] existingSpans = editable.getSpans(lineStart, lineEnd, CheckboxSpan.class);

                if (existingSpans.length > 0) {
                    // Remove existing checkbox spans
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

                    // Remove the placeholder space if it exists
                    if (editable.toString().startsWith(" ", lineStart)) {
                        editable.delete(lineStart, lineStart + 1);
                        // Adjust end position due to deletion
                        newEnd--;
                        lineEnd--;
                    }

                    // Remove any existing strikethrough spans from the line
                    android.text.style.StrikethroughSpan[] strikeSpans = editable.getSpans(lineStart, lineEnd, android.text.style.StrikethroughSpan.class);
                    for (android.text.style.StrikethroughSpan span : strikeSpans) {
                        editable.removeSpan(span);
                    }

                    spanRemoved = true;
                } else {
                    // Remove any existing bullet spans
                    android.text.style.BulletSpan[] existingBullets = editable.getSpans(lineStart, lineEnd, android.text.style.BulletSpan.class);
                    for (android.text.style.BulletSpan bulletSpan : existingBullets) {
                        editable.removeSpan(bulletSpan);
                    }

                    // Insert a placeholder space for the checkbox
                    editable.insert(lineStart, " ");
                    newEnd++; // Adjust end position due to insertion
                    lineEnd++;

                    // Apply new checkbox spans
                    int checkboxColor = ContextCompat.getColor(context, R.color.checkbox_selected);
                    if (fontAwesome == null) {
                        fontAwesome = ResourcesCompat.getFont(context, R.font.fa_free_regular_400);
                    }

                    CheckboxSpan checkboxSpan = new CheckboxSpan(this, fontAwesome, checkboxColor, false);
                    CheckboxClickSpan clickSpan = new CheckboxClickSpan(checkboxSpan);
                    NonEditableSpan nonEditableSpan = new NonEditableSpan();

                    // Apply spans: CheckboxSpan across the line, ClickSpan and NonEditableSpan on the placeholder
                    editable.setSpan(checkboxSpan, lineStart, lineEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    editable.setSpan(clickSpan, lineStart, lineStart + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    editable.setSpan(nonEditableSpan, lineStart, lineStart + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                // Update current position to the start of the next line (+1 for the newline)
                currentPosition = lineEnd + 1;
            } else {
                // Move to the next line (+1 for the newline)
                currentPosition += line.length() + 1;
            }
        }

        return !spanRemoved;
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

    public boolean toggleHeading(Editable editable, int start, int end) {
        String text = editable.toString();
        boolean spanRemoved = false;

        // Adjust start and end to cover full lines
        int[] startLineExtents = getLineExtents(text, start);
        int[] endLineExtents = getLineExtents(text, end > start ? end - 1 : end);
        int adjustedStart = startLineExtents[0];
        int adjustedEnd = endLineExtents[1];

        // Split the text into lines
        String selectedText = text.substring(adjustedStart, adjustedEnd);
        String[] lines = selectedText.split("\n");

        int currentPosition = adjustedStart;

        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                int lineStart = currentPosition;
                int lineEnd = currentPosition + line.length();

                // Check if this line already has heading formatting
                android.text.style.RelativeSizeSpan[] sizeSpans = editable.getSpans(
                        lineStart, lineEnd, android.text.style.RelativeSizeSpan.class);

                boolean lineHasHeading = false;

                for (android.text.style.RelativeSizeSpan span : sizeSpans) {
                    if (Math.abs(span.getSizeChange() - 1.5f) < 0.1) {
                        editable.removeSpan(span);
                        lineHasHeading = true;
                        spanRemoved = true;
                    }
                }

                // Also remove associated bold spans if heading is being removed
                if (lineHasHeading) {
                    StyleSpan[] styleSpans = editable.getSpans(lineStart, lineEnd, StyleSpan.class);
                    for (StyleSpan span : styleSpans) {
                        if (span.getStyle() == android.graphics.Typeface.BOLD) {
                            editable.removeSpan(span);
                        }
                    }
                } else {
                    // Apply heading formatting (larger text + bold)
                    editable.setSpan(new android.text.style.RelativeSizeSpan(1.5f),
                            lineStart, lineEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    editable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                            lineStart, lineEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            // Update current position to the start of the next line
            currentPosition += line.length() + 1; // +1 for the newline
        }

        return !spanRemoved;
    }
}