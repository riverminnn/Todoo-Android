// app/src/main/java/com/example/todooapp/adapter/TestAdapter.java
package com.example.todooapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.todooapp.R;
import com.example.todooapp.data.model.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder> {
    private List<Test> tests;
    private OnTestClickListener listener;

    public interface OnTestClickListener {
        void onTestClick(Test test);
        void onTestLongClick(Test test);
    }

    public TestAdapter(List<Test> tests, OnTestClickListener listener) {
        this.tests = tests;
        this.listener = listener;
    }

    public void setTests(List<Test> tests) {
        this.tests = tests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_test, parent, false);
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        Test test = tests.get(position);
        holder.tvId.setText("ID: " + test.getId());
        holder.tvName.setText(test.getName());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", test.getPrice()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(test.getCreatedDate()));
        holder.tvDate.setText(formattedDate);

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTestLongClick(test);
            }
        });

        holder.itemView.setOnClickListener(v -> listener.onTestClick(test));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onTestLongClick(test);
            return true;
        });

        if (test.getImagePath() != null && !test.getImagePath().isEmpty()) {
            holder.ivTestImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(test.getImagePath())
                    .into(holder.ivTestImage);
        } else {
            holder.ivTestImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return tests != null ? tests.size() : 0;
    }

    static class TestViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvName, tvDate, tvPrice;
        ImageButton btnDelete;
        ImageView ivTestImage;

        TestViewHolder(View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvName = itemView.findViewById(R.id.tvName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            ivTestImage = itemView.findViewById(R.id.ivTestImage);
        }
    }
}