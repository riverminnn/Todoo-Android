package com.example.todooapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.style.ReplacementSpan;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageSpan extends ReplacementSpan {
    private final Context context;
    private final String imagePath;
    private final int maxWidth;
    private Drawable drawable; // Removed final modifier
    private static final String TAG = "ImageSpan";

    public ImageSpan(Context context, String imagePath, int maxWidth) {
        this.context = context;
        this.imagePath = imagePath;
        this.maxWidth = maxWidth;
        loadDrawable(); // Load drawable during construction
    }

    private void loadDrawable() {
        Bitmap bitmap = loadBitmapFromPath();
        if (bitmap != null) {
            drawable = new BitmapDrawable(context.getResources(), bitmap);
            // Scale to respect maxWidth while maintaining aspect ratio
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();

            if (width > maxWidth) {
                float ratio = (float) maxWidth / width;
                width = maxWidth;
                height = (int) (height * ratio);
            }

            drawable.setBounds(0, 0, width, height);
        } else {
            // Use a fallback image if bitmap couldn't be loaded
            drawable = context.getDrawable(android.R.drawable.ic_menu_report_image);
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            }
        }
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        if (drawable == null) {
            return 0;
        }

        if (fm != null) {
            int drawableHeight = drawable.getBounds().height();
            int fontHeight = fm.bottom - fm.top;

            if (drawableHeight > fontHeight) {
                int paddingY = (drawableHeight - fontHeight) / 2;
                fm.ascent = fm.top - paddingY;
                fm.descent = fm.bottom + paddingY;
            }
        }

        return drawable.getBounds().width();
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        if (drawable != null) {
            canvas.save();
            // Center the image vertically in the line
            int drawableHeight = drawable.getBounds().height();
            int lineHeight = bottom - top;
            int transY = top + (lineHeight - drawableHeight) / 2;
            canvas.translate(x, transY);
            drawable.draw(canvas);
            canvas.restore();
        }
    }

    private Bitmap loadBitmapFromPath() {
        try {
            if (imagePath == null) return null;

            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                // Load from local file
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, options);

                // Calculate inSampleSize for memory-efficient loading
                int inSampleSize = 1;
                while (options.outWidth / inSampleSize > maxWidth * 2) {
                    inSampleSize *= 2;
                }

                options.inJustDecodeBounds = false;
                options.inSampleSize = inSampleSize;
                return BitmapFactory.decodeFile(imagePath, options);
            } else if (imagePath.startsWith("content:")) {
                // Load from content URI
                Uri uri = Uri.parse(imagePath);
                try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                    if (is != null) {
                        return BitmapFactory.decodeStream(is);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to load image: " + e.getMessage());
        }
        return null;
    }
}