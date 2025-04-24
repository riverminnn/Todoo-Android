// New file to create:
package com.example.todooapp.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.todooapp.R;
import com.example.todooapp.data.model.Todo;
import com.example.todooapp.viewmodel.TodoViewModel;

public class TodoFormFragment extends Fragment {
    private EditText etTitle, etContent;
    private TodoViewModel todoViewModel;
    private String todoId = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo_form, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        etTitle = view.findViewById(R.id.etTitle);
        etContent = view.findViewById(R.id.etContent);

        // Set up back button with animation
        TextView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Scale down on press
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    return false;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Scale back to normal on release
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    return false;
            }
            return false;
        });

        // Modified back button to auto-save if content exists
        btnBack.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (!title.isEmpty() || !content.isEmpty()) {
                // Save only if there's content to save
                autoSaveTodo();
            } else {
                // Just go back if empty
                Navigation.findNavController(requireView()).popBackStack();
            }
        });

        // Set up menu button with dropdown
        TextView btnMenu = view.findViewById(R.id.btnMenu);
        View menuBackgroundOverlay = view.findViewById(R.id.menuBackgroundOverlay);

        btnMenu.setOnClickListener(v -> {
            // Show overlay
            menuBackgroundOverlay.setVisibility(View.VISIBLE);

            // Create and show popup menu with custom style
            Context wrapper = new ContextThemeWrapper(requireContext(), R.style.CustomMenuTheme);
            PopupMenu popupMenu = new PopupMenu(wrapper, v);
            popupMenu.inflate(R.menu.menu_todo_options);

            // Handle popup menu dismissal
            popupMenu.setOnDismissListener(menu -> {
                menuBackgroundOverlay.setVisibility(View.GONE);
            });

            // Handle menu item clicks
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.action_delete) {
                    deleteTodo();
                    return true;
                } else if (itemId == R.id.action_move_to) {
                    showCategorySelection();
                    return true;
                }

                return false;
            });

            popupMenu.show();
        });

        // Clicking overlay also dismisses menu
        menuBackgroundOverlay.setOnClickListener(v -> {
            menuBackgroundOverlay.setVisibility(View.GONE);
        });

        // Get ViewModel
        todoViewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);

        // In the if block where you check for todoId
        if (getArguments() != null && getArguments().getString("todoId") != null) {
            todoId = getArguments().getString("todoId");
            try {
                long id = Long.parseLong(todoId);
                todoViewModel.getTodoById(id).observe(getViewLifecycleOwner(), todo -> {
                    if (todo != null) {
                        etTitle.setText(todo.getTitle());
                        etContent.setText(todo.getContent());
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid todo ID", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).popBackStack();
            }
        }
    }

    // Add these methods to handle menu actions
    private void deleteTodo() {
        if (todoId != null) {
            try {
                long id = Long.parseLong(todoId);
                todoViewModel.getTodoById(id).observe(getViewLifecycleOwner(), todo -> {
                    if (todo != null) {
                        todoViewModel.delete(todo);
                        Toast.makeText(requireContext(), "Todo deleted", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid todo ID", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Just go back if we're creating a new todo
            Navigation.findNavController(requireView()).popBackStack();
        }
    }

    private void showCategorySelection() {
        // Only proceed if we have a valid todo
        if (todoId == null) {
            // We need to save the current todo first before assigning category
            autoSaveTodo();
            Toast.makeText(requireContext(), "Please save your todo first", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        // Convert the single todoId to the expected long array format
        long[] todoIds = new long[1];
        todoIds[0] = Long.parseLong(todoId);
        bundle.putLongArray("selectedTodoIds", todoIds);

        Navigation.findNavController(requireView())
                .navigate(R.id.action_todoFormFragment_to_categoryManagementFragment, bundle);
    }

    // New method to update todo category
    private void updateTodoCategory(String category) {
        try {
            long id = Long.parseLong(todoId);
            todoViewModel.getTodoById(id).observe(getViewLifecycleOwner(), todo -> {
                if (todo != null && getViewLifecycleOwner().getLifecycle().getCurrentState()
                        .isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
                    todoViewModel.updateTodoCategory(id, category);
                    Toast.makeText(requireContext(), "Todo moved to " + category, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid todo ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void autoSaveTodo() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        // If title is empty but content exists, use first few chars as title
        if (title.isEmpty() && !content.isEmpty()) {
            title = content.substring(0, Math.min(content.length(), 20)) + "...";
        }

        if (todoId == null) {
            // Create new Todo object
            Todo todo = new Todo();
            todo.setTitle(title);
            todo.setContent(content);
            todo.setTimestamp(System.currentTimeMillis()); // Last modified
            todo.setCreationDate(System.currentTimeMillis()); // Set creation date

            // Insert new todo
            todoViewModel.insert(todo);
        } else {
            // For existing todos, preserve the category by getting existing todo first
            try {
                long id = Long.parseLong(todoId);
                String finalTitle = title;
                todoViewModel.getTodoById(id).observe(getViewLifecycleOwner(), existingTodo -> {
                    if (existingTodo != null && getViewLifecycleOwner().getLifecycle().getCurrentState()
                            .isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
                        // Update only what changed, preserve the category
                        existingTodo.setTitle(finalTitle);
                        existingTodo.setContent(content);
                        existingTodo.setTimestamp(System.currentTimeMillis());

                        // Update with preserved category
                        todoViewModel.update(existingTodo);

                        // Navigate back after successful update
                        Navigation.findNavController(requireView()).popBackStack();
                    }
                });
                return; // Return early as navigation happens in the observer
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid todo ID", Toast.LENGTH_SHORT).show();
            }
        }

        // Navigate back only for new todos
        if (todoId == null) {
            Navigation.findNavController(requireView()).popBackStack();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Handle system back button to auto-save
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        String title = etTitle.getText().toString().trim();
                        String content = etContent.getText().toString().trim();

                        if (!title.isEmpty() || !content.isEmpty()) {
                            // Save only if there's content to save
                            autoSaveTodo();
                        } else {
                            // Just go back if empty
                            this.remove();
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });
    }
}