package com.example.todooapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.todooapp.data.model.Test;

import java.util.List;

@Dao
public interface TestDao {
    @Insert
    long insert(Test test);

    @Update
    void update(Test test);

    @Delete
    void delete(Test test);

    @Query("SELECT * FROM tests ORDER BY createdDate DESC")
    LiveData<List<Test>> getAllTests();

    @Query("SELECT * FROM tests WHERE id = :id")
    LiveData<Test> getTestById(long id);
}