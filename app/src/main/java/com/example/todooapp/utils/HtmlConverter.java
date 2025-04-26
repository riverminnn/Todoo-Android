package com.example.todooapp.utils;

import android.content.Context;
import android.graphics.Typeface;
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
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlConverter {

    public static String toHtml(Context context, Spannable spannable) {
        SpannableStringBuilder builder = new SpannableStringBuilder(spannable);
        StringBuilder htmlBuilder = new StringBuilder();

        // Collect all spans (checkboxes, bullets, headings)
        Object[] allSpans = builder.getSpans(0, builder.length(), Object.class);
        List<SpanInfo> spanInfos = new ArrayList<>();

        for (Object span : allSpans) {
            if (span instanceof CheckboxSpan || span instanceof android.text.style.BulletSpan || span instanceof RelativeSizeSpan) {
                int start = builder.getSpanStart(span);
                int end = builder.getSpanEnd(span);
                spanInfos.add(new SpanInfo(start, end, span));
            }
        }

        // Sort spans by start position
        spanInfos.sort(Comparator.comparingInt(s -> s.start));

        int currentPosition = 0;
        for (SpanInfo spanInfo : spanInfos) {
            // Append text before the span
            if (currentPosition < spanInfo.start) {
                CharSequence sub = builder.subSequence(currentPosition, spanInfo.start);
                String subHtml = HtmlCompat.toHtml(new SpannableString(sub), HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
                htmlBuilder.append(subHtml);
            }

            // Handle the span
            if (spanInfo.span instanceof CheckboxSpan) {
                CheckboxSpan checkboxSpan = (CheckboxSpan) spanInfo.span;
                int start = spanInfo.start;
                int end = spanInfo.end;

                // Extract the content with its formatting
                Spannable contentSpannable = new SpannableStringBuilder(builder.subSequence(start, end));
                // Remove the placeholder space from the content for HTML
                if (contentSpannable.toString().startsWith(" ")) {
                    contentSpannable = new SpannableStringBuilder(contentSpannable.subSequence(1, contentSpannable.length()));
                }

                // Convert the content to HTML, preserving all formatting
                String contentHtml = HtmlCompat.toHtml(contentSpannable, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                        .replaceAll("<p[^>]*>", "") // Remove <p> tags
                        .replaceAll("</p>", "") // Remove </p> tags
                        .trim();

                String checkboxTag = "<todoo-checkbox checked=\"" + checkboxSpan.isChecked() + "\">" + contentHtml + "</todoo-checkbox>";
                htmlBuilder.append(checkboxTag);
            } else if (spanInfo.span instanceof android.text.style.BulletSpan) {
                int start = spanInfo.start;
                int end = spanInfo.end;

                // Extract the content with its formatting
                Spannable contentSpannable = new SpannableStringBuilder(builder.subSequence(start, end));

                // Convert the content to HTML, preserving all formatting
                String contentHtml = HtmlCompat.toHtml(contentSpannable, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                        .replaceAll("<p[^>]*>", "")
                        .replaceAll("</p>", "")
                        .trim();

                String bulletTag = "<todoo-bullet>" + contentHtml + "</todoo-bullet>";
                htmlBuilder.append(bulletTag);
            } else if (spanInfo.span instanceof RelativeSizeSpan) {
                RelativeSizeSpan sizeSpan = (RelativeSizeSpan) spanInfo.span;
                if (Math.abs(sizeSpan.getSizeChange() - 1.5f) < 0.1) {
                    Spannable contentSpannable = new SpannableStringBuilder(builder.subSequence(spanInfo.start, spanInfo.end));
                    String contentHtml = HtmlCompat.toHtml(contentSpannable, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                            .replaceAll("<p[^>]*>", "")
                            .replaceAll("</p>", "")
                            .trim();
                    boolean isBold = false;
                    StyleSpan[] styleSpans = builder.getSpans(spanInfo.start, spanInfo.end, StyleSpan.class);
                    for (StyleSpan styleSpan : styleSpans) {
                        if (styleSpan.getStyle() == Typeface.BOLD) {
                            isBold = true;
                            break;
                        }
                    }
                    String headingTag = "<todoo-heading" + (isBold ? " bold=\"true\"" : "") + ">" + contentHtml + "</todoo-heading>";
                    htmlBuilder.append(headingTag);
                }
            }

            currentPosition = spanInfo.end;
        }

        // Append remaining text
        if (currentPosition < builder.length()) {
            CharSequence sub = builder.subSequence(currentPosition, builder.length());
            String subHtml = HtmlCompat.toHtml(new SpannableString(sub), HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
            htmlBuilder.append(subHtml);
        }

        // Clean up extra paragraph tags from the entire HTML
        String html = htmlBuilder.toString()
                .replaceAll("<p[^>]*>", "") // Remove <p> tags
                .replaceAll("</p>", "") // Remove </p> tags
                .trim();
        return html;
    }

    public static Spannable fromHtml(Context context, String html) {
        if (html == null || html.isEmpty()) return new SpannableString("");

        TextFormattingManager textFormattingManager = new TextFormattingManager(context);
        Typeface fontAwesome = ResourcesCompat.getFont(context, R.font.fa_free_regular_400);
        int checkboxColor = ContextCompat.getColor(context, R.color.checkbox_selected);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        int lastEnd = 0;

        // Pattern to match custom tags (checkbox, bullet, heading)
        Pattern tagPattern = Pattern.compile("<todoo-(checkbox|bullet|heading)(?:\\s+[^>]*)?>(.*?)</todoo-\\1>", Pattern.DOTALL);
        Matcher matcher = tagPattern.matcher(html);

        while (matcher.find()) {
            // Append text before the tag
            if (lastEnd < matcher.start()) {
                String before = html.substring(lastEnd, matcher.start());
                Spanned spannedBefore = HtmlCompat.fromHtml(before, HtmlCompat.FROM_HTML_MODE_LEGACY);
                builder.append(spannedBefore);
            }

            String type = matcher.group(1);
            String content = matcher.group(2);

            if ("checkbox".equals(type)) {
                // Extract checked state
                Pattern checkedPattern = Pattern.compile("checked=\"(true|false)\"");
                Matcher checkedMatcher = checkedPattern.matcher(matcher.group(0));
                boolean isChecked = checkedMatcher.find() && "true".equals(checkedMatcher.group(1));

                // Parse the content as HTML to restore formatting
                Spanned contentSpanned = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY);

                // Insert placeholder space and content
                int start = builder.length();
                builder.append(" "); // Placeholder for checkbox
                builder.append(contentSpanned);
                int end = builder.length();

                // Apply checkbox spans
                CheckboxSpan checkboxSpan = new CheckboxSpan(textFormattingManager, fontAwesome, checkboxColor, isChecked);
                CheckboxClickSpan clickSpan = new CheckboxClickSpan(checkboxSpan);
                NonEditableSpan nonEditableSpan = new NonEditableSpan();
                builder.setSpan(checkboxSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(clickSpan, start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(nonEditableSpan, start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (isChecked) {
                    builder.setSpan(new StrikethroughSpan(), start + 1, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if ("bullet".equals(type)) {
                // Parse the content as HTML to restore formatting
                Spanned contentSpanned = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY);
                int start = builder.length();
                builder.append(contentSpanned);
                int end = builder.length();
                int gapWidth = (int) (8 * context.getResources().getDisplayMetrics().density);
                builder.setSpan(new android.text.style.BulletSpan(gapWidth), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if ("heading".equals(type)) {
                Pattern boldPattern = Pattern.compile("bold=\"(true|false)\"");
                Matcher boldMatcher = boldPattern.matcher(matcher.group(0));
                boolean isBold = boldMatcher.find() && "true".equals(boldMatcher.group(1));

                // Parse the content as HTML to restore formatting
                Spanned contentSpanned = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY);
                int start = builder.length();
                builder.append(contentSpanned);
                int end = builder.length();
                builder.setSpan(new RelativeSizeSpan(1.5f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (isBold) {
                    builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            lastEnd = matcher.end();
        }

        // Append remaining text
        if (lastEnd < html.length()) {
            String remaining = html.substring(lastEnd);
            Spanned spannedRemaining = HtmlCompat.fromHtml(remaining, HtmlCompat.FROM_HTML_MODE_LEGACY);
            builder.append(spannedRemaining);
        }

        return builder;
    }

    // Helper class for span information
    private static class SpanInfo {
        int start;
        int end;
        Object span;

        SpanInfo(int start, int end, Object span) {
            this.start = start;
            this.end = end;
            this.span = span;
        }
    }
}