package com.example.todooapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooapp.R;
import com.example.todooapp.data.model.Todo;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private List<Todo> todoList;
    private OnTodoClickListener listener;

    private boolean selectionMode = false;
    private Set<Todo> selectedTodos = new HashSet<>();

    public interface OnTodoClickListener {
        void onTodoClick(Todo todo);
        void onTodoDelete(Todo todo);
        void onTodoStatusChanged(Todo todo, boolean isCompleted);
        void onSelectionModeChanged(boolean active, Set<Todo> selectedItems);
    }

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
        if (!selectionMode) {
            selectedTodos.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public Set<Todo> getSelectedTodos() {
        return selectedTodos;
    }

    public TodoAdapter(List<Todo> todoList, OnTodoClickListener listener) {
        this.todoList = todoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todoList.get(position);
        holder.tvTitle.setText(todo.getTitle());
        holder.tvContent.setText(todo.getContent());


        // Check if content is empty or null and display "No text" if needed
        String content = todo.getContent();
        if (content == null || content.trim().isEmpty()) {
            holder.tvContent.setText("No text");
        } else {
            holder.tvContent.setText(content);
        }


        // Access the checkbox and card view
        CheckBox checkboxSelected = holder.checkboxSelected;
        MaterialCardView cardView = (MaterialCardView) holder.itemView;

        // Handle selection mode appearance
        if (selectionMode) {
            // Show checkbox in selection mode
            checkboxSelected.setVisibility(View.VISIBLE);
            checkboxSelected.setChecked(selectedTodos.contains(todo));

            if (selectedTodos.contains(todo)) {
                // Darken the card when selected
                cardView.setCardBackgroundColor(Color.parseColor("#F0F0F0"));
            } else {
                // Normal color when not selected
                cardView.setCardBackgroundColor(Color.WHITE);
            }

            // Add specific click listener for checkbox
            checkboxSelected.setOnClickListener(v -> {
                toggleSelection(todo);
            });
        } else {
            // Hide checkbox when not in selection mode
            checkboxSelected.setVisibility(View.GONE);
            cardView.setCardBackgroundColor(Color.WHITE);
            // Remove checkbox click listener when not in selection mode
            checkboxSelected.setOnClickListener(null);
        }

        // Set click listeners for the item
        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                // Only toggle if the click was not on the checkbox
                if (!(v.getId() == R.id.checkboxSelected)) {
                    toggleSelection(todo);
                }
            } else if (listener != null) {
                listener.onTodoClick(todo);
            }
        });

        // Long press to enter selection mode
        holder.itemView.setOnLongClickListener(v -> {
            if (!selectionMode) {
                selectionMode = true;
                toggleSelection(todo);
                if (listener != null) {
                    listener.onSelectionModeChanged(true, selectedTodos);
                }
                notifyDataSetChanged();
                return true;
            }
            return false;
        });

        // Format and display the creation date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(todo.getCreationDate()));
        holder.tvDate.setText("Created: " + formattedDate);
    }

    private void toggleSelection(Todo todo) {
        if (selectedTodos.contains(todo)) {
            selectedTodos.remove(todo);
        } else {
            selectedTodos.add(todo);
        }

        if (listener != null) {
            listener.onSelectionModeChanged(selectionMode, selectedTodos);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate;
        CheckBox checkboxSelected;

        TodoViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvDate = itemView.findViewById(R.id.tvDate);
            checkboxSelected = itemView.findViewById(R.id.checkboxSelected);
        }
    }

    public void selectAll(Set<Todo> allTodos) {
        selectedTodos.clear();
        selectedTodos.addAll(allTodos);
        notifyDataSetChanged();
    }
}