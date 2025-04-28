package com.example.todooapp.utils.todoForm.location;

import android.content.pm.PackageManager;
import android.location.Location;
import android.text.Editable;
import android.text.Spannable;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.todooapp.utils.todoForm.content.UndoRedoManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {
    public static final int REQUEST_LOCATION_PERMISSION = 300;

    private final Fragment fragment;
    private final UndoRedoManager undoRedoManager;
    private final EditText contentEditText;

    public LocationHelper(Fragment fragment, UndoRedoManager undoRedoManager, EditText contentEditText) {
        this.fragment = fragment;
        this.undoRedoManager = undoRedoManager;
        this.contentEditText = contentEditText;
    }

    public void checkPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(fragment.requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            fragment.requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        getCurrentLocationAndInsert();
    }

    public void getCurrentLocationAndInsert() {
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(fragment.requireActivity());

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(fragment.requireActivity(), location -> {
                        if (location != null) {
                            insertLocationIntoContent(location);
                        } else {
                            Toast.makeText(fragment.requireContext(),
                                    "Unable to get current location",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(fragment.requireContext(),
                                "Error getting location: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException e) {
            Toast.makeText(fragment.requireContext(),
                    "Location permission denied",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void insertLocationIntoContent(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Get a readable location name
        String locationName = "My Location";

        // Save state for undo
        undoRedoManager.saveFormatState();

        // Insert location reference at current position
        Editable editable = contentEditText.getText();
        int pos = Math.max(0, contentEditText.getSelectionStart());

        // Make sure there's a newline before if needed
        if (pos > 0 && editable.charAt(pos-1) != '\n') {
            editable.insert(pos++, "\n");
        }

        // Insert location placeholder and spans
        String placeholder = "📍 Location: " + locationName;
        editable.insert(pos, placeholder);

        LocationSpan locationSpan = new LocationSpan(fragment.requireContext(), latitude, longitude, locationName);
        LocationClickSpan clickSpan = new LocationClickSpan(locationSpan);

        editable.setSpan(locationSpan, pos, pos + placeholder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        editable.setSpan(clickSpan, pos, pos + placeholder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Add newline after if needed
        if (pos + placeholder.length() < editable.length() &&
                editable.charAt(pos + placeholder.length()) != '\n') {
            editable.insert(pos + placeholder.length(), "\n");
        }

        Toast.makeText(fragment.requireContext(), "Location added", Toast.LENGTH_SHORT).show();
    }
}