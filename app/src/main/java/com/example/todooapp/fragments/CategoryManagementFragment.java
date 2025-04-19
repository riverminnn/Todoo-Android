package com.example.todooapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooapp.R;
import com.example.todooapp.adapter.CategoryAdapter;
import com.example.todooapp.viewmodel.TodoViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryManagementFragment extends Fragment implements CategoryAdapter.CategoryClickListener {
    private RecyclerView rvCategories;
    private EditText etNewCategory;
    private Button btnAddCategory;
    private List<String> categories = new ArrayList<>();
    private CategoryAdapter categoryAdapter;
    private TodoViewModel todoViewModel;
    private long[] selectedTodoIds;

    private BottomNavigationView bottomActionBar;
    private Set<String> selectedCategories = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        rvCategories = view.findViewById(R.id.rvCategories);
        etNewCategory = view.findViewById(R.id.etNewCategory);
        btnAddCategory = view.findViewById(R.id.btnAddCategory);
        bottomActionBar = view.findViewById(R.id.bottomActionBar);
        bottomActionBar.setVisibility(View.GONE);

        // Get selected todo IDs (may be null when opened directly)
        if (getArguments() != null && getArguments().containsKey("selectedTodoIds")) {
            selectedTodoIds = getArguments().getLongArray("selectedTodoIds");
        }

        // Setup ViewModel
        todoViewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);

        // Setup CategoryAdapter
        categoryAdapter = new CategoryAdapter(categories, this);
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategories.setAdapter(categoryAdapter);

        // Load categories
        todoViewModel.getAllUniqueCategories().observe(getViewLifecycleOwner(), cats -> {
            categories.clear();
            if (cats != null) {
                categories.addAll(cats);
            }
            categoryAdapter.notifyDataSetChanged();
        });

        btnAddCategory.setOnClickListener(v -> {
            String category = etNewCategory.getText().toString().trim();

            // Allow empty category names by setting a default name
            if (category.isEmpty()) {
                category = "Unnamed Category";
            }

            // Only add if not already in the list
            if (!categories.contains(category)) {
                final String finalCategory = category;

                // Save the category even if no todos are assigned yet
                todoViewModel.saveCategory(finalCategory);

                // Add the category to the local list and update the UI immediately
                categories.add(finalCategory);
                categoryAdapter.notifyDataSetChanged();

                // If todos are selected, assign them to this category
                if (selectedTodoIds != null && selectedTodoIds.length > 0) {
                    // ...existing code...
                } else {
                    // Just notify that category was added
                    Toast.makeText(requireContext(),
                            "Category '" + finalCategory + "' created",
                            Toast.LENGTH_SHORT).show();
                    etNewCategory.setText("");
                }
            } else {
                Toast.makeText(requireContext(), "Category already exists", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up bottom action bar
        bottomActionBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit_category) {
                // Only allow editing if exactly one category is selected
                if (selectedCategories.size() == 1) {
                    showEditCategoryDialog(selectedCategories.iterator().next());
                } else {
                    Toast.makeText(requireContext(),
                            "Select exactly one category to edit",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (itemId == R.id.action_delete_category) {
                deleteSelectedCategories();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onCategoryClick(String category) {
        assignCategory(category);
    }

    @Override
    public void onSelectionModeChanged(boolean active, Set<String> selectedItems) {
        if (active) {
            bottomActionBar.setVisibility(View.VISIBLE);
            selectedCategories = selectedItems;

            // Show/hide edit option based on selection count
            Menu menu = bottomActionBar.getMenu();
            MenuItem editItem = menu.findItem(R.id.action_edit_category);
            editItem.setVisible(selectedCategories.size() == 1);
        } else {
            bottomActionBar.setVisibility(View.GONE);
            selectedCategories.clear();
        }
    }

    private void showEditCategoryDialog(String category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Category");

        // Set up the input
        EditText input = new EditText(requireContext());
        input.setText(category);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty() && !categories.contains(newName)) {
                todoViewModel.updateCategory(category, newName);
                exitSelectionMode();
            } else if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Category name cannot be empty",
                        Toast.LENGTH_SHORT).show();
            } else if (categories.contains(newName)) {
                Toast.makeText(requireContext(), "Category already exists",
                        Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void deleteSelectedCategories() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Categories");
        builder.setMessage("Are you sure you want to delete " +
                selectedCategories.size() + " categories?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            for (String category : selectedCategories) {
                todoViewModel.deleteCategory(category);
            }
            Toast.makeText(requireContext(),
                    selectedCategories.size() + " categories deleted",
                    Toast.LENGTH_SHORT).show();
            exitSelectionMode();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void exitSelectionMode() {
        categoryAdapter.setSelectionMode(false);
        bottomActionBar.setVisibility(View.GONE);
        selectedCategories.clear();
    }

    // Updated method to handle empty selectedTodoIds
    private void assignCategory(String category) {
        if (selectedTodoIds != null && selectedTodoIds.length > 0) {
            for (long id : selectedTodoIds) {
                todoViewModel.updateTodoCategory(id, category);
            }
            Toast.makeText(requireContext(),
                    "Moved " + selectedTodoIds.length + " todos to " + category,
                    Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).popBackStack();
        } else {
            // No todos selected, just inform user this category is selected
            Toast.makeText(requireContext(),
                    "Category '" + category + "' selected",
                    Toast.LENGTH_SHORT).show();
        }
    }
}