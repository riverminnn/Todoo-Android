package com.example.todooapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.todooapp.data.model.Todo;
import com.example.todooapp.data.repository.TodoRepository;

import java.util.List;

public class TodoViewModel extends AndroidViewModel {
    private TodoRepository repository;
    private LiveData<List<Todo>> allTodos;

    public TodoViewModel(@NonNull Application application) {
        super(application);
        repository = new TodoRepository(application);
        allTodos = repository.getAllTodos();
    }

    public LiveData<List<Todo>> getAllTodos() {
        return allTodos;
    }

    public LiveData<List<Todo>> searchTodos(String query) {
        return repository.searchTodos(query);
    }

    public void insert(Todo todo) {
        repository.insert(todo);
    }

    public void update(Todo todo) {
        repository.update(todo);
    }

    public void delete(Todo todo) {
        repository.delete(todo);
    }

    public LiveData<Todo> getTodoById(long id) {
        return repository.getTodoById(id);
    }

    public void updateTodoCategory(long todoId, String category) {
        repository.updateTodoCategory(todoId, category);
    }

    public LiveData<List<String>> getAllUniqueCategories() {
        return repository.getAllUniqueCategories();
    }

    public void saveCategory(String category) {
        repository.saveCategory(category);
    }

    public void updateCategory(String oldName, String newName) {
        repository.updateCategory(oldName, newName);
    }

    public void deleteCategory(String category) {
        repository.deleteCategory(category);
    }

    public LiveData<List<Todo>> getTodosByCategory(String category) {
        return repository.getTodosByCategory(category);
    }
}