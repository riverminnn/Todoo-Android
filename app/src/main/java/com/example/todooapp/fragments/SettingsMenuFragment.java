package com.example.todooapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.todooapp.R;
import com.example.todooapp.utils.OverlayUtils;
import com.example.todooapp.utils.TodoSortOption;
import com.example.todooapp.viewmodel.TodoViewModel;

public class SettingsMenuFragment extends Fragment {
    private TodoViewModel todoViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_menu_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        todoViewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);

        // Setup back button
        TextView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(requireView()).popBackStack());

        // Setup sort option click listener
        LinearLayout sortOption = view.findViewById(R.id.sortOption);
        TextView sortValueText = view.findViewById(R.id.sortValue);

        // Get and display current sort option
        TodoSortOption currentOption = todoViewModel.getCurrentSortOption().getValue();
        if (currentOption != null) {
            sortValueText.setText(currentOption.getDisplayName());
        }

        sortOption.setOnClickListener(v -> {
            // Show overlay using OverlayUtils
            OverlayUtils.showOverlay(requireContext(), v1 -> {
                // Hide overlay when clicked
                OverlayUtils.hideOverlay(requireContext());
                // Optionally dismiss the popup menu if needed
            });

            // Create popup menu with custom style
            Context wrapper = new ContextThemeWrapper(requireContext(), R.style.CustomDropdownTheme);
            PopupMenu popupMenu = new PopupMenu(wrapper, v, Gravity.END);
            popupMenu.inflate(R.menu.item_sort_dropdown);

            // Handle menu item clicks
            popupMenu.setOnMenuItemClickListener(item -> {
                TodoSortOption selectedOption;
                int itemId = item.getItemId();

                if (itemId == R.id.sort_modified_desc) {
                    selectedOption = TodoSortOption.MODIFIED_DESC;
                } else if (itemId == R.id.sort_modified_asc) {
                    selectedOption = TodoSortOption.MODIFIED_ASC;
                } else if (itemId == R.id.sort_created_desc) {
                    selectedOption = TodoSortOption.CREATED_DESC;
                } else if (itemId == R.id.sort_created_asc) {
                    selectedOption = TodoSortOption.CREATED_ASC;
                } else if (itemId == R.id.sort_alphabetical_asc) {
                    selectedOption = TodoSortOption.ALPHABETICAL_ASC;
                } else if (itemId == R.id.sort_alphabetical_desc) {
                    selectedOption = TodoSortOption.ALPHABETICAL_DESC;
                } else {
                    return false;
                }

                todoViewModel.setSortOption(selectedOption);
                sortValueText.setText(selectedOption.getDisplayName());
                // Hide overlay after selection
                OverlayUtils.hideOverlay(requireContext());
                return true;
            });

            // Hide overlay when popup menu is dismissed
            popupMenu.setOnDismissListener(menu -> OverlayUtils.hideOverlay(requireContext()));

            popupMenu.show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up the overlay when the fragment is destroyed
        OverlayUtils.hideOverlay(requireContext());
    }
}