// filepath: d:\University\Year4\HK2\AndroidNC\Project\Todoo\app\src\main\java\com\example\todooapp\data\TodoDatabase.java
package com.example.todooapp.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.todooapp.data.dao.TodoDao;
import com.example.todooapp.data.model.Todo;

@Database(entities = {Todo.class}, version = 9)
public abstract class TodoDatabase extends RoomDatabase {
    private static TodoDatabase instance;

    public abstract TodoDao todoDao();

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