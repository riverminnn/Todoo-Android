package com.example.todooapp.utils.todoForm;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;

import com.example.todooapp.R;
import com.example.todooapp.utils.todoForm.audio.AudioClickSpan;
import com.example.todooapp.utils.todoForm.audio.AudioSpan;
import com.example.todooapp.utils.todoForm.checkbox.CheckboxClickSpan;
import com.example.todooapp.utils.todoForm.checkbox.CheckboxSpan;
import com.example.todooapp.utils.todoForm.content.TextFormattingManager;
import com.example.todooapp.utils.todoForm.image.ImageSpan;
import com.example.todooapp.utils.todoForm.location.LocationSpan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlConverter {
    private static final String TAG = "HtmlConverter";
    private static final String IMAGE_TAG_PATTERN = "<img src=\"([^\"]+)\"\\s*/>";
    private static final String CHECKBOX_TAG_PATTERN = "<todoo-checkbox checked=\"(true|false)\">(.*?)</todoo-checkbox>";
    private static final String BULLET_TAG_PATTERN = "<todoo-bullet>(.*?)</todoo-bullet>";
    private static final String HEADING_TAG_PATTERN = "<todoo-heading>(.*?)</todoo-heading>";

    private static final String AUDIO_TAG_PATTERN = "<todoo-audio src=\"([^\"]+)\"\\s*/>";

    private static final String LOCATION_TAG_PATTERN = "<todoo-location lat=\"([\\d.-]+)\" lng=\"([\\d.-]+)\" name=\"([^\"]+)\"\\s*/>";

    // Helper class to track spans and their positions
    static class SpanInfo {
        int start;
        int end;
        Object span;

        SpanInfo(int start, int end, Object span) {
            this.start = start;
            this.end = end;
            this.span = span;
        }
    }

    public static String toHtml(Context context, Spannable spannable) {
        SpannableStringBuilder builder = new SpannableStringBuilder(spannable);
        StringBuilder htmlBuilder = new StringBuilder();

        // Get all spans
        Object[] allSpans = builder.getSpans(0, builder.length(), Object.class);
        List<SpanInfo> spanInfos = new ArrayList<>();

        // Track processed ranges to prevent duplicates
        boolean[] processed = new boolean[builder.length()];

        // First, process image spans separately (they take precedence)
        ImageSpan[] imageSpans = builder.getSpans(0, builder.length(), ImageSpan.class);
        for (ImageSpan span : imageSpans) {
            int start = builder.getSpanStart(span);
            int end = builder.getSpanEnd(span);
            spanInfos.add(new SpanInfo(start, end, span));

            // Mark range as processed
            for (int i = start; i < end && i < processed.length; i++) {
                processed[i] = true;
            }
        }

        // Process audio spans similarly (they also take precedence)
        AudioSpan[] audioSpans = builder.getSpans(0, builder.length(), AudioSpan.class);
        for (AudioSpan span : audioSpans) {
            int start = builder.getSpanStart(span);
            int end = builder.getSpanEnd(span);
            spanInfos.add(new SpanInfo(start, end, span));

            // Mark range as processed
            for (int i = start; i < end && i < processed.length; i++) {
                processed[i] = true;
            }
        }

        // Process location spans similarly
        LocationSpan[] locationSpans = builder.getSpans(0, builder.length(), LocationSpan.class);
        for (LocationSpan span : locationSpans) {
            int start = builder.getSpanStart(span);
            int end = builder.getSpanEnd(span);
            spanInfos.add(new SpanInfo(start, end, span));

            // Mark range as processed
            for (int i = start; i < end && i < processed.length; i++) {
                processed[i] = true;
            }
        }

        // Then add all other span types
        for (Object span : allSpans) {
            if (span instanceof CheckboxSpan ||
                    span instanceof android.text.style.BulletSpan ||
                    span instanceof RelativeSizeSpan) {
                int start = builder.getSpanStart(span);
                int end = builder.getSpanEnd(span);
                spanInfos.add(new SpanInfo(start, end, span));
            }
        }

        // Sort spans by start position
        spanInfos.sort(Comparator.comparingInt(s -> s.start));

        int currentPosition = 0;
        for (SpanInfo spanInfo : spanInfos) {
            if (currentPosition < spanInfo.start) {
                CharSequence sub = builder.subSequence(currentPosition, spanInfo.start);
                String subHtml = HtmlCompat.toHtml(new SpannableString(sub), HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
                htmlBuilder.append(subHtml);
            }

            if (spanInfo.span instanceof ImageSpan) {
                ImageSpan imageSpan = (ImageSpan) spanInfo.span;
                String imagePath = imageSpan.getImagePath();
                htmlBuilder.append("<img src=\"").append(imagePath).append("\"/>");
                currentPosition = spanInfo.end;
            } else if (spanInfo.span instanceof AudioSpan) {
                AudioSpan audioSpan = (AudioSpan) spanInfo.span;
                String audioPath = audioSpan.getAudioPath();
                htmlBuilder.append("<todoo-audio src=\"").append(audioPath).append("\"/>");
                currentPosition = spanInfo.end;
            } else if (spanInfo.span instanceof LocationSpan) {
                LocationSpan locationSpan = (LocationSpan) spanInfo.span;
                htmlBuilder.append("<todoo-location lat=\"")
                        .append(locationSpan.getLatitude())
                        .append("\" lng=\"")
                        .append(locationSpan.getLongitude())
                        .append("\" name=\"")
                        .append(locationSpan.getLocationName())
                        .append("\"/>");
                currentPosition = spanInfo.end;
            } else if (spanInfo.span instanceof CheckboxSpan) {
                CheckboxSpan checkboxSpan = (CheckboxSpan) spanInfo.span;
                boolean isChecked = checkboxSpan.isChecked();

                // Extract the checkbox text content (exclude the checkbox character)
                CharSequence content = builder.subSequence(spanInfo.start + 1, spanInfo.end);

                // Convert inner content to HTML to preserve nested formatting
                String contentHtml = HtmlCompat.toHtml(new SpannableString(content),
                                HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                        .replaceAll("<p[^>]*>", "")
                        .replaceAll("</p>", "");

                htmlBuilder.append("<todoo-checkbox checked=\"")
                        .append(isChecked)
                        .append("\">")
                        .append(contentHtml)
                        .append("</todoo-checkbox>");

                currentPosition = spanInfo.end;
            } else if (spanInfo.span instanceof android.text.style.BulletSpan) {
                // Handle bullet points
                CharSequence content = builder.subSequence(spanInfo.start, spanInfo.end);

                // Convert inner content to HTML
                String contentHtml = HtmlCompat.toHtml(new SpannableString(content),
                                HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                        .replaceAll("<p[^>]*>", "")
                        .replaceAll("</p>", "");

                htmlBuilder.append("<todoo-bullet>")
                        .append(contentHtml)
                        .append("</todoo-bullet>");

                currentPosition = spanInfo.end;
            } else if (spanInfo.span instanceof RelativeSizeSpan) {
                RelativeSizeSpan sizeSpan = (RelativeSizeSpan) spanInfo.span;

                // Check if this is a heading (1.5x size)
                if (Math.abs(sizeSpan.getSizeChange() - 1.5f) < 0.1) {
                    CharSequence content = builder.subSequence(spanInfo.start, spanInfo.end);

                    // Convert inner content to HTML
                    String contentHtml = HtmlCompat.toHtml(new SpannableString(content),
                                    HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                            .replaceAll("<p[^>]*>", "")
                            .replaceAll("</p>", "");

                    htmlBuilder.append("<todoo-heading>")
                            .append(contentHtml)
                            .append("</todoo-heading>");

                    currentPosition = spanInfo.end;
                }
            }
        }

        // Process any remaining text
        if (currentPosition < builder.length()) {
            CharSequence sub = builder.subSequence(currentPosition, builder.length());
            String subHtml = HtmlCompat.toHtml(new SpannableString(sub), HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
            htmlBuilder.append(subHtml);
        }

        String html = htmlBuilder.toString()
                .replaceAll("<p[^>]*>", "")
                .replaceAll("</p>", "")
                .trim();

        Log.d(TAG, "Converted to HTML: " + html);
        return html;
    }

    public static Spannable fromHtml(Context context, String html) {
        if (html == null || html.isEmpty()) {
            return new SpannableString("");
        }

        Log.d(TAG, "Converting from HTML: " + html);

        SpannableStringBuilder result = new SpannableStringBuilder();

        // First extract all image paths and map them to positions
        List<String> imagePaths = new ArrayList<>();
        List<String> audioPaths = new ArrayList<>();
        List<LocationInfo> locationInfos = new ArrayList<>();

        // Extract image paths from both formats (for compatibility)
        extractImagePaths(html, IMAGE_TAG_PATTERN, imagePaths);
        extractImagePaths(html, "<todoo-image src=\"([^\"]+)\"\\s*/>", imagePaths);

// Extract audio paths
        extractImagePaths(html, AUDIO_TAG_PATTERN, audioPaths);

        // Replace image tags with placeholders
        String processedHtml = html.replaceAll(IMAGE_TAG_PATTERN, "\uFFFC");
        processedHtml = processedHtml.replaceAll("<todoo-image src=\"[^\"]+\"\\s*/>", "\uFFFC");

        // Replace audio tags with placeholders (use a different character if needed, or same if okay)
        processedHtml = processedHtml.replaceAll(AUDIO_TAG_PATTERN, "🔊 Audio Recording");

        // Process custom tags
        int lastEnd = 0;
        TextFormattingManager textFormattingManager = new TextFormattingManager(context);
        Typeface fontAwesome = ResourcesCompat.getFont(context, R.font.fa_free_regular_400);
        int checkboxColor = ContextCompat.getColor(context, R.color.checkbox_selected);

        Pattern tagPattern = Pattern.compile("<todoo-(checkbox|bullet|heading)(?:\\s+[^>]*)?>(.*?)</todoo-\\1>", Pattern.DOTALL);
        Matcher matcher = tagPattern.matcher(processedHtml);

        Pattern locationPattern = Pattern.compile(LOCATION_TAG_PATTERN);
        Matcher locationMatcher = locationPattern.matcher(html);


        // Track image placeholders
        List<Integer> imagePlaceholderPositions = new ArrayList<>();
        List<Integer> audioPlaceholderPositions = new ArrayList<>();

        Pattern audioPattern = Pattern.compile("🔊 Audio Recording");
        Matcher audioMatcher = audioPattern.matcher(result);
        int audioIndex = 0;

        while (audioMatcher.find() && audioIndex < audioPaths.size()) {
            int start = audioMatcher.start();
            int end = audioMatcher.end();

            String audioPath = audioPaths.get(audioIndex++);
            AudioSpan audioSpan = new AudioSpan(context, audioPath);
            AudioClickSpan clickSpan = new AudioClickSpan(audioSpan);

            result.setSpan(audioSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            result.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        while (locationMatcher.find()) {
            double lat = Double.parseDouble(locationMatcher.group(1));
            double lng = Double.parseDouble(locationMatcher.group(2));
            String name = locationMatcher.group(3);
            locationInfos.add(new LocationInfo(lat, lng, name));
        }

        processedHtml = processedHtml.replaceAll(LOCATION_TAG_PATTERN, "📍 Location: $3");

        while (matcher.find()) {
            // Append text before the tag
            if (lastEnd < matcher.start()) {
                String before = processedHtml.substring(lastEnd, matcher.start());
                Spanned spannedBefore = HtmlCompat.fromHtml(before, HtmlCompat.FROM_HTML_MODE_COMPACT);

                // Count placeholders in this segment
                countAndTrackPlaceholders(spannedBefore.toString(), result.length(), imagePlaceholderPositions);

                result.append(spannedBefore);
            }

            String type = matcher.group(1);
            String content = matcher.group(2);

            if ("checkbox".equals(type)) {
                // Checkbox processing
                Pattern checkedPattern = Pattern.compile("checked=\"(true|false)\"");
                Matcher checkedMatcher = checkedPattern.matcher(matcher.group(0));
                boolean isChecked = checkedMatcher.find() && "true".equals(checkedMatcher.group(1));

                Spanned contentSpanned = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_COMPACT);

                int start = result.length();
                result.append(" "); // Space for checkbox
                result.append(contentSpanned);
                int end = result.length();

                CheckboxSpan checkboxSpan = new CheckboxSpan(textFormattingManager, fontAwesome, checkboxColor, isChecked);
                CheckboxClickSpan clickSpan = new CheckboxClickSpan(checkboxSpan);
                NonEditableSpan nonEditableSpan = new NonEditableSpan();

                result.setSpan(checkboxSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                result.setSpan(clickSpan, start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                result.setSpan(nonEditableSpan, start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                if (isChecked) {
                    result.setSpan(new StrikethroughSpan(), start + 1, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if ("bullet".equals(type)) {
                // Bullet processing
                Spanned contentSpanned = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_COMPACT);
                int start = result.length();
                result.append(contentSpanned);
                int end = result.length();

                int gapWidth = (int) (8 * context.getResources().getDisplayMetrics().density);
                result.setSpan(new android.text.style.BulletSpan(gapWidth), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if ("heading".equals(type)) {
                // Heading processing
                Spanned contentSpanned = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_COMPACT);
                int start = result.length();
                result.append(contentSpanned);
                int end = result.length();

                result.setSpan(new RelativeSizeSpan(1.5f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                result.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            lastEnd = matcher.end();
        }

        // Append remaining text
        if (lastEnd < processedHtml.length()) {
            String remaining = processedHtml.substring(lastEnd);
            Spanned spannedRemaining = HtmlCompat.fromHtml(remaining, HtmlCompat.FROM_HTML_MODE_COMPACT);

            // Count remaining placeholders
            countAndTrackPlaceholders(spannedRemaining.toString(), result.length(), imagePlaceholderPositions);

            result.append(spannedRemaining);
        }

        // Apply image spans
        int maxWidth = (int)(context.getResources().getDisplayMetrics().widthPixels * 0.8);
        for (int i = 0; i < Math.min(imagePlaceholderPositions.size(), imagePaths.size()); i++) {
            int position = imagePlaceholderPositions.get(i);
            String path = imagePaths.get(i);
            ImageSpan imageSpan = new ImageSpan(context, path, maxWidth);
            result.setSpan(imageSpan, position, position + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return result;
    }

    // Helper method to extract image paths from HTML
    private static void extractImagePaths(String html, String patternStr, List<String> paths) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            paths.add(matcher.group(1));
        }
    }

    // Helper method to count and track placeholder positions
    private static void countAndTrackPlaceholders(String content, int offset, List<Integer> positions) {
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\uFFFC') {
                positions.add(offset + i);
            }
        }
    }

    // Helper method to process image tags
    private static String processImageTags(String html, Pattern imagePattern, SpannableStringBuilder builder) {
        Matcher imageMatcher = imagePattern.matcher(html);
        StringBuffer sb = new StringBuffer();

        while (imageMatcher.find()) {
            String imagePath = imageMatcher.group(1);
            // Just replace with a placeholder - we'll process actual image positions later
            imageMatcher.appendReplacement(sb, "\uFFFC");
        }
        imageMatcher.appendTail(sb);
        return sb.toString();
    }

    // Helper method to track image placeholder positions
    private static void trackImagePlaceholders(SpannableStringBuilder result,
                                               Spanned content,
                                               List<ImageInfo> imageInfos) {
        // Find image placeholder chars in the content
        String contentStr = content.toString();
        int startPos = result.length();

        // Find all image placeholders in this content
        for (int i = 0; i < contentStr.length(); i++) {
            if (contentStr.charAt(i) == '\uFFFC') {
                // This is an image placeholder character
                Pattern imagePattern = Pattern.compile(IMAGE_TAG_PATTERN);
                Matcher matcher = imagePattern.matcher(contentStr);

                // Find corresponding image path
                if (matcher.find(i)) {
                    String imagePath = matcher.group(1);
                    int spanStart = startPos + i;
                    int spanEnd = spanStart + 1;

                    // Store image info for later span application
                    imageInfos.add(new ImageInfo(spanStart, spanEnd, imagePath));
                }
            }
        }

        // Append content to result
        result.append(content);
    }

    // Helper class to store image information
    private static class ImageInfo {
        final int start;
        final int end;
        final String path;

        ImageInfo(int start, int end, String path) {
            this.start = start;
            this.end = end;
            this.path = path;
        }
    }

    private static class LocationInfo {
        final double lat;
        final double lng;
        final String name;

        LocationInfo(double lat, double lng, String name) {
            this.lat = lat;
            this.lng = lng;
            this.name = name;
        }
    }

    private static void appendHtmlAsSpannable(Context context, SpannableStringBuilder builder, String html) {
        Spanned spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT);
        builder.append(spanned);
    }

    private interface CustomTagProcessor {
        void processTag(CharSequence content, int start, int end, String attributes);
    }

    private static void processCustomTags(Context context, SpannableStringBuilder builder,
                                          String tagPattern, CustomTagProcessor processor) {
        Pattern pattern = Pattern.compile(tagPattern);
        Matcher matcher = pattern.matcher(builder.toString());

        // Process tags from end to start to avoid position shifts
        List<int[]> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(new int[]{matcher.start(), matcher.end(),
                    matcher.groupCount() >= 1 ? 1 : -1,
                    matcher.groupCount() >= 2 ? 2 : -1});
        }

        for (int i = matches.size() - 1; i >= 0; i--) {
            int[] match = matches.get(i);
            int start = match[0];
            int end = match[1];
            int attrGroupIndex = match[2];
            int contentGroupIndex = match[3];

            matcher.find(start);
            String attributes = attrGroupIndex != -1 ? matcher.group(attrGroupIndex) : "";
            CharSequence content = contentGroupIndex != -1 ?
                    HtmlCompat.fromHtml(matcher.group(contentGroupIndex),
                            HtmlCompat.FROM_HTML_MODE_COMPACT) :
                    new SpannableString("");

            processor.processTag(content, start, end, attributes);
        }
    }
}