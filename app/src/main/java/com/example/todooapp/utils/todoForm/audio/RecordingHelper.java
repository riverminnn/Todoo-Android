package com.example.todooapp.utils.todoForm.audio;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.text.Editable;
import android.text.Spannable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.todooapp.R;
import com.example.todooapp.utils.shared.TodooDialogBuilder;
import com.example.todooapp.utils.todoForm.MediaHelper;
import com.example.todooapp.utils.todoForm.content.UndoRedoManager;

import java.io.IOException;

public class RecordingHelper {
    public static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private final Fragment fragment;
    private final UndoRedoManager undoRedoManager;
    private final EditText contentEditText;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String recordingFilePath;
    private boolean isRecording = false;

    public RecordingHelper(Fragment fragment, UndoRedoManager undoRedoManager, EditText contentEditText) {
        this.fragment = fragment;
        this.undoRedoManager = undoRedoManager;
        this.contentEditText = contentEditText;
    }

    public void checkPermissionAndRecord() {
        if (ContextCompat.checkSelfPermission(fragment.requireContext(),
                android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            fragment.requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }

        // Show recording dialog
        showRecordingDialog();
    }

    public void showRecordingDialog() {
        // Create dialog with custom view
        TodooDialogBuilder builder = new TodooDialogBuilder(fragment.requireContext());
        View recordingView = fragment.getLayoutInflater().inflate(R.layout.dialog_recording, null);
        builder.setView(recordingView)
                .setTitle("Record Audio");

        // Get view references
        TextView tvRecordingStatus = recordingView.findViewById(R.id.tvRecordingStatus);
        TextView btnRecord = recordingView.findViewById(R.id.btnRecord);
        TextView btnPlay = recordingView.findViewById(R.id.btnPlay);
        TextView btnSave = recordingView.findViewById(R.id.btnSave);

        // Setup recording file path
        recordingFilePath = MediaHelper.getNewAudioFilePath(fragment.requireContext());

        // Create dialog
        final androidx.appcompat.app.AlertDialog dialog = builder.create();

        // Setup record button
        btnRecord.setOnClickListener(v -> {
            if (!isRecording) {
                startRecording();
                tvRecordingStatus.setText("Recording...");
                btnRecord.setText("Stop");
                btnPlay.setEnabled(false);
                btnSave.setEnabled(false);
            } else {
                stopRecording();
                tvRecordingStatus.setText("Recording complete");
                btnRecord.setText("Record");
                btnPlay.setEnabled(true);
                btnSave.setEnabled(true);
            }
            isRecording = !isRecording;
        });

        // Setup play button
        btnPlay.setEnabled(false);
        btnPlay.setOnClickListener(v -> {
            if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
                playRecording();
                tvRecordingStatus.setText("Playing...");
            } else {
                stopPlayback();
                tvRecordingStatus.setText("Playback stopped");
            }
        });

        // Setup save button
        btnSave.setEnabled(false);
        btnSave.setOnClickListener(v -> {
            insertAudioIntoContent();
            dialog.dismiss();
        });

        // Clean up on dismiss
        dialog.setOnDismissListener(d -> {
            cleanupMediaResources();
        });

        dialog.show();
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(recordingFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            Toast.makeText(fragment.requireContext(), "Failed to start recording", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                // Handle the case when recording is too short
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void playRecording() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(recordingFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> stopPlayback());
        } catch (IOException e) {
            Toast.makeText(fragment.requireContext(), "Failed to play recording", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    public void cleanupMediaResources() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        isRecording = false;
    }

    private void insertAudioIntoContent() {
        // Get relative path and save state for undo
        String relativePath = MediaHelper.getRelativeAudioPath(fragment.requireContext(), recordingFilePath);
        undoRedoManager.saveFormatState();

        // Insert audio reference at current position
        Editable editable = contentEditText.getText();
        int pos = Math.max(0, contentEditText.getSelectionStart());

        // Make sure there's a newline before if needed
        if (pos > 0 && editable.charAt(pos-1) != '\n') {
            editable.insert(pos++, "\n");
        }

        // Insert audio placeholder and spans
        String placeholder = "🔊 Audio Recording";
        editable.insert(pos, placeholder);

        AudioSpan audioSpan = new AudioSpan(fragment.requireContext(), relativePath);
        AudioClickSpan clickSpan = new AudioClickSpan(audioSpan);

        editable.setSpan(audioSpan, pos, pos + placeholder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        editable.setSpan(clickSpan, pos, pos + placeholder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Add newline after if needed
        if (pos + placeholder.length() < editable.length() &&
                editable.charAt(pos + placeholder.length()) != '\n') {
            editable.insert(pos + placeholder.length(), "\n");
        }

        Toast.makeText(fragment.requireContext(), "Audio recording added", Toast.LENGTH_SHORT).show();
    }
}