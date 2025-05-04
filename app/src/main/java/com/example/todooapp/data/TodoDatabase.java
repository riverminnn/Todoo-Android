package com.example.todooapp.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.todooapp.data.model.Test;
import com.example.todooapp.data.model.Todo;
import com.example.todooapp.data.dao.TodoDao;
import com.example.todooapp.data.dao.TestDao;

@Database(entities = {Todo.class, Test.class}, version = 16, exportSchema = false)
public abstract class TodoDatabase extends RoomDatabase {
    private static TodoDatabase instance;

    public abstract TodoDao todoDao();
    public abstract TestDao testDao();

    public static synchronized TodoDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            TodoDatabase.class,
                            "todo_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }


}