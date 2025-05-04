// app/src/main/java/com/example/todooapp/fragments/TestFragment.java
package com.example.todooapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooapp.R;
import com.example.todooapp.adapter.TestAdapter;
import com.example.todooapp.data.model.Test;
import com.example.todooapp.utils.shared.TodooDialogBuilder;
import com.example.todooapp.viewmodel.TestViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

public class TestFragment extends Fragment implements TestAdapter.OnTestClickListener {
    private TestViewModel viewModel;
    private TestAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.test, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TestViewModel.class);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TestAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fabAddTest);
        fab.setOnClickListener(v -> showAddDialog());

        viewModel.getAllTests().observe(getViewLifecycleOwner(),
                tests -> adapter.setTests(tests));
    }

    // TestFragment.java
    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_test_input, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);

        new TodooDialogBuilder(requireContext())
                .setTitle("Add New Test")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();

                    if (!name.isEmpty() && !priceStr.isEmpty()) {
                        try {
                            double price = Double.parseDouble(priceStr);
                            viewModel.insert(new Test(name, price));
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Test test) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_test_input, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);

        etName.setText(test.getName());
        etPrice.setText(String.format(Locale.getDefault(), "%.2f", test.getPrice()));

        new TodooDialogBuilder(requireContext())
                .setTitle("Edit Test")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();

                    if (!name.isEmpty() && !priceStr.isEmpty()) {
                        try {
                            double price = Double.parseDouble(priceStr);
                            test.setName(name);
                            test.setPrice(price);
                            viewModel.update(test);
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onTestClick(Test test) {
        showEditDialog(test);
    }

    @Override
    public void onTestLongClick(Test test) {
        new TodooDialogBuilder(requireContext())
                .setTitle("Delete Test")
                .setMessage("Are you sure you want to delete this test?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.delete(test);
                    Toast.makeText(requireContext(), "Test deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}