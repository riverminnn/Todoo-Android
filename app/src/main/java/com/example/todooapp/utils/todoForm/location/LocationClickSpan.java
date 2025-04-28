package com.example.todooapp.utils.todoForm.location;

import android.text.style.ClickableSpan;
import android.view.View;

public class LocationClickSpan extends ClickableSpan {
    private final LocationSpan locationSpan;

    public LocationClickSpan(LocationSpan locationSpan) {
        this.locationSpan = locationSpan;
    }

    @Override
    public void onClick(View widget) {
        locationSpan.onClick(widget);
    }
}