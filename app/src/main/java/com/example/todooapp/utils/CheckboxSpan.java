package com.example.todooapp.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.LeadingMarginSpan;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.example.todooapp.R;

public class CheckboxSpan implements LeadingMarginSpan {
    private boolean isChecked;
    private final TextFormattingManager textFormattingManager;
    private final TextPaint textPaint;
    private final int color;
    private final Typeface fontAwesome;
    private final float scale;
    private final int indentWidth; // Indent for all lines
    private final int gapWidth; // Gap between checkbox and text

    public CheckboxSpan(TextFormattingManager textFormattingManager, Typeface fontAwesome, int color, boolean isChecked) {
        this.textFormattingManager = textFormattingManager;
        this.fontAwesome = fontAwesome;
        this.color = color;
        this.isChecked = isChecked;
        this.scale = 1.3f;
        this.gapWidth = 32; // Gap between checkbox and text
        this.indentWidth = 40; // Indent width to push the line (adjust as needed)
        textPaint = new TextPaint();
        textPaint.setTypeface(fontAwesome);
    }

    @Override
    public int getLeadingMargin(boolean first) {
        // Apply the same indentation to all lines of the paragraph
        float checkboxWidth = textPaint.measureText(isChecked ? "\uf14a" : "\uf0c8");
        return (int) (checkboxWidth + gapWidth + indentWidth);
    }

    @Override
    public void drawLeadingMargin(Canvas canvas, Paint paint, int x, int dir, int top, int baseline, int bottom,
                                  CharSequence text, int start, int end, boolean first, Layout layout) {
        if (!first) {
            return; // Only draw the checkbox on the first line of the paragraph
        }

        textPaint.set(paint);
        textPaint.setColor(isChecked ? color : paint.getColor());
        textPaint.setTypeface(fontAwesome);
        textPaint.setTextSize(paint.getTextSize() * scale);

        // Draw the checkbox
        float checkboxX = x + indentWidth; // Position the checkbox with the indent
        canvas.drawText(isChecked ? "\uf14a" : "\uf0c8", checkboxX, baseline, textPaint);
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void toggle(Editable editable, int spanStart, int spanEnd) {
        isChecked = !isChecked;

        // Find the end of the paragraph for strikethrough formatting
        String text = editable.toString();
        int lineEnd = spanEnd;
        while (lineEnd < text.length() && text.charAt(lineEnd) != '\n') {
            lineEnd++;
        }

        // Apply or remove strikethrough based on checkbox state
        textFormattingManager.toggleStrikeThrough(editable, spanEnd, lineEnd, isChecked);
    }
}