package com.example.todooapp.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.todooapp.data.TodoDatabase;
import com.example.todooapp.data.dao.TodoDao;
import com.example.todooapp.data.model.Todo;
import com.example.todooapp.utils.shared.UserManager;
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

    // In TodoRepository constructor
    public TodoRepository(Context context) {
        TodoDatabase database = TodoDatabase.getInstance(context);
        todoDao = database.todoDao();
        allTodos = todoDao.getAllTodos();

        executorService = Executors.newSingleThreadExecutor();
        sharedPreferences = context.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE);

        // Get current user ID from UserManager
        UserManager userManager = new UserManager(context);
        String userId = userManager.getUserId();

        // Only initialize Firebase with user ID if we have one
        if (userId != null && !userId.isEmpty()) {
            // Structure data by user ID
            firebaseRef = FirebaseDatabase.getInstance().getReference("todos").child(userId);
        } else {
            // Create a temporary reference that will be updated when user logs in
            firebaseRef = FirebaseDatabase.getInstance().getReference("temp");
        }
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
                todo.setFirebaseKey(key);
                firebaseRef.child(key).setValue(todo);
                // Update Room with the Firebase key
                todoDao.update(todo);
            }
        });
    }

    public void update(Todo todo) {
        executorService.execute(() -> {
            // Debug log to verify inTrash status
            if (todo.isInTrash()) {
                Log.d("TodoRepository", "Updating todo with inTrash=true: " + todo.getId());
            }

            // Update in Room
            todoDao.update(todo);

            // Update in Firebase using the Firebase key
            String key = todo.getFirebaseKey();
            if (key != null) {
                firebaseRef.child(key).setValue(todo);
            } else {
                // Fallback if no Firebase key exists
                firebaseRef.child(String.valueOf(todo.getId())).setValue(todo);
            }
        });
    }

    // Fix in TodoRepository.java
    public void delete(Todo todo) {
        executorService.execute(() -> {
            // Delete from local database
            todoDao.delete(todo);

            // Delete from Firebase using the correct key
            String firebaseKey = todo.getFirebaseKey();
            if (firebaseKey != null && !firebaseKey.isEmpty()) {
                firebaseRef.child(firebaseKey).removeValue();
            }
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
        // Create a MediatorLiveData to combine Room categories with SharedPreferences
        // categories
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

    public Set<String> getCategoriesFromPrefs() {
        return sharedPreferences.getStringSet(PREF_CATEGORIES, new HashSet<>());
    }

    // Add to TodoRepository.java
    public LiveData<List<Todo>> getTrashTodos() {
        Log.d("TodoRepository", "Getting trash todos");
        return todoDao.getTrashTodos();
    }

    public void emptyTrash() {
        executorService.execute(() -> {
            todoDao.emptyTrash();
        });
    }

    // Add to TodoRepository.java
    public void deleteExpiredTrashedTodos() {
        executorService.execute(() -> {
            // Calculate date 30 days ago
            long thirtyDaysInMillis = 30 * 24 * 60 * 60 * 1000L;
            long expirationThreshold = System.currentTimeMillis() - thirtyDaysInMillis;

            // Delete trash items older than 30 days
            todoDao.deleteExpiredTrashedTodos(expirationThreshold);
        });
    }

    // Add to TodoRepository.java
    public LiveData<List<Todo>> getHiddenTodos() {
        return todoDao.getHiddenTodos();
    }

    public List<Todo> getActiveTodosSync() {
        return todoDao.getActiveTodosSync();
    }
}