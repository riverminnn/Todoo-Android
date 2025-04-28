package com.example.todooapp.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.todooapp.R;

import java.io.File;
import java.io.IOException;

public class AudioSpan extends ReplacementSpan {
    private static final String TAG = "AudioSpan";
    private final Context context;
    private final String audioPath;
    private MediaPlayer mediaPlayer;
    private final int iconSize;
    private final Drawable playIcon;
    private boolean isPlaying = false;
    private OnPlaybackCompleteListener playbackCompleteListener;

    public AudioSpan(Context context, String audioPath) {
        this.context = context;
        this.audioPath = audioPath;
        this.iconSize = (int) (24 * context.getResources().getDisplayMetrics().density);
        this.playIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_media_play);

        if (this.playIcon != null) {
            this.playIcon.setBounds(0, 0, iconSize, iconSize);
        }
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end,
                       @Nullable Paint.FontMetricsInt fm) {
        // Return the width needed for the audio icon + text
        return (int) (paint.measureText(text, start, end) + iconSize + 8);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top,
                     int y, int bottom, @NonNull Paint paint) {
        // Draw the audio icon
        canvas.save();
        int iconY = y - iconSize + (bottom - top) / 4;

        if (playIcon != null) {
            playIcon.setBounds((int) x, iconY, (int) x + iconSize, iconY + iconSize);
            playIcon.draw(canvas);
        }

        // Draw the text after the icon
        canvas.drawText(text, start, end, x + iconSize + 8, y, paint);
        canvas.restore();
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void onClick(View v) {
        if (isPlaying) {
            stopPlayback();
        } else {
            startPlayback();
        }
    }

    private void startPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        try {
            File audioFile = new File(context.getFilesDir(), audioPath);
            if (!audioFile.exists()) {
                Log.e(TAG, "Audio file not found: " + audioFile.getAbsolutePath());
                return;
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                if (playbackCompleteListener != null) {
                    playbackCompleteListener.onPlaybackComplete();
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error playing audio", e);
            isPlaying = false;
        }
    }

    public void stopPlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            isPlaying = false;
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void setOnPlaybackCompleteListener(OnPlaybackCompleteListener listener) {
        this.playbackCompleteListener = listener;
    }

    public interface OnPlaybackCompleteListener {
        void onPlaybackComplete();
    }
}