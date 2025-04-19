// New file to create:
package com.example.todooapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
    private EditText etTitle, etContent, etCategory;
    private CheckBox cbCompleted;
    private Button btnSave;
    private TodoViewModel todoViewModel;
    private String todoId = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        etTitle = view.findViewById(R.id.etTitle);
        etContent = view.findViewById(R.id.etContent);
        etCategory = view.findViewById(R.id.etCategory);
        cbCompleted = view.findViewById(R.id.cbCompleted);
        btnSave = view.findViewById(R.id.btnSave);

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
                        etCategory.setText(todo.getCategory());
                        cbCompleted.setChecked(todo.isCompleted());
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid todo ID", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).popBackStack();
            }
        }

        btnSave.setOnClickListener(v -> saveTodo());
    }

    private void saveTodo() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        boolean completed = cbCompleted.isChecked();

        // In saveTodo() method - field validation
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new Todo object
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setContent(content);
        todo.setCategory(category);
        todo.setCompleted(completed);
        todo.setTimestamp(System.currentTimeMillis());

        // In saveTodo() method - handling save/update
        if (todoId == null) {
            // Insert new todo
            todoViewModel.insert(todo);
            Toast.makeText(requireContext(), "Todo added successfully", Toast.LENGTH_SHORT).show();
        } else {
            // Update existing todo
            long id = Long.parseLong(todoId);
            todo.setId(id);
            todoViewModel.update(todo);
            Toast.makeText(requireContext(), "Todo updated successfully", Toast.LENGTH_SHORT).show();
        }

        // Navigate back
        Navigation.findNavController(requireView()).popBackStack();
    }
}