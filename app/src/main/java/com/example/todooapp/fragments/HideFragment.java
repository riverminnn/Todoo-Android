package com.example.todooapp.fragments;

import android.os.Bundle;
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

public class HideFragment extends Fragment implements TodoAdapter.OnTodoClickListener {
    private RecyclerView recyclerView;
    private TodoViewModel todoViewModel;
    private TodoAdapter adapter;
    private List<Todo> hiddenTodoList = new ArrayList<>();
    private TextView btnBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hide, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        recyclerView = view.findViewById(R.id.recyclerViewHidden);
        btnBack = view.findViewById(R.id.btnBack);

        // Initialize ViewModel
        todoViewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);

        // Setup RecyclerView with special adapter for hidden items
        adapter = new TodoAdapter(hiddenTodoList, this);
        // Disable selection mode functionality
        adapter.setSelectionMode(false);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Load hidden todos
        loadHiddenTodos();

        // Back button click
        btnBack.setOnClickListener(v ->
                Navigation.findNavController(requireView()).popBackStack());
    }

    private void loadHiddenTodos() {
        todoViewModel.getHiddenTodos().observe(getViewLifecycleOwner(), todos -> {
            hiddenTodoList.clear();
            if (todos != null) {
                hiddenTodoList.addAll(todos);
            }
            adapter.notifyDataSetChanged();

            // Update empty state view
            updateEmptyState();
        });
    }

    private void updateEmptyState() {
        View emptyState = requireView().findViewById(R.id.emptyHiddenState);
        if (hiddenTodoList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTodoClick(Todo todo) {
        // Show unhide/delete options when clicking on hidden todo
        new TodooDialogBuilder(requireContext())
                .setTitle("Hidden Item Options")
                .setMessage("What would you like to do with this hidden item?")
                .setPositiveButton("Unhide", (dialog, which) -> {
                    todoViewModel.unhideItem(todo);
                    Toast.makeText(requireContext(), "Item unhidden", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Move to Trash", (dialog, which) -> {
                    todoViewModel.moveToTrash(todo);
                    Toast.makeText(requireContext(), "Item moved to trash", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    public void onTodoDelete(Todo todo) {
        // Move to trash rather than delete directly
        todoViewModel.moveToTrash(todo);
        Toast.makeText(requireContext(), "Item moved to trash", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTodoStatusChanged(Todo todo, boolean isCompleted) {
        // No status changes for hidden items
        // This method is required by the interface but we don't use it here
    }

    @Override
    public void onSelectionModeChanged(boolean active, Set<Todo> selectedItems) {
        // Force disable selection mode
        if (active) {
            adapter.setSelectionMode(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Handle back press to prevent selection mode
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        this.remove();
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                });
    }
}