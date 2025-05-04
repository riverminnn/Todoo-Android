// app/src/main/java/com/example/todooapp/fragments/TestFragment.java
package com.example.todooapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

    private static final int PICK_IMAGE_REQUEST = 1;
    private String selectedImagePath;
    private ImageView ivPreview;

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
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        ivPreview = dialogView.findViewById(R.id.ivPreview);

        btnSelectImage.setOnClickListener(v -> openImagePicker());

        AlertDialog dialog = new TodooDialogBuilder(requireContext())
                .setTitle("Add New Test")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String priceStr = etPrice.getText().toString().trim();

                if (!name.isEmpty() && !priceStr.isEmpty()) {
                    try {
                        double price = Double.parseDouble(priceStr);
                        Test test = new Test(name, price);
                        test.setImagePath(selectedImagePath);
                        viewModel.insert(test);
                        dialog.dismiss();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            selectedImagePath = getPathFromUri(imageUri);
            if (ivPreview != null) {
                ivPreview.setImageURI(imageUri);
                ivPreview.setVisibility(View.VISIBLE);
            }
        }
    }

    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }

    private void showEditDialog(Test test) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_test_input, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        ivPreview = dialogView.findViewById(R.id.ivPreview);

        etName.setText(test.getName());
        etPrice.setText(String.format(Locale.getDefault(), "%.2f", test.getPrice()));
        selectedImagePath = test.getImagePath();

        // Show existing image in preview
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            ivPreview.setImageURI(Uri.parse(selectedImagePath));
            ivPreview.setVisibility(View.VISIBLE);
        }

        btnSelectImage.setOnClickListener(v -> openImagePicker());

        AlertDialog dialog = new TodooDialogBuilder(requireContext())
                .setTitle("Edit Test")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String priceStr = etPrice.getText().toString().trim();

                if (!name.isEmpty() && !priceStr.isEmpty()) {
                    try {
                        double price = Double.parseDouble(priceStr);
                        test.setName(name);
                        test.setPrice(price);
                        test.setImagePath(selectedImagePath);
                        viewModel.update(test);
                        dialog.dismiss();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
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