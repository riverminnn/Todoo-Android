package com.example.todooapp.utils.todoForm;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MediaHelper {
    private static final String TAG = "MediaHelper";
    private static final String IMAGE_DIRECTORY = "images";
    private static final String AUDIO_DIRECTORY = "audio";

    // Existing method for saving images
    public static String saveImageToLocal(Context context, Uri sourceUri) {
        try {
            // Create directory if it doesn't exist
            File directory = new File(context.getFilesDir(), IMAGE_DIRECTORY);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate unique file name
            String fileName = "img_" + UUID.randomUUID().toString() + ".jpg";
            File destFile = new File(directory, fileName);

            // Copy content from selected Uri to internal storage
            try (InputStream is = context.getContentResolver().openInputStream(sourceUri);
                 FileOutputStream os = new FileOutputStream(destFile)) {

                if (is == null) {
                    return null;
                }

                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
            }

            // Return relative path to be stored in database
            return IMAGE_DIRECTORY + "/" + fileName;

        } catch (IOException e) {
            Log.e(TAG, "Error saving image", e);
            return null;
        }
    }

    // Method to get a new file path for audio recording
    public static String getNewAudioFilePath(Context context) {
        // Create directory if it doesn't exist
        File directory = new File(context.getFilesDir(), AUDIO_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generate unique file name with timestamp for sorting
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String timeStamp = dateFormat.format(new Date());
        String fileName = "audio_" + timeStamp + "_" + UUID.randomUUID().toString().substring(0, 8) + ".3gp";

        // Return the absolute path for MediaRecorder
        return new File(directory, fileName).getAbsolutePath();
    }

    // Method to get relative audio path for storing in database
    public static String getRelativeAudioPath(Context context, String absolutePath) {
        File file = new File(absolutePath);
        if (!file.exists()) {
            return null;
        }

        // Convert absolute path to relative path
        return AUDIO_DIRECTORY + "/" + file.getName();
    }

    // Method to get full path from a relative path
    public static String getFullPathFromRelative(Context context, String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }

        return new File(context.getFilesDir(), relativePath).getAbsolutePath();
    }

    // Helper method to delete media file
    public static boolean deleteMediaFile(Context context, String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return false;
        }

        File file = new File(context.getFilesDir(), relativePath);
        return file.exists() && file.delete();
    }
}