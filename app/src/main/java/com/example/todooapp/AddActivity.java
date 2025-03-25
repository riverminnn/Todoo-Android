package com.example.todooapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todooapp.model.TodoModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddActivity extends AppCompatActivity {
    private EditText etName, etDescription;
    private Button btnAdd;

    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        btnAdd = findViewById(R.id.btnAdd);

        databaseReference = FirebaseDatabase.getInstance().getReference("Todos");

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTodo();
            }
        });
    }

    private void addTodo() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please enter both name and description", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Generate a unique ID using Firebase push()
            String todoId = databaseReference.push().getKey();
            if (todoId == null) throw new Exception("Failed to generate unique ID");

            // Create a new ToDo object
            TodoModel newTodo = new TodoModel(todoId, name, description);

            // Save to Firebase using the generated key
            databaseReference.child(todoId).setValue(newTodo)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddActivity.this, "Added Successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close activity after adding
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Toast.makeText(AddActivity.this, "Unexpected Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}