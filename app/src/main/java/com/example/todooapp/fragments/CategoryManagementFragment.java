package com.example.todooapp.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.example.todooapp.data.model.Todo;
import com.example.todooapp.utils.TodooDialogBuilder;
import com.example.todooapp.viewmodel.TodoViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategoryManagementFragment extends Fragment implements CategoryAdapter.CategoryClickListener {

    private RecyclerView rvCategories;
    private TodoViewModel todoViewModel;
    private CategoryAdapter categoryAdapter;
    private TextView btnBack, btnX, btnSelectAll, titleText, btnDelete, btnEdit, btnTrashCan;
    private LinearLayout bottomActionBar, editLayout;
    private TextView actionAddFolder;
    private Set<String> selectedCategories = new HashSet<>();
    private long[] selectedTodoIds = null;
    private boolean moveMode = false;
    private boolean allCategoriesSelected = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViewModel();
        initializeViews(view);
        setupRecyclerView();
        checkMoveMode(view);
        setupNormalModeUI();
        setupClickListeners(view);
        loadCategoriesWithCounts();
    }

    private void initializeViewModel() {
        todoViewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);
    }

    private void initializeViews(View view) {
        rvCategories = view.findViewById(R.id.rvCategories);
        btnBack = view.findViewById(R.id.btnBack);
        btnX = view.findViewById(R.id.btnCancelSelection);
        btnSelectAll = view.findViewById(R.id.btnSelectAll);
        titleText = view.findViewById(R.id.title);
        actionAddFolder = view.findViewById(R.id.action_add_folder);
        bottomActionBar = view.findViewById(R.id.bottomActionBar);
        editLayout = view.findViewById(R.id.action_edit_container);
        btnDelete = view.findViewById(R.id.action_delete);
        btnEdit = view.findViewById(R.id.action_edit);
        btnTrashCan = view.findViewById(R.id.btnTrashCan);
    }

    private void setupRecyclerView() {
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void checkMoveMode(View view) {
        if (getArguments() != null && getArguments().getLongArray("selectedTodoIds") != null) {
            selectedTodoIds = getArguments().getLongArray("selectedTodoIds");
            moveMode = true;
            setupForMoveMode();
        }
    }

    private void setupClickListeners(View view) {
        btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        btnX.setOnClickListener(v -> exitSelectionMode());
        btnSelectAll.setOnClickListener(v -> toggleSelectAll());
        actionAddFolder.setOnClickListener(v -> showAddCategoryDialog());
        btnDelete.setOnClickListener(v -> {
            if (!selectedCategories.isEmpty()) {
                confirmDeleteCategories();
            } else {
                Toast.makeText(requireContext(), "Select folders to delete", Toast.LENGTH_SHORT).show();
            }
        });
        btnEdit.setOnClickListener(v -> {
            if (selectedCategories.size() == 1) {
                String category = selectedCategories.iterator().next();
                showEditCategoryDialog(category);
            } else {
                Toast.makeText(requireContext(), "Select only one folder to edit", Toast.LENGTH_SHORT).show();
            }
        });
        btnTrashCan.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_categoryManagementFragment_to_trashFragment));
    }

    private void setupNormalModeUI() {
        titleText.setText("Folders");
        btnBack.setVisibility(View.VISIBLE);
        btnX.setVisibility(View.GONE);
        btnSelectAll.setVisibility(View.GONE);
        actionAddFolder.setVisibility(View.VISIBLE);
        bottomActionBar.setVisibility(View.GONE);
        btnTrashCan.setVisibility(View.VISIBLE);
    }

    private void setupSelectionModeUI(int selectedCount) {
        titleText.setText(selectedCount == 1 ? "1 Folder selected" : selectedCount + " Folders selected");
        btnBack.setVisibility(View.GONE);
        btnX.setVisibility(View.VISIBLE);
        btnSelectAll.setVisibility(View.VISIBLE);
        actionAddFolder.setVisibility(View.GONE);
        bottomActionBar.setVisibility(View.VISIBLE);
        btnTrashCan.setVisibility(View.GONE);
    }

    private void setupForMoveMode() {
        titleText.setText("Move to Folder");
        btnBack.setVisibility(View.VISIBLE);
        bottomActionBar.setVisibility(View.GONE);
    }

    private void toggleSelectAll() {
        List<String> categories = categoryAdapter.getCategories();

        if (allCategoriesSelected) {
            selectedCategories.clear();
            btnSelectAll.setTextColor(Color.parseColor("#4A4A4A"));
        } else {
            selectedCategories.addAll(categories);
            btnSelectAll.setTextColor(Color.parseColor("#2196F3"));
        }

        allCategoriesSelected = !allCategoriesSelected;
        categoryAdapter.notifyDataSetChanged();
        onSelectionModeChanged(true, selectedCategories);
    }

    private void exitSelectionMode() {
        categoryAdapter.setSelectionMode(false);
        selectedCategories.clear();
        allCategoriesSelected = false;
        setupNormalModeUI();
    }

    private void loadCategoriesWithCounts() {
        todoViewModel.getAllUniqueCategories().observe(getViewLifecycleOwner(), categories -> {
            todoViewModel.getAllTodos().observe(getViewLifecycleOwner(), todos -> {
                Map<String, Integer> categoryCounts = new HashMap<>();

                for (String category : categories) {
                    if (category != null && !category.isEmpty()) {
                        categoryCounts.put(category, 0);
                    }
                }

                if (todos != null) {
                    for (Todo todo : todos) {
                        String todoCategory = todo.getCategory();
                        if (todoCategory != null && !todoCategory.isEmpty()) {
                            categoryCounts.put(todoCategory, categoryCounts.getOrDefault(todoCategory, 0) + 1);
                        }
                    }
                }

                categoryAdapter = new CategoryAdapter(new ArrayList<>(categoryCounts.keySet()), categoryCounts, this);
                rvCategories.setAdapter(categoryAdapter);
            });
        });
    }

    private void showAddCategoryDialog() {
        new TodooDialogBuilder(requireContext())
                .withInputField(
                        "Add New Folder",
                        "Folder name",
                        null,
                        categoryName -> {
                            if (!categoryName.isEmpty()) {
                                addCategory(categoryName);
                            } else {
                                Toast.makeText(requireContext(), "Folder name cannot be empty", Toast.LENGTH_SHORT).show();
                            }
                        })
                .show();
    }

    private void showEditCategoryDialog(String currentName) {
        new TodooDialogBuilder(requireContext())
                .withInputField(
                        "Edit Folder Name",
                        "Folder name",
                        currentName,
                        newName -> {
                            if (!newName.isEmpty() && !newName.equals(currentName)) {
                                todoViewModel.updateCategory(currentName, newName);
                                Toast.makeText(requireContext(), "Folder renamed", Toast.LENGTH_SHORT).show();
                                exitSelectionMode();
                                loadCategoriesWithCounts();
                            }
                        })
                .show();
    }

    private void addCategory(String category) {
        boolean success = todoViewModel.addCategory(category);
        if (!success) {
            Toast.makeText(requireContext(), "Folder already exists", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Folder added: " + category, Toast.LENGTH_SHORT).show();
            loadCategoriesWithCounts();
        }
    }

    private void confirmDeleteCategories() {
        new TodooDialogBuilder(requireContext())
                .setTitle("Delete Folders")
                .setMessage("Are you sure you want to delete the selected folders? All todos in these folders will be moved to 'Uncategorized'.")
                .setPositiveButton("Delete", (dialog, which) -> deleteSelectedCategories())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteSelectedCategories() {
        for (String category : selectedCategories) {
            todoViewModel.deleteCategory(category);
        }

        Toast.makeText(requireContext(), "Folders deleted", Toast.LENGTH_SHORT).show();
        exitSelectionMode();
        loadCategoriesWithCounts();
    }

    @Override
    public void onCategoryClick(String category) {
        if (moveMode && selectedTodoIds != null) {
            for (long todoId : selectedTodoIds) {
                todoViewModel.updateTodoCategory(todoId, category);
            }
            Toast.makeText(requireContext(), "Items moved to " + category, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).popBackStack();
        }
    }

    @Override
    public void onSelectionModeChanged(boolean active, Set<String> selectedItems) {
        this.selectedCategories = selectedItems;

        if (active) {
            setupSelectionModeUI(selectedItems.size());
            editLayout.setVisibility(selectedItems.size() == 1 ? View.VISIBLE : View.GONE);

            if (categoryAdapter != null) {
                List<String> allCategories = categoryAdapter.getCategories();
                allCategoriesSelected = !allCategories.isEmpty() && selectedItems.size() == allCategories.size();
                btnSelectAll.setTextColor(allCategoriesSelected ? Color.parseColor("#2196F3") : Color.parseColor("#4A4A4A"));
            }
        } else {
            setupNormalModeUI();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (categoryAdapter != null && categoryAdapter.isSelectionMode()) {
                            exitSelectionMode();
                        } else {
                            this.remove();
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });
    }
}