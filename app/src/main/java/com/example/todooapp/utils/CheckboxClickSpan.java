package com.example.todooapp.utils;

import android.text.Editable;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;

public class CheckboxClickSpan extends ClickableSpan {
    private final CheckboxSpan checkboxSpan;

    public CheckboxClickSpan(CheckboxSpan checkboxSpan) {
        this.checkboxSpan = checkboxSpan;
    }

    @Override
    public void onClick(@NonNull View widget) {
        if (widget instanceof EditText) {
            EditText editText = (EditText) widget;
            Editable editable = editText.getText();

            int start = editable.getSpanStart(this);
            int end = editable.getSpanEnd(this);

            if (start >= 0 && end > start) {
                checkboxSpan.toggle(editable, start, end);
                widget.invalidate();
            }
        }
    }
}