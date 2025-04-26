package com.example.todooapp.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;
import androidx.annotation.NonNull;

public class NonEditableSpan extends ReplacementSpan {
    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return 0; // The placeholder takes no width since the checkbox is drawn by LeadingMarginSpan
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        // Do nothing, as the checkbox is drawn by LeadingMarginSpan
    }
}