package com.example.todooapp.fragments;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.todooapp.R;
import com.example.todooapp.adapter.TodoAdapter;
import com.example.todooapp.data.model.Todo;
import com.example.todooapp.utils.settings.SettingsManager;
import com.example.todooapp.utils.shared.TodooDialogBuilder;
import com.example.todooapp.viewmodel.TodoViewModel;
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
    private String currentCategory; // null means "All"

    private SettingsManager settingsManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set system bar colors programmatically (alternative approach)
        // Get the backgroundColor from the theme
        TypedValue typedValue = new TypedValue();
        requireActivity().getTheme().resolveAttribute(R.attr.backgroundColor, typedValue, true);
        int backgroundColor = typedValue.data;

        // Set system bar colors through the activity
        requireActivity().getWindow().setStatusBarColor(backgroundColor);
        requireActivity().getWindow().setNavigationBarColor(backgroundColor);

        todoViewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);

        initializeViews(view);
        setupRecyclerView();
        setupPinButton(view);
        setupCategoryFilter();
        setupSearchView();
        setupNavigationButtons(view);
        setupSelectionModeButtons(view);
        setupHideButton(view);
        setupSwipeRefresh(view);
        loadAllTodos();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnCategoryManager = view.findViewById(R.id.btnCategoryManager);
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup);
        bottomActionBar = view.findViewById(R.id.bottomActionBar);

        // Initialize settings manager
        settingsManager = new SettingsManager(requireContext());
    }

    private void setupRecyclerView() {
        adapter = new TodoAdapter(todoList, this);
        recyclerView.setAdapter(adapter);

        // Apply the layout based on user settings
        applyLayoutSettings();
    }

    private void applyLayoutSettings() {
        String layoutType = settingsManager.getLayoutType();
        if ("Grid view".equals(layoutType)) {
            // Use GridLayoutManager with 2 columns for grid view
            int spanCount = 2;
            recyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(
                    requireContext(), spanCount));
            adapter.setLayoutType(TodoAdapter.LAYOUT_GRID);
        } else {
            // Use LinearLayoutManager for list view (default)
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter.setLayoutType(TodoAdapter.LAYOUT_LIST);
        }
    }

    private void setupSearchView() {
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
    }

    private void setupPinButton(View view) {
        TextView pinButton = view.findViewById(R.id.action_pin);
        LinearLayout pinContainer = view.findViewById(R.id.action_pin_container);
        TextView pinLabel = view.findViewById(R.id.action_pin_label);

        pinButton.setOnClickListener(v -> togglePinStatusForSelectedTodos());
        pinContainer.setOnClickListener(v -> togglePinStatusForSelectedTodos());
    }

    private void togglePinStatusForSelectedTodos() {
        if (adapter.isSelectionMode() && !adapter.getSelectedTodos().isEmpty()) {
            // Check if all selected todos are already pinned
            boolean allPinned = true;
            for (Todo todo : adapter.getSelectedTodos()) {
                if (!todo.isPinned()) {
                    allPinned = false;
                    break;
                }
            }

            // Toggle pin status based on current state
            todoViewModel.togglePinStatus(adapter.getSelectedTodos(), !allPinned);
            Toast.makeText(requireContext(),
                    allPinned ? "Items unpinned" : "Items pinned",
                    Toast.LENGTH_SHORT).show();

            adapter.setSelectionMode(false);
            onSelectionModeChanged(false, new HashSet<>());
            loadAllTodos();
        }
    }

    private void setupNavigationButtons(View view) {
        btnAdd.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_todoListFragment_to_todoFormFragment));

        btnCategoryManager.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_todoListFragment_to_categoryManagementFragment));

        TextView btnSettings = view.findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_todoListFragment_to_settingsMenuFragment));

        TextView moveButton = view.findViewById(R.id.action_move);
        moveButton.setOnClickListener(v -> moveSelectedTodos());

        TextView deleteButton = view.findViewById(R.id.action_delete);
        deleteButton.setOnClickListener(v -> deleteSelectedTodos());
    }

    private void setupSelectionModeButtons(View view) {
        TextView btnCancelSelection = view.findViewById(R.id.btnCancelSelection);
        TextView btnSelectAll = view.findViewById(R.id.btnSelectAll);

        btnCancelSelection.setOnClickListener(v -> {
            adapter.setSelectionMode(false);
            onSelectionModeChanged(false, new HashSet<>());
        });

        btnSelectAll.setOnClickListener(v -> {
            if (allTodosSelected) {
                adapter.selectAll(new HashSet<>());
                btnSelectAll.setTextColor(Color.parseColor("#4A4A4A"));
            } else {
                Set<Todo> allTodos = new HashSet<>(todoList);
                adapter.selectAll(allTodos);
                btnSelectAll.setTextColor(Color.parseColor("#2196F3"));
            }
            allTodosSelected = !allTodosSelected;
        });
    }

    private void setupHideButton(View view) {
        TextView hideButton = view.findViewById(R.id.action_hide);
        LinearLayout hideContainer = view.findViewById(R.id.action_hide_container);

        hideButton.setOnClickListener(v -> hideSelectedTodos());
        hideContainer.setOnClickListener(v -> hideSelectedTodos());
    }

    private void setupSwipeRefresh(View view) {
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        TextView lockIcon = view.findViewById(R.id.lockIcon);

        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setProgressViewOffset(true, -1000, -1000);
        swipeRefreshLayout.setColorSchemeColors(Color.TRANSPARENT);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.TRANSPARENT);
        swipeRefreshLayout.setSlingshotDistance(0);
        swipeRefreshLayout.setProgressViewEndTarget(true, -1000);
        swipeRefreshLayout.setDistanceToTriggerSync(500);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            lockIcon.setVisibility(View.VISIBLE);
            lockIcon.setAlpha(1f);
            lockIcon.setScaleX(1.2f);
            lockIcon.setScaleY(1.2f);

            lockIcon.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        Navigation.findNavController(view).navigate(R.id.action_todoListFragment_to_hideFragment);
                        swipeRefreshLayout.setRefreshing(false);
                        lockIcon.setVisibility(View.GONE);
                        lockIcon.setScaleX(1f);
                        lockIcon.setScaleY(1f);
                    });
        });
    }

    private void hideSelectedTodos() {
        if (adapter.isSelectionMode() && !adapter.getSelectedTodos().isEmpty()) {
            for (Todo todo : adapter.getSelectedTodos()) {
                todoViewModel.hideItem(todo);
            }

            Toast.makeText(
                    requireContext(),
                    "Items hidden. Pull down from the top to see hidden todos.",
                    Toast.LENGTH_LONG
            ).show();

            adapter.setSelectionMode(false);
            onSelectionModeChanged(false, new HashSet<>());
            loadAllTodos();
        }
    }

    private void setupCategoryFilter() {
        categoryChipGroup.removeAllViews();
        Chip allChip = createCategoryChip("All");

        // Check saved category selection in ViewModel
        String savedCategory = todoViewModel.getCurrentCategory();
        boolean isAllSelected = savedCategory == null;

        allChip.setChecked(isAllSelected);
        categoryChipGroup.addView(allChip);

        todoViewModel.getAllUniqueCategories().observe(getViewLifecycleOwner(), categories -> {
            // Remove all existing category chips (except "All" which is at index 0)
            if (categoryChipGroup.getChildCount() > 1) {
                categoryChipGroup.removeViews(1, categoryChipGroup.getChildCount() - 1);
            }

            // Add the new category chips
            for (String category : categories) {
                if (category != null && !category.isEmpty()) {
                    Chip chip = createCategoryChip(category);

                    // Check if this is the previously selected category
                    if (category.equals(savedCategory)) {
                        chip.setChecked(true);
                        // Make sure we load the correct data for this category
                        currentCategory = category;
                        filterTodosByCategory(category);
                    }

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

        // Use theme attributes instead of static colors
        int[] attrs = {R.attr.backgroundColor, R.attr.textColorPrimary, R.attr.cardBackgroundColor};
        @SuppressLint("ResourceType") android.content.res.TypedArray ta = requireContext().obtainStyledAttributes(attrs);

        // Create state list for chip background color
        @SuppressLint("ResourceType") ColorStateList chipColorStateList = new ColorStateList(
                new int[][] {
                        new int[] {android.R.attr.state_checked},
                        new int[] {}
                },
                new int[] {
                        ta.getColor(2, 0), // colorControlActivated for selected state
                        ta.getColor(0, 0)  // backgroundColor for default state
                }
        );

        // Create state list for text color
        @SuppressLint("ResourceType") ColorStateList textColorStateList = new ColorStateList(
                new int[][] {
                        new int[] {android.R.attr.state_checked},
                        new int[] {}
                },
                new int[] {
                        ta.getColor(1, 0), // White text for selected state
                        ta.getColor(1, 0)  // textColorPrimary for default state
                }
        );

        ta.recycle();

        chip.setChipBackgroundColor(chipColorStateList);
        chip.setTextColor(textColorStateList);

        // Keep the rest of the styling
        chip.setChipStrokeWidth(0f);
        chip.setElevation(0f);
        chip.setChipMinHeight(108f);
        chip.setChipStartPadding(24f);
        chip.setChipEndPadding(24f);
        chip.setPadding(0, 12, 0, 12);
        chip.setRippleColor(null);

        chip.setOnClickListener(v -> {
            if ("All".equals(category)) {
                currentCategory = null;
                todoViewModel.setCurrentCategory(null); // Save to ViewModel
                loadAllTodos();
            } else {
                currentCategory = category;
                todoViewModel.setCurrentCategory(category); // Save to ViewModel
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
            if (currentCategory == null) {
                loadAllTodos();
            } else {
                filterTodosByCategory(currentCategory);
            }
            return;
        }

        todoViewModel.searchTodos(query).observe(getViewLifecycleOwner(), todos -> {
            todoList.clear();

            if (todos != null) {
                if (currentCategory != null) {
                    for (Todo todo : todos) {
                        if (currentCategory.equals(todo.getCategory())) {
                            todoList.add(todo);
                        }
                    }
                } else {
                    todoList.addAll(todos);
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

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
        // Update the pin/unpin button based on selection
        TextView pinButton = requireView().findViewById(R.id.action_pin);
        TextView pinLabel = requireView().findViewById(R.id.action_pin_label);

        boolean allPinned = !selectedItems.isEmpty();
        for (Todo todo : selectedItems) {
            if (!todo.isPinned()) {
                allPinned = false;
                break;
            }
        }

        // Update icon and text based on whether all selected items are pinned
        if (allPinned) {
            pinButton.setText("\ue68f"); // Using unpin icon
            pinLabel.setText("Unpin");
        } else {
            pinButton.setText("\uf08d"); // Using pin icon
            pinLabel.setText("Pin");
        }

        if (active) {
            int count = selectedItems.size();
            titleText.setText(count == 1 ? "1 item selected" : count + " items selected");

            allTodosSelected = !todoList.isEmpty() && selectedItems.size() == todoList.size();
            btnSelectAll.setTextColor(allTodosSelected ? Color.parseColor("#2196F3") : Color.parseColor("#4A4A4A"));

            btnCancel.setVisibility(View.VISIBLE);
            btnSelectAll.setVisibility(View.VISIBLE);
            btnCategoryManager.setVisibility(View.GONE);
            btnSettings.setVisibility(View.GONE);
            bottomActionBar.setVisibility(View.VISIBLE);
            btnAdd.hide();
        } else {
            titleText.setText("Todoo");
            btnCancel.setVisibility(View.GONE);
            btnSelectAll.setVisibility(View.GONE);
            btnCategoryManager.setVisibility(View.VISIBLE);
            btnSettings.setVisibility(View.VISIBLE);
            bottomActionBar.setVisibility(View.GONE);
            btnAdd.show();
            allTodosSelected = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Apply layout settings in case they changed while fragment was paused
        applyLayoutSettings();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (adapter != null && adapter.isSelectionMode()) {
                            adapter.setSelectionMode(false);
                            onSelectionModeChanged(false, new HashSet<>());
                        } else {
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
            new TodooDialogBuilder(requireContext())
                    .setTitle("Delete Items")
                    .setMessage("What would you like to do with the selected items?")
                    .setPositiveButton("Move to Trash", (dialog, which) -> {
                        for (Todo todo : adapter.getSelectedTodos()) {
                            todoViewModel.moveToTrash(todo);
                        }
                        Toast.makeText(requireContext(), "Items moved to trash", Toast.LENGTH_SHORT).show();
                        exitSelectionMode();
                    })
                    .setNegativeButton("Delete Permanently", (dialog, which) -> {
                        for (Todo todo : adapter.getSelectedTodos()) {
                            todoViewModel.delete(todo);
                        }
                        Toast.makeText(requireContext(), "Items deleted permanently", Toast.LENGTH_SHORT).show();
                        exitSelectionMode();
                    })
                    .show();
        }
    }

    // Add this helper method to properly exit selection mode
    private void exitSelectionMode() {
        adapter.setSelectionMode(false);
        onSelectionModeChanged(false, new HashSet<>());
        loadAllTodos();
    }
}