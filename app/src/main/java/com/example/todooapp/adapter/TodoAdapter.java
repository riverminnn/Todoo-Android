package com.example.todooapp.adapter;

import android.graphics.Color;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private final List<Todo> todoList;
    private final OnTodoClickListener listener;
    private boolean selectionMode = false;
    private boolean trashMode = false;
    private final Set<Todo> selectedTodos = new HashSet<>();

    public interface OnTodoClickListener {
        void onTodoClick(Todo todo);
        void onTodoDelete(Todo todo);
        void onTodoStatusChanged(Todo todo, boolean isCompleted);
        void onSelectionModeChanged(boolean active, Set<Todo> selectedItems);
    }

    public TodoAdapter(List<Todo> todoList, OnTodoClickListener listener) {
        this.todoList = todoList;
        this.listener = listener;
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

    public void setTrashMode(boolean trashMode) {
        this.trashMode = trashMode;
        notifyDataSetChanged();
    }

    public void selectAll(Set<Todo> allTodos) {
        selectedTodos.clear();
        selectedTodos.addAll(allTodos);
        notifyDataSetChanged();
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
        bindTodoContent(holder, todo);

        if (trashMode) {
            bindTrashMode(holder, todo);
        } else {
            bindNormalMode(holder, todo);
        }
    }

    private void bindTodoContent(TodoViewHolder holder, Todo todo) {
        // Clean the title by removing HTML tags and normalizing spaces
        String title = todo.getTitle()
                .replaceAll("<.*?>", " ") // Replace all HTML tags with space
                .replaceAll("\\s{2,}", " ") // Replace multiple spaces with single space
                .trim();

        // Limit title length to prevent second line
        int maxTitleLength = 50;
        if (title.length() > maxTitleLength) {
            title = title.substring(0, maxTitleLength) + "...";
        }

        // Configure TextView to display as a single line
        holder.tvTitle.setText(title);
        holder.tvTitle.setSingleLine(true);
        holder.tvTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);

        String htmlContent = todo.getContent();
        if (htmlContent != null && !htmlContent.trim().isEmpty()) {
            // Split the content into "lines" based on HTML tags
            String[] lines = htmlContent.split("(?=<todoo-(bullet|checkbox|heading))");
            String firstLine = "";

            // Extract the first line by taking the content within the first tag (or plain text)
            if (lines.length > 0) {
                String firstSegment = lines[0].trim();
                // Match the content within the first tag (e.g., <todoo-bullet>1</todoo-bullet>)
                Pattern contentPattern = Pattern.compile("<todoo-(bullet|checkbox|heading)(?:\\s+[^>]*)?>(.*?)</todoo-\\1>", Pattern.DOTALL);
                Matcher matcher = contentPattern.matcher(firstSegment);
                if (matcher.find()) {
                    // Extract the content (e.g., "1 <b>123456</b>")
                    String contentWithHtml = matcher.group(2).trim();
                    // Convert HTML content to plain text, preserving the text inside tags like <b>, <i>
                    Spanned spannedContent = HtmlCompat.fromHtml(contentWithHtml, HtmlCompat.FROM_HTML_MODE_LEGACY);
                    firstLine = spannedContent.toString().trim();
                } else {
                    // If no tags, treat the segment as plain text and clean it
                    firstLine = firstSegment
                            .replaceAll("<.*?>", " ")
                            .replaceAll("\\s{2,}", " ")
                            .trim();
                }
            }

            // Limit content length to avoid pushing date off screen
            int maxLength = 150;
            if (firstLine.length() > maxLength) {
                firstLine = firstLine.substring(0, maxLength) + "...";
            }

            // Configure TextView to display as a single line
            holder.tvContent.setText(firstLine);
            holder.tvContent.setSingleLine(true);
            holder.tvContent.setEllipsize(android.text.TextUtils.TruncateAt.END);
        } else {
            holder.tvContent.setText("No text");
            holder.tvContent.setSingleLine(true);
            holder.tvContent.setEllipsize(android.text.TextUtils.TruncateAt.END);
        }

        TextView tvPinIcon = holder.tvPinIcon;
        if (todo.isPinned()) {
            tvPinIcon.setVisibility(View.VISIBLE);
        } else {
            tvPinIcon.setVisibility(View.GONE);
        }
    }
    private void bindTrashMode(TodoViewHolder holder, Todo todo) {
        // Display trash date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(todo.getTrashDate()));
        holder.tvDate.setText("Trashed: " + formattedDate);

        // Apply trash mode styling
        MaterialCardView cardView = (MaterialCardView) holder.itemView;
        cardView.setStrokeWidth(2);
        cardView.setStrokeColor(Color.parseColor("#FFE0E0"));
        holder.checkboxSelected.setVisibility(View.GONE);

        // Set click listener for trash mode
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTodoClick(todo);
            }
        });

        // Disable long press in trash mode
        holder.itemView.setOnLongClickListener(null);
    }

    private void bindNormalMode(TodoViewHolder holder, Todo todo) {
        // Display creation date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(todo.getCreationDate()));
        holder.tvDate.setText("Created: " + formattedDate);

        // Reset styling for normal mode
        MaterialCardView cardView = (MaterialCardView) holder.itemView;
        cardView.setStrokeWidth(0);

        // Handle selection mode
        CheckBox checkboxSelected = holder.checkboxSelected;
        if (selectionMode) {
            setupSelectionMode(holder, todo, checkboxSelected, cardView);
        } else {
            setupNormalMode(holder, checkboxSelected, cardView);
        }

        // Set click listeners for normal mode
        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                if (v.getId() != R.id.checkboxSelected) {
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
    }

    private void setupSelectionMode(TodoViewHolder holder, Todo todo, CheckBox checkboxSelected, MaterialCardView cardView) {
        checkboxSelected.setVisibility(View.VISIBLE);
        checkboxSelected.setChecked(selectedTodos.contains(todo));

        cardView.setCardBackgroundColor(selectedTodos.contains(todo) ? Color.parseColor("#F0F0F0") : Color.WHITE);

        checkboxSelected.setOnClickListener(v -> toggleSelection(todo));
    }

    private void setupNormalMode(TodoViewHolder holder, CheckBox checkboxSelected, MaterialCardView cardView) {
        checkboxSelected.setVisibility(View.GONE);
        cardView.setCardBackgroundColor(Color.WHITE);
        checkboxSelected.setOnClickListener(null);
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
        TextView tvTitle, tvContent, tvDate, tvPinIcon;
        CheckBox checkboxSelected;

        TodoViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPinIcon = itemView.findViewById(R.id.tvPinIcon);
            checkboxSelected = itemView.findViewById(R.id.checkboxSelected);
        }
    }
}