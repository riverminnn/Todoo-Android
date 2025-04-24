package com.example.todooapp.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooapp.R;
import com.example.todooapp.adapter.TodoAdapter;
import com.example.todooapp.data.model.Todo;
import com.example.todooapp.utils.UserManager;
import com.example.todooapp.viewmodel.TodoViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TodoListFragment extends Fragment implements TodoAdapter.OnTodoClickListener {
    private RecyclerView recyclerView;

    private boolean allTodosSelected = false;
    private SearchView searchView;
    private FloatingActionButton btnAdd;
    private TextView btnCategoryManager;
    private View bottomActionBar;
    private ChipGroup categoryChipGroup;

    private TodoViewModel todoViewModel;
    private List<Todo> todoList = new ArrayList<>();
    private TodoAdapter adapter;
    private String currentCategory = null; // null means "All"

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add to onViewCreated in TodoListFragment
        UserManager userManager = new UserManager(requireContext());
        bottomActionBar = view.findViewById(R.id.bottomActionBar);
        TextView moveButton = view.findViewById(R.id.action_move);
        TextView deleteButton = view.findViewById(R.id.action_delete);

        // Get references to UI elements
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnCategoryManager = view.findViewById(R.id.btnCategoryManager);
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup);

        // Initialize ViewModel - must be initialized before setupCategoryFilter
        todoViewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);

        // Setup RecyclerView
        adapter = new TodoAdapter(todoList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize category filter after ViewModel
        setupCategoryFilter();

        // Load all todos
        loadAllTodos();

        // Search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTodos(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchTodos(newText);
                return true;
            }
        });

        // Add button click
        btnAdd.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_todoListFragment_to_todoFormFragment);
        });

        // Category manager button click
        btnCategoryManager.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_todoListFragment_to_categoryManagementFragment);
        });

        moveButton.setOnClickListener(v -> moveSelectedTodos());
        deleteButton.setOnClickListener(v -> deleteSelectedTodos());

        // Add references to selection mode buttons
        TextView btnCancelSelection = view.findViewById(R.id.btnCancelSelection);
        TextView btnSelectAll = view.findViewById(R.id.btnSelectAll);

        // Cancel selection mode
        btnCancelSelection.setOnClickListener(v -> {
            adapter.setSelectionMode(false);
            onSelectionModeChanged(false, new HashSet<>());
        });

        // Select all/none toggle
        btnSelectAll.setOnClickListener(v -> {
            if (allTodosSelected) {
                // Unselect all todos
                adapter.selectAll(new HashSet<>());
                // Update visual state immediately
                btnSelectAll.setTextColor(Color.parseColor("#4A4A4A")); // Original color
            } else {
                // Select all todos
                Set<Todo> allTodos = new HashSet<>(todoList);
                adapter.selectAll(allTodos);
                // Update visual state immediately
                btnSelectAll.setTextColor(Color.parseColor("#2196F3")); // Blue 400
            }
            // Toggle the state
            allTodosSelected = !allTodosSelected;
        });

        TextView btnSettings = view.findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_todoListFragment_to_settingsMenuFragment);
        });
    }

    private void setupCategoryFilter() {
        // Clear previous chips first
        categoryChipGroup.removeAllViews();

        // Create "All" chip first
        Chip allChip = createCategoryChip("All");
        allChip.setChecked(true);
        categoryChipGroup.addView(allChip);

        // Observe categories and add chips
        todoViewModel.getAllUniqueCategories().observe(getViewLifecycleOwner(), categories -> {
            // Skip "All" chip since we already added it
            for (String category : categories) {
                if (category != null && !category.isEmpty()) {
                    Chip chip = createCategoryChip(category);
                    categoryChipGroup.addView(chip);
                }
            }
        });
    }

    private Chip createCategoryChip(String category) {
        Chip chip = new Chip(requireContext());
        chip.setText(category);
        chip.setCheckable(true);
        chip.setClickable(true);

        // Use chip-specific styling methods
        chip.setChipBackgroundColorResource(R.color.chip_background_color);
        chip.setChipStrokeWidth(0f);  // No border
        chip.setElevation(0f);  // No shadow/elevation

        // Set text color
        chip.setTextColor(getResources().getColorStateList(R.color.chip_text_color, null));

        // Set chip padding - 24sp instead of 12sp
        chip.setChipMinHeight(108f);
        chip.setChipStartPadding(24f);
        chip.setChipEndPadding(24f);
        chip.setPadding(0, 12, 0, 12);

        // Remove ripple effect
        chip.setRippleColor(null);

        // Set chip click listener
        chip.setOnClickListener(v -> {
            if ("All".equals(category)) {
                currentCategory = null; // null means show all todos
                loadAllTodos();
            } else {
                currentCategory = category;
                filterTodosByCategory(category);
            }
        });

        return chip;
    }

    private void loadAllTodos() {
        todoViewModel.getSortedTodos().observe(getViewLifecycleOwner(), todos -> {
            todoList.clear();
            if (todos != null) {
                todoList.addAll(todos);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void filterTodosByCategory(String category) {
        todoViewModel.getTodosByCategory(category).observe(getViewLifecycleOwner(), todos -> {
            todoList.clear();
            if (todos != null) {
                todoList.addAll(todos);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void searchTodos(String query) {
        if (query.isEmpty()) {
            // Return to current category view or all todos if no category selected
            if (currentCategory == null) {
                loadAllTodos();
            } else {
                filterTodosByCategory(currentCategory);
            }
            return;
        }

        // Search within the current category filter if one is applied
        todoViewModel.searchTodos(query).observe(getViewLifecycleOwner(), todos -> {
            todoList.clear();

            if (todos != null) {
                if (currentCategory != null) {
                    // Filter search results by current category
                    for (Todo todo : todos) {
                        if (currentCategory.equals(todo.getCategory())) {
                            todoList.add(todo);
                        }
                    }
                } else {
                    // No category filter, show all search results
                    todoList.addAll(todos);
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    // Required implementations for TodoAdapter.OnTodoClickListener
    @Override
    public void onTodoClick(Todo todo) {
        Bundle args = new Bundle();
        args.putString("todoId", String.valueOf(todo.getId()));
        Navigation.findNavController(requireView()).navigate(
                R.id.action_todoListFragment_to_todoFormFragment, args);
    }

    @Override
    public void onTodoDelete(Todo todo) {
        todoViewModel.delete(todo);
    }

    @Override
    public void onTodoStatusChanged(Todo todo, boolean isCompleted) {
        todo.setCompleted(isCompleted);
        todoViewModel.update(todo);
    }

    @Override
    public void onSelectionModeChanged(boolean active, Set<Todo> selectedItems) {
        TextView titleText = requireView().findViewById(R.id.title);
        TextView btnCancel = requireView().findViewById(R.id.btnCancelSelection);
        TextView btnSelectAll = requireView().findViewById(R.id.btnSelectAll);
        TextView btnSettings = requireView().findViewById(R.id.btnSettings);

        if (active) {
            // Update title with proper singular/plural form
            int count = selectedItems.size();
            titleText.setText(count == 1 ? "1 item selected" : count + " items selected");

            // Check if all todos in the current list are selected
            allTodosSelected = !todoList.isEmpty() && selectedItems.size() == todoList.size();

            // Update select all button appearance based on state
            if (allTodosSelected) {
                btnSelectAll.setTextColor(Color.parseColor("#2196F3")); // Blue 400
            } else {
                btnSelectAll.setTextColor(Color.parseColor("#4A4A4A")); // Original color
            }

            // Show selection mode controls
            btnCancel.setVisibility(View.VISIBLE);
            btnSelectAll.setVisibility(View.VISIBLE);

            // Hide regular toolbar buttons
            btnCategoryManager.setVisibility(View.GONE);
            btnSettings.setVisibility(View.GONE);
            searchView.setVisibility(View.GONE);

            // Show bottom action bar and hide FAB
            bottomActionBar.setVisibility(View.VISIBLE);
            btnAdd.hide();
        } else {
            // Restore original title and UI
            titleText.setText("Todoo");

            // Hide selection mode controls
            btnCancel.setVisibility(View.GONE);
            btnSelectAll.setVisibility(View.GONE);

            // Show regular toolbar buttons
            btnCategoryManager.setVisibility(View.VISIBLE);
            btnSettings.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.VISIBLE);

            // Hide bottom action bar and show FAB
            bottomActionBar.setVisibility(View.GONE);
            btnAdd.show();

            // Reset selection state
            allTodosSelected = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Handle back button press to exit selection mode
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (adapter != null && adapter.isSelectionMode()) {
                            // Exit selection mode instead of going back
                            adapter.setSelectionMode(false);
                            onSelectionModeChanged(false, new HashSet<>());
                        } else {
                            // Normal back behavior
                            this.remove();
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });
    }

    private void moveSelectedTodos() {
        if (adapter.isSelectionMode() && !adapter.getSelectedTodos().isEmpty()) {
            Bundle args = new Bundle();
            long[] todoIds = new long[adapter.getSelectedTodos().size()];
            int i = 0;
            for (Todo todo : adapter.getSelectedTodos()) {
                todoIds[i++] = todo.getId();
            }
            args.putLongArray("selectedTodoIds", todoIds);
            Navigation.findNavController(requireView()).navigate(
                    R.id.action_todoListFragment_to_categoryManagementFragment, args);
        }
    }

    private void deleteSelectedTodos() {
        if (adapter.isSelectionMode() && !adapter.getSelectedTodos().isEmpty()) {
            for (Todo todo : adapter.getSelectedTodos()) {
                todoViewModel.delete(todo);
            }
            // Call onSelectionModeChanged to properly update the UI
            onSelectionModeChanged(false, new HashSet<>());
            adapter.setSelectionMode(false);
        }
    }
}