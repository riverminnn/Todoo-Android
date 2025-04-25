package com.example.todooapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.todooapp.adapter.TodoAdapter;
import com.example.todooapp.data.model.Todo;
import com.example.todooapp.utils.TodooDialogBuilder;
import com.example.todooapp.viewmodel.TodoViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TrashFragment extends Fragment implements TodoAdapter.OnTodoClickListener {
    private RecyclerView recyclerView;
    private TodoViewModel todoViewModel;
    private TodoAdapter adapter;
    private List<Todo> trashedTodoList = new ArrayList<>();
    private TextView emptyTrashButton;
    private TextView btnBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Initialize UI components
        recyclerView = view.findViewById(R.id.recyclerViewTrash);
        emptyTrashButton = view.findViewById(R.id.btnEmptyTrash);
        btnBack = view.findViewById(R.id.btnBack);
        TextView infoText = view.findViewById(R.id.trashInfoText);

        // Set info text
        infoText.setText("Items in trash will be automatically deleted after 30 days");

        // Initialize ViewModel
        todoViewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);

        // Check for and delete expired trashed todos
        todoViewModel.deleteExpiredTrashedTodos();

        // Setup RecyclerView with special adapter for trash
        adapter = new TodoAdapter(trashedTodoList, this);
        adapter.setTrashMode(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Load trashed todos
        loadTrashedTodos();

        // Back button click
        btnBack.setOnClickListener(v ->
                Navigation.findNavController(requireView()).popBackStack());

        // Empty trash button click
        emptyTrashButton.setOnClickListener(v -> confirmEmptyTrash());
    }

    // In TrashFragment.java - Ensure we're loading trash items correctly
    private void loadTrashedTodos() {

// After (added debug logs to help diagnose):
        todoViewModel.getTrashTodos().observe(getViewLifecycleOwner(), todos -> {
            trashedTodoList.clear();
            if (todos != null) {
                trashedTodoList.addAll(todos);
                // Added more detailed logging to debug trash content
                for (Todo todo : todos) {
                    Log.d("TrashFragment", "Trash item: " + todo.getTitle() + ", inTrash=" + todo.isInTrash());
                }
            }
            adapter.notifyDataSetChanged();

            // Update empty state view
            updateEmptyState();

            // Debug log to check if trash items are being found
            Log.d("TrashFragment", "Loaded " + trashedTodoList.size() + " trashed items");
        });
    }

    private void updateEmptyState() {
        View emptyState = requireView().findViewById(R.id.emptyTrashState);
        if (trashedTodoList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void confirmEmptyTrash() {
        if (trashedTodoList.isEmpty()) {
            Toast.makeText(requireContext(), "Trash is already empty", Toast.LENGTH_SHORT).show();
            return;
        }

        new TodooDialogBuilder(requireContext())
                .setTitle("Empty Trash")
                .setMessage("Are you sure you want to permanently delete all items in trash? This action cannot be undone.")
                .setPositiveButton("Empty Trash", (dialog, which) -> {
                    todoViewModel.emptyTrash();
                    Toast.makeText(requireContext(), "Trash emptied", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Implementation of TodoAdapter.OnTodoClickListener methods
    @Override
    public void onTodoClick(Todo todo) {
        // Show restore/delete options when clicking on trashed todo
        new TodooDialogBuilder(requireContext())
                .setTitle("Todo Options")
                .setMessage("Would you like to restore this item or delete it permanently?")
                .setPositiveButton("Restore", (dialog, which) -> {
                    todoViewModel.restoreFromTrash(todo);
                    Toast.makeText(requireContext(), "Todo restored", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Delete Permanently", (dialog, which) -> {
                    todoViewModel.delete(todo);
                    Toast.makeText(requireContext(), "Todo permanently deleted", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    public void onTodoDelete(Todo todo) {
        // Permanently delete
        todoViewModel.delete(todo);
    }

    @Override
    public void onTodoStatusChanged(Todo todo, boolean isCompleted) {
        // No status changes in trash
    }

    @Override
    public void onSelectionModeChanged(boolean active, Set<Todo> selectedItems) {
        // Not implementing selection mode in trash for this version
    }
}