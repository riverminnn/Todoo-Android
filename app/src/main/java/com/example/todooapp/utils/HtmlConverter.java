package com.example.todooapp.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;

import com.example.todooapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlConverter {

    // Convert spannable to HTML string (for saving)
    public static String toHtml(Context context, Spannable spannable) {
        // First, convert standard spans to HTML
        String html = HtmlCompat.toHtml(spannable, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);

        // Handle checkboxes
        CheckboxSpan[] checkboxSpans = spannable.getSpans(0, spannable.length(), CheckboxSpan.class);
        for (CheckboxSpan span : checkboxSpans) {
            int start = spannable.getSpanStart(span);
            int end = spannable.getSpanEnd(span);
            String content = spannable.toString().substring(start, end);
            // Remove the placeholder space at the beginning
            if (content.startsWith(" ")) {
                content = content.substring(1);
            }
            // Replace the original text with our custom tag
            String checkboxTag = "<todoo-checkbox checked=\"" + span.isChecked() + "\">" + content + "</todoo-checkbox>";
            html = html.replaceFirst(Pattern.quote(content), checkboxTag);
        }

        // Handle bullet points
        android.text.style.BulletSpan[] bulletSpans = spannable.getSpans(0, spannable.length(), android.text.style.BulletSpan.class);
        for (android.text.style.BulletSpan span : bulletSpans) {
            int start = spannable.getSpanStart(span);
            int end = spannable.getSpanEnd(span);
            String content = spannable.toString().substring(start, end);
            // Replace with custom tag, ensuring exact match
            String bulletTag = "<todoo-bullet>" + content + "</todoo-bullet>";
            html = html.replaceFirst(Pattern.quote(content), bulletTag);
        }

        // Handle headings (H1)
        RelativeSizeSpan[] sizeSpans = spannable.getSpans(0, spannable.length(), RelativeSizeSpan.class);
        for (RelativeSizeSpan span : sizeSpans) {
            // Only process spans that are likely headings (around 1.5x size)
            if (Math.abs(span.getSizeChange() - 1.5f) < 0.1) {
                int start = spannable.getSpanStart(span);
                int end = spannable.getSpanEnd(span);
                String content = spannable.toString().substring(start, end);
                // Ensure bold style is preserved by checking StyleSpan
                StyleSpan[] styleSpans = spannable.getSpans(start, end, StyleSpan.class);
                boolean isBold = false;
                for (StyleSpan styleSpan : styleSpans) {
                    if (styleSpan.getStyle() == Typeface.BOLD) {
                        isBold = true;
                        break;
                    }
                }
                // Replace with custom tag, preserving bold if present
                String headingTag = "<todoo-heading" + (isBold ? " bold=\"true\"" : "") + ">" + content + "</todoo-heading>";
                html = html.replaceFirst(Pattern.quote(content), headingTag);
            }
        }

        return html;
    }

    // Convert HTML string to spannable (for loading)
    public static Spannable fromHtml(Context context, String html) {
        if (html == null || html.isEmpty()) return new SpannableString("");

        TextFormattingManager textFormattingManager = new TextFormattingManager(context);
        Typeface fontAwesome = ResourcesCompat.getFont(context, R.font.fa_free_regular_400);
        int checkboxColor = ContextCompat.getColor(context, R.color.checkbox_selected);

        // Store information about custom spans
        List<CustomSpanInfo> customSpans = new ArrayList<>();

        // Extract checkbox information
        Pattern checkboxPattern = Pattern.compile("<todoo-checkbox checked=\"([^\"]*)\">(.+?)</todoo-checkbox>");
        Matcher checkboxMatcher = checkboxPattern.matcher(html);
        while (checkboxMatcher.find()) {
            boolean isChecked = Boolean.parseBoolean(checkboxMatcher.group(1));
            String content = checkboxMatcher.group(2);
            customSpans.add(new CustomSpanInfo("checkbox", content, isChecked));
        }

        // Extract bullet information
        Pattern bulletPattern = Pattern.compile("<todoo-bullet>(.+?)</todoo-bullet>");
        Matcher bulletMatcher = bulletPattern.matcher(html);
        while (bulletMatcher.find()) {
            String content = bulletMatcher.group(1);
            customSpans.add(new CustomSpanInfo("bullet", content, false));
        }

        // Extract heading information
        Pattern headingPattern = Pattern.compile("<todoo-heading(?: bold=\"([^\"]*)\")?>(.+?)</todoo-heading>");
        Matcher headingMatcher = headingPattern.matcher(html);
        while (headingMatcher.find()) {
            String boldAttr = headingMatcher.group(1); // May be null if bold attribute is not present
            boolean isBold = "true".equals(boldAttr);
            String content = headingMatcher.group(2);
            customSpans.add(new CustomSpanInfo("heading", content, isBold));
        }

        // Remove custom tags for standard parsing, preserving content
        String standardHtml = html
                .replaceAll("<todoo-checkbox checked=\"[^\"]*\">(.+?)</todoo-checkbox>", "$1")
                .replaceAll("<todoo-bullet>(.+?)</todoo-bullet>", "$1")
                .replaceAll("<todoo-heading(?: bold=\"[^\"]*\")?>(.+?)</todoo-heading>", "$1");

        // Parse the modified HTML to retain standard formatting (bold, italic, etc.)
        Spanned spanned = HtmlCompat.fromHtml(standardHtml, HtmlCompat.FROM_HTML_MODE_LEGACY);
        SpannableStringBuilder builder = new SpannableStringBuilder(spanned);

        // Re-apply custom spans
        int offset = 0; // Track offset due to checkbox space insertions
        for (CustomSpanInfo spanInfo : customSpans) {
            String content = spanInfo.content;
            // Find the exact position of the content in the builder
            int start = builder.toString().indexOf(content, offset);
            if (start == -1) {
                // Try trimmed content to handle whitespace issues
                content = content.trim();
                start = builder.toString().indexOf(content, offset);
            }

            if (start >= 0) {
                int end = start + content.length();

                if ("checkbox".equals(spanInfo.type)) {
                    // Add space for checkbox
                    builder.insert(start, " ");
                    end++; // Adjust for inserted space
                    offset++; // Update offset for subsequent spans

                    // Apply checkbox spans
                    CheckboxSpan checkboxSpan = new CheckboxSpan(textFormattingManager, fontAwesome, checkboxColor, spanInfo.isChecked);
                    CheckboxClickSpan clickSpan = new CheckboxClickSpan(checkboxSpan);
                    NonEditableSpan nonEditableSpan = new NonEditableSpan();

                    builder.setSpan(checkboxSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(clickSpan, start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(nonEditableSpan, start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // Apply strikethrough if checked
                    if (spanInfo.isChecked) {
                        builder.setSpan(new StrikethroughSpan(), start + 1, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else if ("bullet".equals(spanInfo.type)) {
                    // Apply bullet span, ensuring it doesn't overwrite existing spans
                    int gapWidth = (int) (8 * context.getResources().getDisplayMetrics().density);
                    // Check for existing BulletSpan to avoid duplication
                    android.text.style.BulletSpan[] existingBullets = builder.getSpans(start, end, android.text.style.BulletSpan.class);
                    if (existingBullets.length == 0) {
                        builder.setSpan(new android.text.style.BulletSpan(gapWidth), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else if ("heading".equals(spanInfo.type)) {
                    // Apply heading spans (larger text + bold if specified)
                    builder.setSpan(new RelativeSizeSpan(1.5f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (spanInfo.isChecked) { // isChecked repurposed to mean isBold for headings
                        // Only apply bold if not already present
                        StyleSpan[] existingBold = builder.getSpans(start, end, StyleSpan.class);
                        boolean hasBold = false;
                        for (StyleSpan span : existingBold) {
                            if (span.getStyle() == Typeface.BOLD) {
                                hasBold = true;
                                break;
                            }
                        }
                        if (!hasBold) {
                            builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }

                // Update offset to start after the current span
                offset = end;
            }
        }

        return builder;
    }

    // Helper class to store custom span information
    private static class CustomSpanInfo {
        String type;
        String content;
        boolean isChecked; // Used for checkbox checked state or bold state for headings

        CustomSpanInfo(String type, String content, boolean isChecked) {
            this.type = type;
            this.content = content;
            this.isChecked = isChecked;
        }
    }
}