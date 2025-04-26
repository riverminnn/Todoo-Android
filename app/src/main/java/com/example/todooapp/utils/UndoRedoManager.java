package com.example.todooapp.utils;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class UndoRedoManager {
    // Classes for span storage and state management
    public static class SpanInfo {
        public final Object span;
        public final int start;
        public final int end;
        public final int flags;

        public SpanInfo(Object span, int start, int end, int flags) {
            this.span = span;
            this.start = start;
            this.end = end;
            this.flags = flags;
        }
    }

    public static class EditState {
        public final String text;
        public final int cursorPosition;
        public final List<SpanInfo> spans;

        public EditState(String text, int cursorPosition, List<SpanInfo> spans) {
            this.text = text;
            this.cursorPosition = Math.min(Math.max(0, cursorPosition), text.length());
            this.spans = spans;
        }
    }

    private EditText editText;
    private final Stack<EditState> undoStack = new Stack<>();
    private final Stack<EditState> redoStack = new Stack<>();
    private boolean isUndoOrRedoInProgress = false;
    private OnUndoRedoStateChangedListener listener;
    private String lastSavedText = "";
    private int lastSelectionStart = 0;
    private int lastSelectionEnd = 0;

    public interface OnUndoRedoStateChangedListener {
        void onUndoRedoStateChanged(boolean canUndo, boolean canRedo);
    }

    public UndoRedoManager(EditText editText) {
        this.editText = editText;
    }

    public void setOnUndoRedoStateChangedListener(OnUndoRedoStateChangedListener listener) {
        this.listener = listener;
        notifyStateChanged();
    }

    // New method to track formatting operations specifically
    public void saveFormatState() {
        String currentText = editText.getText().toString();
        int cursorPosition = editText.getSelectionStart();
        List<SpanInfo> spans = captureSpans(editText.getText());

        // Always save format operations as individual steps
        undoStack.push(new EditState(currentText, cursorPosition, spans));
        redoStack.clear();
        notifyStateChanged();

        // Update last saved state
        lastSavedText = currentText;
        lastSelectionStart = editText.getSelectionStart();
        lastSelectionEnd = editText.getSelectionEnd();
    }

    // In UndoRedoManager.java, modify the saveState method to preserve formatting
    public void saveState(String text, int cursorPosition) {
        // Get current spans from the EditText, not from the text parameter
        List<SpanInfo> spans = captureSpans(editText.getText());

        // Only save state if text content changed
        if (!text.equals(lastSavedText)) {
            undoStack.push(new EditState(text, cursorPosition, spans));
            redoStack.clear();
            notifyStateChanged();

            lastSavedText = text;
        }
    }

    public void undo() {
        if (undoStack.isEmpty()) return;

        isUndoOrRedoInProgress = true;

        // Save current state to redo stack
        String currentContent = editText.getText().toString();
        int currentCursorPos = editText.getSelectionStart();
        List<SpanInfo> currentSpans = captureSpans(editText.getText());
        redoStack.push(new EditState(currentContent, currentCursorPos, currentSpans));

        // Restore previous state
        EditState previousState = undoStack.pop();
        restoreState(previousState);

        isUndoOrRedoInProgress = false;
        notifyStateChanged();
    }

    public void redo() {
        if (redoStack.isEmpty()) return;

        isUndoOrRedoInProgress = true;

        // Save current state to undo stack
        String currentContent = editText.getText().toString();
        int currentCursorPos = editText.getSelectionStart();
        List<SpanInfo> currentSpans = captureSpans(editText.getText());
        undoStack.push(new EditState(currentContent, currentCursorPos, currentSpans));

        // Restore next state
        EditState nextState = redoStack.pop();
        restoreState(nextState);

        isUndoOrRedoInProgress = false;
        notifyStateChanged();
    }

    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        notifyStateChanged();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public boolean isUndoOrRedoInProgress() {
        return isUndoOrRedoInProgress;
    }

    private void restoreState(EditState state) {
        Editable editable = editText.getText();
        editable.clear(); // Clear current text and spans
        editable.append(state.text); // Set text

        // Reapply spans with stored position information
        for (SpanInfo spanInfo : state.spans) {
            editable.setSpan(spanInfo.span, spanInfo.start, spanInfo.end, spanInfo.flags);
        }

        // Restore cursor position
        editText.setSelection(state.cursorPosition);
    }

    private List<SpanInfo> captureSpans(Spannable spannable) {
        List<SpanInfo> result = new ArrayList<>();
        Object[] spans = spannable.getSpans(0, spannable.length(), Object.class);

        for (Object span : spans) {
            int start = spannable.getSpanStart(span);
            int end = spannable.getSpanEnd(span);
            int flags = spannable.getSpanFlags(span);

            // Skip invalid spans or system spans we don't want to restore
            if (start >= 0 && end > start &&
                    !(span instanceof android.text.style.SuggestionSpan)) {
                result.add(new SpanInfo(span, start, end, flags));
            }
        }
        return result;
    }

    private void notifyStateChanged() {
        if (listener != null) {
            listener.onUndoRedoStateChanged(canUndo(), canRedo());
        }
    }
}