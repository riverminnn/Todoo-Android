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

import java.util.HashSet;
import java.util.List;
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
        holder.tvCategory.setText(todo.getCategory());
        holder.checkboxCompleted.setChecked(todo.isCompleted());

        // Handle selection mode appearance
        if (selectionMode) {
            holder.itemView.setBackgroundColor(selectedTodos.contains(todo) ?
                    Color.parseColor("#DDDDFF") : Color.TRANSPARENT);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Set click listeners
        holder.checkboxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() && listener != null) {
                listener.onTodoStatusChanged(todo, isChecked);
            }
        });

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(todo);
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
        TextView tvTitle, tvContent, tvCategory;
        CheckBox checkboxCompleted;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            checkboxCompleted = itemView.findViewById(R.id.checkboxCompleted);
        }
    }
}