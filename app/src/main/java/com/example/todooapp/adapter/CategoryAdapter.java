package com.example.todooapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooapp.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<String> categories;
    private CategoryClickListener listener;

    private boolean selectionMode = false;
    private Set<String> selectedCategories = new HashSet<>();

    public interface CategoryClickListener {
        void onCategoryClick(String category);
        void onSelectionModeChanged(boolean active, Set<String> selectedItems);
    }



    public CategoryAdapter(List<String> categories, CategoryClickListener listener) {
        this.categories = categories;
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
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.tvCategory.setText(category);

        // Handle selection mode appearance
        if (selectionMode) {
            holder.itemView.setBackgroundColor(selectedCategories.contains(category) ?
                    Color.parseColor("#DDDDFF") : Color.TRANSPARENT);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(category);
            } else if (listener != null) {
                listener.onCategoryClick(category);
            }
        });

        // Long press to enter selection mode
        holder.itemView.setOnLongClickListener(v -> {
            if (!selectionMode) {
                selectionMode = true;
                toggleSelection(category);
                if (listener != null) {
                    listener.onSelectionModeChanged(true, selectedCategories);
                }
                notifyDataSetChanged();
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
        TextView tvCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(android.R.id.text1);
        }
    }
}