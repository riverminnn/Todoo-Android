package com.example.todooapp.utils;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.Navigation;

import com.example.todooapp.data.model.Todo;
import com.example.todooapp.viewmodel.TodoViewModel;

public class TodoSaveManager {
    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    private final TodoViewModel todoViewModel;
    private final EditText etTitle;
    private final EditText etContent;
    private final UndoRedoManager undoRedoManager;
    private String todoId;

    public TodoSaveManager(
            Context context,
            LifecycleOwner lifecycleOwner,
            TodoViewModel todoViewModel,
            EditText etTitle,
            EditText etContent,
            UndoRedoManager undoRedoManager) {

        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.todoViewModel = todoViewModel;
        this.etTitle = etTitle;
        this.etContent = etContent;
        this.undoRedoManager = undoRedoManager;
    }

    public void setTodoId(String todoId) {
        this.todoId = todoId;
    }

    public String getTodoId() {
        return todoId;
    }

    public boolean hasContent() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        return !title.isEmpty() || !content.isEmpty();
    }

    public void autoSaveTodo() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() && !content.isEmpty()) {
            title = content.substring(0, Math.min(content.length(), 20)) + "...";
        }

        undoRedoManager.clearHistory();

        if (todoId == null) {
            Todo todo = new Todo();
            todo.setTitle(title);
            todo.setContent(content);
            todo.setTimestamp(System.currentTimeMillis());
            todo.setCreationDate(System.currentTimeMillis());
            todoViewModel.insert(todo);
        } else {
            try {
                long id = Long.parseLong(todoId);
                String finalTitle = title;
                todoViewModel.getTodoById(id).observe(lifecycleOwner, existingTodo -> {
                    if (existingTodo != null) {
                        existingTodo.setTitle(finalTitle);
                        existingTodo.setContent(content);
                        existingTodo.setTimestamp(System.currentTimeMillis());
                        todoViewModel.update(existingTodo);
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid todo ID", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void deleteTodo() {
        if (todoId != null) {
            try {
                long id = Long.parseLong(todoId);
                todoViewModel.getTodoById(id).observe(lifecycleOwner, todo -> {
                    if (todo != null) {
                        todoViewModel.delete(todo);
                        Toast.makeText(context, "Todo deleted", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid todo ID", Toast.LENGTH_SHORT).show();
            }
        }
    }
}