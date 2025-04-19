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

    @Query("SELECT * FROM todos ORDER BY timestamp DESC")
    LiveData<List<Todo>> getAllTodos();

    @Query("SELECT * FROM todos WHERE category = :category ORDER BY timestamp DESC")
    LiveData<List<Todo>> getTodosByCategory(String category);

    @Query("SELECT * FROM todos WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    LiveData<List<Todo>> searchTodos(String query);

    @Query("SELECT * FROM todos WHERE id = :id")
    LiveData<Todo> getTodoById(long id);

    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    Todo getTodoByIdSync(long id);

    @Query("SELECT DISTINCT category FROM todos WHERE category IS NOT NULL AND category != '' ORDER BY category ASC")
    LiveData<List<String>> getAllUniqueCategories();

    @Query("SELECT * FROM todos WHERE category = :category")
    List<Todo> getTodosByCategorySync(String category);
}