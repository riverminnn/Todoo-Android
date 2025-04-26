package com.example.todooapp.utils;

import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;

public class HtmlConverter {
    /**
     * Convert a Spanned text to HTML format for storage
     */
    public static String toHtml(Spanned spanned) {
        if (spanned == null) return "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.toHtml(spanned, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        } else {
            return Html.toHtml(spanned);
        }
    }

    /**
     * Convert HTML to a Spanned object for display
     */
    public static Spanned fromHtml(String html) {
        if (html == null || html.isEmpty()) return new SpannableString("");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(html);
        }
    }
}