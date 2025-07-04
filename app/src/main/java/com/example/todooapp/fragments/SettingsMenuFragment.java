package com.example.todooapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.todooapp.R;
import com.example.todooapp.utils.settings.SettingsManager;
import com.example.todooapp.utils.settings.ThemeOption;
import com.example.todooapp.utils.settings.TodoSortOption;
import com.example.todooapp.utils.shared.TodooDialogBuilder;
import com.example.todooapp.utils.shared.UserManager;
import com.example.todooapp.viewmodel.TodoViewModel;

public class SettingsMenuFragment extends Fragment {

    private TextView btnBack, fontSizeValue, sortValue, layoutValue;
    private View fontSizeOption, sortOption, layoutOption;
    private View btnSignOut;
    private TextView tvVersionInfo, cloudStatus;

    private SettingsManager settingsManager;
    private UserManager userManager;
    private TodoViewModel todoViewModel;

    // Add to class fields
    private View themeOption;
    private TextView themeValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize managers and viewmodel
        settingsManager = new SettingsManager(requireContext());
        userManager = new UserManager(requireContext());
        todoViewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);

        // Find views
        btnBack = view.findViewById(R.id.btnBack);
        fontSizeOption = view.findViewById(R.id.fontSizeOption);
        sortOption = view.findViewById(R.id.sortOption);
        layoutOption = view.findViewById(R.id.layoutOption);
        fontSizeValue = view.findViewById(R.id.fontSizeValue);
        sortValue = view.findViewById(R.id.sortValue);
        layoutValue = view.findViewById(R.id.layoutValue);
        btnSignOut = view.findViewById(R.id.btnSignOut);
        tvVersionInfo = view.findViewById(R.id.tvVersionInfo);
        cloudStatus = view.findViewById(R.id.cloudStatus);

        // Set click listeners
        btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        fontSizeOption.setOnClickListener(v -> showFontSizeDialog());
        sortOption.setOnClickListener(v -> showSortOptionDialog());
        layoutOption.setOnClickListener(v -> showLayoutDialog());
        btnSignOut.setOnClickListener(v -> handleSignOut());

        // In onViewCreated method, add:
        themeOption = view.findViewById(R.id.themeOption);
        themeValue = view.findViewById(R.id.themeValue);
        themeOption.setOnClickListener(v -> showThemeDialog());

        // Load saved settings
        loadSettings();

        // Update cloud status
        updateCloudStatus();
    }
    private void loadSettings() {
        // Display saved settings
        fontSizeValue.setText(settingsManager.getFontSize());
        layoutValue.setText(settingsManager.getLayoutType());
        sortValue.setText(settingsManager.getSortOption().getDisplayName());

        // Add this line to load theme
        themeValue.setText(settingsManager.getThemeOption().getDisplayName());
    }

    // Add this method:
    private void showThemeDialog() {
        // Theme options
        final ThemeOption[] options = ThemeOption.values();
        String[] displayNames = new String[options.length];

        // Get current theme
        ThemeOption currentOption = settingsManager.getThemeOption();
        int selectedIndex = 0;

        // Prepare display names and find selected
        for (int i = 0; i < options.length; i++) {
            displayNames[i] = options[i].getDisplayName();
            if (options[i] == currentOption) {
                selectedIndex = i;
            }
        }

        // Create dialog
        new TodooDialogBuilder(requireContext())
                .setTitle("Theme")
                .setSingleChoiceItems(displayNames, selectedIndex, (dialog, which) -> {
                    // Save selected theme
                    settingsManager.setThemeOption(options[which]);

                    // Update UI
                    themeValue.setText(options[which].getDisplayName());

                    // Apply theme
                    settingsManager.applyTheme();

                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showFontSizeDialog() {
        // Font size options
        final String[] fontSizeOptions = {"Small", "Medium", "Large"};
        String currentFontSize = settingsManager.getFontSize();

        // Find current selection index
        int selectedIndex = 1; // Default to Medium
        for (int i = 0; i < fontSizeOptions.length; i++) {
            if (fontSizeOptions[i].equals(currentFontSize)) {
                selectedIndex = i;
                break;
            }
        }

        // Create dialog
        new TodooDialogBuilder(requireContext())
                .setTitle("Font Size")
                .setSingleChoiceItems(fontSizeOptions, selectedIndex, (dialog, which) -> {
                    // Save selected font size
                    settingsManager.setFontSize(fontSizeOptions[which]);

                    // Update UI
                    fontSizeValue.setText(fontSizeOptions[which]);

                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSortOptionDialog() {
        // Get all available sort options
        TodoSortOption[] options = TodoSortOption.values();
        String[] displayNames = new String[options.length];

        // Get current sort option from settings
        TodoSortOption currentOption = todoViewModel.getCurrentSortOption().getValue();
        int selectedIndex = 0;

        // Prepare the display names and find the selected index
        for (int i = 0; i < options.length; i++) {
            displayNames[i] = options[i].getDisplayName();
            if (options[i] == currentOption) {
                selectedIndex = i;
            }
        }

        // Create and show the dialog
        new TodooDialogBuilder(requireContext())
                .setTitle("Sort Notes By")
                .setSingleChoiceItems(displayNames, selectedIndex, (dialog, which) -> {
                    // Update view model with new sort option
                    todoViewModel.setSortOption(options[which]);

                    // Update the displayed value
                    sortValue.setText(options[which].getDisplayName());

                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLayoutDialog() {
        final String[] layoutOptions = {"List view", "Grid view"};
        String currentLayout = settingsManager.getLayoutType();
        int selectedIndex = 0;
        for (int i = 0; i < layoutOptions.length; i++) {
            if (layoutOptions[i].equals(currentLayout)) {
                selectedIndex = i;
                break;
            }
        }

        new TodooDialogBuilder(requireContext())
                .setTitle("Layout")
                .setSingleChoiceItems(layoutOptions, selectedIndex, (dialog, which) -> {
                    settingsManager.setLayoutType(layoutOptions[which]);
                    layoutValue.setText(layoutOptions[which]);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show(); // This applies the rounded corners and shows the dialog
    }

    private void updateCloudStatus() {
        if (userManager.isLoggedIn()) {
            cloudStatus.setText("Connected");
            cloudStatus.setTextColor(getResources().getColor(R.color.green_500, null));
        } else {
            cloudStatus.setText("Not connected");
            cloudStatus.setTextColor(getResources().getColor(R.color.red_500, null));
        }
    }

    private void handleSignOut() {
        new TodooDialogBuilder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> {
                    userManager.clearUserSession();
                    updateCloudStatus();
                    // Navigate to login screen if needed
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private <T> int findIndex(T[] array, T value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return 0; // Default to first option if not found
    }
}