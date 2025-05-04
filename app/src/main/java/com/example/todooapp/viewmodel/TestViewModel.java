// app/src/main/java/com/example/todooapp/viewmodel/TestViewModel.java
package com.example.todooapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.todooapp.data.TodoDatabase;
import com.example.todooapp.data.dao.TestDao;
import com.example.todooapp.data.model.Test;

import java.util.List;

public class TestViewModel extends AndroidViewModel {
    private TestDao testDao;
    private LiveData<List<Test>> allTests;

    public TestViewModel(@NonNull Application application) {
        super(application);
        TodoDatabase db = TodoDatabase.getInstance(application);
        testDao = db.testDao();
        allTests = testDao.getAllTests();
    }

    public LiveData<List<Test>> getAllTests() {
        return allTests;
    }

    public void insert(Test test) {
        new Thread(() -> testDao.insert(test)).start();
    }

    public void update(Test test) {
        new Thread(() -> testDao.update(test)).start();
    }

    public void delete(Test test) {
        new Thread(() -> testDao.delete(test)).start();
    }
}