package com.example.todooapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.Manifest;

import com.example.todooapp.utils.ReminderHelper;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private Bundle pendingNavigationArgs = null;
    private int pendingNavigationId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ReminderHelper.createNotificationChannel(this);

        // Set system bar colors programmatically (alternative approach)
        getWindow().setStatusBarColor(Color.parseColor("#f7f7f7"));
        getWindow().setNavigationBarColor(Color.parseColor("#f7f7f7"));

        // Ensure dark icons on light background
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Parse intent first, but delay navigation until navController is ready
        parseIntent(getIntent());

        // Handle any pending navigation now that navController is initialized
        if (pendingNavigationId != -1) {
            if (pendingNavigationArgs != null) {
                navController.navigate(pendingNavigationId, pendingNavigationArgs);
            } else {
                navController.navigate(pendingNavigationId);
            }
            pendingNavigationId = -1;
            pendingNavigationArgs = null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        parseIntent(intent);

        // For new intents, we can navigate immediately since navController is already initialized
        if (pendingNavigationId != -1) {
            if (pendingNavigationArgs != null) {
                navController.navigate(pendingNavigationId, pendingNavigationArgs);
            } else {
                navController.navigate(pendingNavigationId);
            }
            pendingNavigationId = -1;
            pendingNavigationArgs = null;
        }
    }

    private void parseIntent(Intent intent) {
        if (intent != null) {
            // Handle opening the form for adding a new todo
            if (intent.getBooleanExtra("open_form", false)) {
                pendingNavigationId = R.id.todoFormFragment;
            }

            // Handle opening a specific todo
            Bundle extras = intent.getExtras();
            // In MainActivity.parseIntent
            if (extras != null && extras.containsKey("todoId")) {
                String todoId = extras.getString("todoId");
                if (todoId != null) {
                    Log.d("MainActivity", "Received todoId: " + todoId);
                    // Add this line to see all extras
                    for (String key : extras.keySet()) {
                        Log.d("MainActivity", "Extra: " + key + " = " + extras.get(key));
                    }
                    pendingNavigationArgs = new Bundle();
                    pendingNavigationArgs.putString("todoId", todoId);
                    pendingNavigationId = R.id.todoFormFragment;
                }
            }
        }
    }
}