package com.example.todooapp.utils;

import android.text.style.ClickableSpan;
import android.view.View;

public class AudioClickSpan extends ClickableSpan {
    private final AudioSpan audioSpan;

    public AudioClickSpan(AudioSpan audioSpan) {
        this.audioSpan = audioSpan;
    }

    @Override
    public void onClick(View widget) {
        audioSpan.onClick(widget);
    }
}