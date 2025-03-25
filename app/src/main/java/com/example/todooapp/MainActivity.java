package com.example.todooapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooapp.adapter.TodoAdapter;
import com.example.todooapp.model.TodoModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TodoAdapter todoAdapter;
    private List<TodoModel> todoModelList;

    private DatabaseReference databaseReference;

    private FloatingActionButton btnAddActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerView = findViewById(R.id.recycleView);
        btnAddActivity = findViewById(R.id.btnAddActivity);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        todoModelList = new ArrayList<>();
        todoAdapter = new TodoAdapter(todoModelList);
        recyclerView.setAdapter(todoAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Todos");

        int duration = Toast.LENGTH_SHORT;
        var toast = Toast.makeText(this, "haha", duration);
        toast.show();

        btnAddActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddActivity.class));
            }
        });

        fetchTodos();
    }

    private void fetchTodos() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                todoModelList.clear();
                for (DataSnapshot data: snapshot.getChildren()){
                    TodoModel todoModel = data.getValue(TodoModel.class);
                    if (todoModel != null) {
                        todoModelList.add(todoModel);
                    }
                }
                todoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}