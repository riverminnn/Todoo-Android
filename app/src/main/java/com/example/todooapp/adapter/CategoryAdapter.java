package com.example.todooapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooapp.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<String> categories;
    private Map<String, Integer> categoryCounts;
    private CategoryClickListener listener;

    private boolean selectionMode = false;
    private Set<String> selectedCategories = new HashSet<>();

    // Add this method to the CategoryAdapter class
    public List<String> getCategories() {
        return categories;
    }

    public interface CategoryClickListener {
        void onCategoryClick(String category);
        void onSelectionModeChanged(boolean active, Set<String> selectedItems);
    }

    public CategoryAdapter(List<String> categories, Map<String, Integer> categoryCounts, CategoryClickListener listener) {
        this.categories = categories;
        this.categoryCounts = categoryCounts != null ? categoryCounts : new HashMap<>();
        this.listener = listener;
    }

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
        if (!selectionMode) {
            selectedCategories.clear();
        }
        notifyDataSetChanged();
    }
    public boolean isSelectionMode() {
        return selectionMode;
    }

    public Set<String> getSelectedCategories() {
        return selectedCategories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.tvCategoryName.setText(category);

        // Set the count or default to 0
        int count = categoryCounts.getOrDefault(category, 0);
        holder.tvTodoCount.setText(String.valueOf(count));

        // Handle selection mode
        if (selectionMode) {
            holder.checkboxSelected.setVisibility(View.VISIBLE);
            holder.checkboxSelected.setChecked(selectedCategories.contains(category));
            holder.tvTodoCount.setVisibility(View.GONE);

            // Add direct click listener to the checkbox
            holder.checkboxSelected.setOnClickListener(v -> {
                toggleSelection(category);
            });
        } else {
            holder.checkboxSelected.setVisibility(View.GONE);
            holder.tvTodoCount.setVisibility(View.VISIBLE);
            // Remove click listener when not in selection mode
            holder.checkboxSelected.setOnClickListener(null);
        }

        // Set click listeners for the item
        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(category);
            } else if (listener != null) {
                listener.onCategoryClick(category);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!selectionMode) {
                selectionMode = true;
                toggleSelection(category);
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onSelectionModeChanged(true, selectedCategories);
                }
                return true;
            }
            return false;
        });
    }

    private void toggleSelection(String category) {
        if (selectedCategories.contains(category)) {
            selectedCategories.remove(category);
        } else {
            selectedCategories.add(category);
        }

        if (listener != null) {
            listener.onSelectionModeChanged(selectionMode, selectedCategories);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvTodoCount;
        CheckBox checkboxSelected;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvTodoCount = itemView.findViewById(R.id.tvTodoCount);
            checkboxSelected = itemView.findViewById(R.id.checkboxSelected);
        }
    }
}