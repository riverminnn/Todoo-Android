package com.example.todooapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.todooapp.data.model.Todo;

import java.util.List;

@Dao
public interface TodoDao {
    @Insert
    long insert(Todo todo);

    @Update
    void update(Todo todo);

    @Delete
    void delete(Todo todo);

    // Update existing getAllTodos query to exclude hidden todos
    @Query("SELECT * FROM todos WHERE inTrash = 0 AND hidden = 0 ORDER BY timestamp DESC")
    LiveData<List<Todo>> getAllTodos();

    @Query("SELECT * FROM todos WHERE id = :id")
    LiveData<Todo> getTodoById(long id);

    @Query("SELECT * FROM todos WHERE id = :id")
    Todo getTodoByIdSync(long id);

    @Query("SELECT * FROM todos WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND inTrash = 0")
    LiveData<List<Todo>> searchTodos(String query);

    @Query("SELECT DISTINCT category FROM todos WHERE category IS NOT NULL AND category != ''")
    LiveData<List<String>> getAllUniqueCategories();

    @Query("SELECT * FROM todos WHERE category = :category AND inTrash = 0")
    LiveData<List<Todo>> getTodosByCategory(String category);

    @Query("SELECT * FROM todos WHERE category = :category AND inTrash = 0")
    List<Todo> getTodosByCategorySync(String category);

    // Trash-related queries
    @Query("SELECT * FROM todos WHERE inTrash = 1")
    LiveData<List<Todo>> getTrashTodos();

    @Query("DELETE FROM todos WHERE inTrash = 1")
    void emptyTrash();

    @Query("DELETE FROM todos WHERE inTrash = 1 AND trashDate < :expirationDate")
    void deleteExpiredTrashedTodos(long expirationDate);

    // Add to TodoDao.java
    @Query("SELECT * FROM todos WHERE hidden = 1 AND inTrash = 0")
    LiveData<List<Todo>> getHiddenTodos();

    // Add to TodoDao.java
    @Query("SELECT * FROM todos WHERE inTrash = 0 AND hidden = 0 ORDER BY timestamp DESC")
    List<Todo> getActiveTodosSync();
}