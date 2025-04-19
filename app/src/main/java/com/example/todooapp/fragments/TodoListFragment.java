package com.example.todooapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

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
import java.util.List;
import java.util.Set;

public class TodoListFragment extends Fragment implements TodoAdapter.OnTodoClickListener {
    private RecyclerView recyclerView;
    private SearchView searchView;
    private FloatingActionButton btnAdd;
    private ImageButton btnCategoryManager;
    private BottomNavigationView bottomActionBar;
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
        ImageButton btnUserAuth = view.findViewById(R.id.btnUserAuth);
        UserManager userManager = new UserManager(requireContext());

        // Set icon based on login status
        updateUserAuthButton(btnUserAuth, userManager);

        // Get references to UI elements
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnCategoryManager = view.findViewById(R.id.btnCategoryManager);
        bottomActionBar = view.findViewById(R.id.bottomActionBar);
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

        // Bottom action bar for selections
        bottomActionBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_move) {
                moveSelectedTodos();
                return true;
            } else if (itemId == R.id.action_delete) {
                deleteSelectedTodos();
                return true;
            }
            return false;
        });
        // User authentication button click
        btnUserAuth.setOnClickListener(v -> {
            if (userManager.isLoggedIn()) {
                // Show logout confirmation
                new AlertDialog.Builder(requireContext())
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            userManager.clearUserSession();
                            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                            updateUserAuthButton(btnUserAuth, userManager);
                        })
                        .setNegativeButton("No", null)
                        .show();
            } else {
                // Navigate to login screen
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_todoListFragment_to_loginFragment);
            }
        });
    }

    // Add this helper method to TodoListFragment
    private void updateUserAuthButton(ImageButton button, UserManager userManager) {
        if (userManager.isLoggedIn()) {
            button.setImageResource(android.R.drawable.ic_menu_myplaces); // Logged in icon
        } else {
            button.setImageResource(android.R.drawable.ic_menu_help); // Not logged in icon
        }
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
        chip.setChipBackgroundColorResource(R.color.design_default_color_primary);

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
        todoViewModel.getAllTodos().observe(getViewLifecycleOwner(), todos -> {
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
        if (active) {
            bottomActionBar.setVisibility(View.VISIBLE);
            btnAdd.hide();
        } else {
            bottomActionBar.setVisibility(View.GONE);
            btnAdd.show();
        }
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
            adapter.setSelectionMode(false);
            bottomActionBar.setVisibility(View.GONE);
            btnAdd.show();
        }
    }
}