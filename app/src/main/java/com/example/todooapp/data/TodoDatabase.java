package com.example.todooapp.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.todooapp.data.model.Todo;
import com.example.todooapp.data.dao.TodoDao;

import msv21a100100107.nguyenquangha.SinhVien;
import msv21a100100107.nguyenquangha.SinhVienDao;

@Database(entities = {Todo.class, SinhVien.class}, version = 18, exportSchema = false)
public abstract class TodoDatabase extends RoomDatabase {
    private static TodoDatabase instance;

    public abstract TodoDao todoDao();
    public abstract SinhVienDao sinhVienDao();

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