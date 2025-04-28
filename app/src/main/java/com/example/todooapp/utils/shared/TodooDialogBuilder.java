package com.example.todooapp.utils.shared;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.example.todooapp.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class TodooDialogBuilder extends MaterialAlertDialogBuilder {

    private final Context context;
    private final int backgroundColor;
    private final int textColor;

    public TodooDialogBuilder(@NonNull Context context) {
        super(context);
        this.context = context;

        // Get colors from theme
        TypedArray ta = context.obtainStyledAttributes(new int[] {
                R.attr.cardBackgroundColor,
                R.attr.textColorPrimary
        });

        this.backgroundColor = ta.getColor(0, 0);
        this.textColor = ta.getColor(1, 0);
        ta.recycle();

        // Apply default styling with theme colors
        setBackgroundInsetStart(32);
        setBackgroundInsetEnd(32);
        setBackgroundInsetTop(20);
        setBackgroundInsetBottom(20);
    }

    @Override
    public TodooDialogBuilder setTitle(CharSequence title) {
        return (TodooDialogBuilder) super.setTitle(title);
    }

    @Override
    public TodooDialogBuilder setTitle(@StringRes int titleId) {
        return (TodooDialogBuilder) super.setTitle(titleId);
    }

    @Override
    public TodooDialogBuilder setMessage(CharSequence message) {
        return (TodooDialogBuilder) super.setMessage(message);
    }

    @Override
    public TodooDialogBuilder setMessage(@StringRes int messageId) {
        return (TodooDialogBuilder) super.setMessage(messageId);
    }

    @Override
    public TodooDialogBuilder setPositiveButton(CharSequence text,
                                                DialogInterface.OnClickListener listener) {
        return (TodooDialogBuilder) super.setPositiveButton(text, listener);
    }

    @Override
    public TodooDialogBuilder setNegativeButton(CharSequence text,
                                                DialogInterface.OnClickListener listener) {
        return (TodooDialogBuilder) super.setNegativeButton(text, listener);
    }

    @Override
    public TodooDialogBuilder setView(View view) {
        return (TodooDialogBuilder) super.setView(view);
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public TodooDialogBuilder withInputField(
            String title,
            String hint,
            String initialValue,
            InputConfirmListener onConfirm) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null);
        TextView textField = view.findViewById(R.id.dialogInput);
        textField.setHint(hint);

        if (initialValue != null) {
            textField.setText(initialValue);
        }

        setTitle(title);
        setView(view);
        setPositiveButton("OK", (dialog, which) -> {
            String value = textField.getText().toString().trim();
            onConfirm.onConfirm(value);
        });
        setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return this;
    }

    public interface InputConfirmListener {
        void onConfirm(String value);
    }

    @Override
    public AlertDialog show() {
        AlertDialog dialog = super.create();

        // Apply rounded corners with theme colors
        if (dialog.getWindow() != null) {
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setColor(backgroundColor);
            shape.setCornerRadius(12 * context.getResources().getDisplayMetrics().density); // 12dp
            dialog.getWindow().setBackgroundDrawable(shape);
        }

        dialog.show();

        // Set button text colors using theme color
        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(textColor);
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(textColor);
        }

        return dialog;
    }
}