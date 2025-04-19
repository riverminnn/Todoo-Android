package com.example.todooapp.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.todooapp.data.TodoDatabase;
import com.example.todooapp.data.dao.TodoDao;
import com.example.todooapp.data.model.Todo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TodoRepository {
    private TodoDao todoDao;
    private LiveData<List<Todo>> allTodos;
    private DatabaseReference firebaseRef;
    private ExecutorService executorService;
    private SharedPreferences sharedPreferences;
    private static final String PREF_CATEGORIES = "categories";

    public TodoRepository(Application application) {
        TodoDatabase database = TodoDatabase.getInstance(application);
        todoDao = database.todoDao();
        allTodos = todoDao.getAllTodos();
        firebaseRef = FirebaseDatabase.getInstance().getReference("todos");
        executorService = Executors.newSingleThreadExecutor();
        sharedPreferences = application.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE);
    }

    public LiveData<List<Todo>> getAllTodos() {
        return allTodos;
    }

    public LiveData<List<Todo>> getTodosByCategory(String category) {
        return todoDao.getTodosByCategory(category);
    }

    public LiveData<List<Todo>> searchTodos(String query) {
        return todoDao.searchTodos(query);
    }

    public void insert(Todo todo) {
        executorService.execute(() -> {
            // Insert to Room
            long id = todoDao.insert(todo);
            todo.setId(id);

            // Insert to Firebase
            String key = firebaseRef.push().getKey();
            if (key != null) {
                firebaseRef.child(key).setValue(todo);
            }
        });
    }

    public void update(Todo todo) {
        executorService.execute(() -> {
            // Update in Room
            todoDao.update(todo);

            // Update in Firebase
            firebaseRef.child(String.valueOf(todo.getId())).setValue(todo);
        });
    }

    public void delete(Todo todo) {
        executorService.execute(() -> {
            // Delete from Room
            todoDao.delete(todo);

            // Delete from Firebase
            firebaseRef.child(String.valueOf(todo.getId())).removeValue();
        });
    }

    public LiveData<Todo> getTodoById(long id) {
        return todoDao.getTodoById(id);
    }

    public void updateTodoCategory(long todoId, String category) {
        executorService.execute(() -> {
            Todo todo = todoDao.getTodoByIdSync(todoId);
            if (todo != null) {
                todo.setCategory(category);
                todoDao.update(todo);

                // Update in Firebase
                firebaseRef.child(String.valueOf(todo.getId())).setValue(todo);
            }
        });
    }

    public LiveData<List<String>> getAllUniqueCategories() {
        // Create a MediatorLiveData to combine Room categories with SharedPreferences categories
        MediatorLiveData<List<String>> result = new MediatorLiveData<>();

        // Add Room database source
        LiveData<List<String>> dbCategories = todoDao.getAllUniqueCategories();
        result.addSource(dbCategories, dbCats -> {
            Set<String> allCategories = new HashSet<>();

            // Add categories from Room
            if (dbCats != null) {
                allCategories.addAll(dbCats);
            }

            // Add categories from SharedPreferences
            Set<String> savedCategories = sharedPreferences.getStringSet(PREF_CATEGORIES, new HashSet<>());
            allCategories.addAll(savedCategories);

            // Sort categories alphabetically
            List<String> sortedList = new ArrayList<>(allCategories);
            Collections.sort(sortedList);

            result.setValue(sortedList);
        });

        return result;
    }

    // Add a method to save a category even if it has no todos
    public void saveCategory(String category) {
        executorService.execute(() -> {
            // Save category to Firebase under a categories node
            DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference("categories");
            categoriesRef.child(category.replace(".", ",")).setValue(true);

            // Also save to SharedPreferences
            Set<String> savedCategories = sharedPreferences.getStringSet(PREF_CATEGORIES, new HashSet<>());
            Set<String> updatedCategories = new HashSet<>(savedCategories);
            updatedCategories.add(category);
            sharedPreferences.edit().putStringSet(PREF_CATEGORIES, updatedCategories).apply();
        });
    }

    // Add method to update a category name
    public void updateCategory(String oldName, String newName) {
        executorService.execute(() -> {
            // Update in Firebase
            DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference("categories");
            categoriesRef.child(oldName.replace(".", ",")).removeValue();
            categoriesRef.child(newName.replace(".", ",")).setValue(true);

            // Update in SharedPreferences
            Set<String> savedCategories = sharedPreferences.getStringSet(PREF_CATEGORIES, new HashSet<>());
            Set<String> updatedCategories = new HashSet<>(savedCategories);
            updatedCategories.remove(oldName);
            updatedCategories.add(newName);
            sharedPreferences.edit().putStringSet(PREF_CATEGORIES, updatedCategories).apply();

            // Update any todos that use this category
            List<Todo> todosToUpdate = todoDao.getTodosByCategorySync(oldName);
            for (Todo todo : todosToUpdate) {
                todo.setCategory(newName);
                todoDao.update(todo);

                // Also update in Firebase
                firebaseRef.child(String.valueOf(todo.getId())).setValue(todo);
            }
        });
    }

    // Add method to delete a category
    public void deleteCategory(String category) {
        executorService.execute(() -> {
            // Delete from Firebase
            DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference("categories");
            categoriesRef.child(category.replace(".", ",")).removeValue();

            // Delete from SharedPreferences
            Set<String> savedCategories = sharedPreferences.getStringSet(PREF_CATEGORIES, new HashSet<>());
            Set<String> updatedCategories = new HashSet<>(savedCategories);
            updatedCategories.remove(category);
            sharedPreferences.edit().putStringSet(PREF_CATEGORIES, updatedCategories).apply();

            // Set category to empty for any todos that use this category
            List<Todo> todosToUpdate = todoDao.getTodosByCategorySync(category);
            for (Todo todo : todosToUpdate) {
                todo.setCategory("");
                todoDao.update(todo);

                // Also update in Firebase
                firebaseRef.child(String.valueOf(todo.getId())).setValue(todo);
            }
        });
    }
}