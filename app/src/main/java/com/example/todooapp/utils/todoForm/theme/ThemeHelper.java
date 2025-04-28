package com.example.todooapp.utils.todoForm.theme;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.example.todooapp.R;
import com.example.todooapp.utils.shared.TodooDialogBuilder;
import com.example.todooapp.viewmodel.TodoViewModel;

import java.util.List;

public class ThemeHelper {
    private int currentTheme = ThemeManager.THEME_DEFAULT;
    private final Fragment fragment;
    private final TodoViewModel todoViewModel;

    public ThemeHelper(Fragment fragment, TodoViewModel todoViewModel) {
        this.fragment = fragment;
        this.todoViewModel = todoViewModel;
    }

    public void setCurrentTheme(int themeIndex) {
        this.currentTheme = themeIndex;
    }

    public int getCurrentTheme() {
        return currentTheme;
    }

    public void showBackgroundOptions(String todoId) {
        TodooDialogBuilder builder = new TodooDialogBuilder(fragment.requireContext());
        View themePickerView = fragment.getLayoutInflater().inflate(R.layout.dialog_theme_picker, null);

        // Set title and view
        builder.setTitle("Choose Background")
                .setView(themePickerView);

        // Set up theme options
        List<ThemeManager.ThemeOption> themes = ThemeManager.getThemes();
        LinearLayout themeContainer = themePickerView.findViewById(R.id.themeContainer);
        themeContainer.removeAllViews();

        View[] themeViews = new View[themes.size()];

        // Get theme colors to apply to elements if needed
        int backgroundColor = builder.getBackgroundColor();
        int textColor = builder.getTextColor();

        // Apply theme attributes to the container if needed
        themeContainer.setBackgroundColor(backgroundColor);

        for (int i = 0; i < themes.size(); i++) {
            ThemeManager.ThemeOption theme = themes.get(i);
            View themeOption = fragment.getLayoutInflater().inflate(R.layout.item_theme_option_larger, themeContainer, false);
            TextView tvThemeName = themeOption.findViewById(R.id.tvThemeName);
            View colorPreview = themeOption.findViewById(R.id.colorPreview);
            themeViews[i] = themeOption;

            tvThemeName.setText(theme.name);
            tvThemeName.setTextColor(textColor); // Apply text color from theme
            colorPreview.setBackgroundColor(ContextCompat.getColor(fragment.requireContext(), theme.backgroundColor));

            if (i == currentTheme) {
                themeOption.setBackground(ContextCompat.getDrawable(fragment.requireContext(), R.drawable.theme_option_selected));
            }

            final int themeIndex = i;
            themeOption.setOnClickListener(v -> {
                // Update visual selection indicator
                for (int j = 0; j < themeViews.length; j++) {
                    themeViews[j].setBackground(j == themeIndex ?
                            ContextCompat.getDrawable(fragment.requireContext(), R.drawable.theme_option_selected) :
                            null);
                }

                // Update current theme index
                currentTheme = themeIndex;
                forceApplyTheme();
                saveTodoWithTheme(todoId);
            });

            themeContainer.addView(themeOption);
        }
        builder.show();
    }

    public void forceApplyTheme() {
        View rootView = fragment.requireView();

        // Use the aggressive theme application method
        ThemeManager.forceCompleteThemeApplication(fragment.requireActivity(), rootView, currentTheme);

        // Force another layout pass after a delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (fragment.isAdded() && !fragment.isDetached()) {
                rootView.invalidate();
                rootView.requestLayout();
            }
        }, 50);
    }

    public void applyCurrentTheme() {
        forceApplyTheme();
    }

    public void saveTodoWithTheme(String todoId) {
        if (todoId != null) {
            try {
                long id = Long.parseLong(todoId);
                todoViewModel.getTodoById(id).observe((LifecycleOwner)fragment, todo -> {
                    if (todo != null) {
                        todo.setThemeIndex(currentTheme);
                        todoViewModel.update(todo);
                    }
                });
            } catch (NumberFormatException e) {
                // Handle error silently
            }
        }
    }
}