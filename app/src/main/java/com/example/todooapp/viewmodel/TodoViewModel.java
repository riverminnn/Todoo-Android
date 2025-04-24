package com.example.todooapp.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.todooapp.data.model.Todo;
import com.example.todooapp.data.repository.TodoRepository;
import com.example.todooapp.utils.TodoSortOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TodoViewModel extends AndroidViewModel {
    private TodoRepository repository;
    private LiveData<List<Todo>> allTodos;

    private MutableLiveData<TodoSortOption> currentSortOption = new MutableLiveData<>(TodoSortOption.MODIFIED_DESC);
    private MediatorLiveData<List<Todo>> sortedTodos = new MediatorLiveData<>();

    public TodoViewModel(@NonNull Application application) {
        super(application);
        repository = new TodoRepository(application);
        allTodos = repository.getAllTodos();

        // Setup sorted todos mediator
        sortedTodos.addSource(allTodos, todos -> sortTodos(todos, currentSortOption.getValue()));
        sortedTodos.addSource(currentSortOption, option -> sortTodos(allTodos.getValue(), option));
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

    public boolean addCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return false;
        }

        // Normalize the category name
        String trimmedCategory = category.trim();

        // Check if category already exists
        if (categoryExists(trimmedCategory)) {
            return false;
        }

        // Save the new category
        repository.saveCategory(trimmedCategory);
        return true;
    }

    public boolean categoryExists(String category) {
        if (category == null || category.trim().isEmpty()) {
            return false;
        }

        // Get the current categories synchronously
        Set<String> savedCategories = ((TodoRepository)repository).getCategoriesFromPrefs();
        return savedCategories.contains(category.trim());
    }

    public LiveData<List<Todo>> getSortedTodos() {
        return sortedTodos;
    }

    public LiveData<TodoSortOption> getCurrentSortOption() {
        return currentSortOption;
    }

    public void setSortOption(TodoSortOption option) {
        currentSortOption.setValue(option);

        // Save selected option to preferences
        SharedPreferences prefs = getApplication().getSharedPreferences("todo_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("sort_option", option.name()).apply();
    }

    private void sortTodos(List<Todo> todos, TodoSortOption option) {
        if (todos == null || option == null) return;

        List<Todo> sortedList = new ArrayList<>(todos);

        switch (option) {
            case MODIFIED_DESC:
                Collections.sort(sortedList, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                break;
            case MODIFIED_ASC:
                Collections.sort(sortedList, (t1, t2) -> Long.compare(t1.getTimestamp(), t2.getTimestamp()));
                break;
            case CREATED_DESC:
                Collections.sort(sortedList, (t1, t2) -> Long.compare(t2.getCreationDate(), t1.getCreationDate()));
                break;
            case CREATED_ASC:
                Collections.sort(sortedList, (t1, t2) -> Long.compare(t1.getCreationDate(), t2.getCreationDate()));
                break;
            case ALPHABETICAL_ASC:
                Collections.sort(sortedList, (t1, t2) -> t1.getTitle().compareToIgnoreCase(t2.getTitle()));
                break;
            case ALPHABETICAL_DESC:
                Collections.sort(sortedList, (t1, t2) -> t2.getTitle().compareToIgnoreCase(t1.getTitle()));
                break;
        }

        sortedTodos.setValue(sortedList);
    }
}