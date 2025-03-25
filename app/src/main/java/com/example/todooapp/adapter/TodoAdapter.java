package com.example.todooapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooapp.R;
import com.example.todooapp.model.TodoModel;

import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private List<TodoModel> todoModelList;

    public TodoAdapter(List<TodoModel> todoModelList){
        this.todoModelList = todoModelList;
    }

    @NonNull
    @Override
    public TodoAdapter.TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoAdapter.TodoViewHolder holder, int position) {
        TodoModel todoModel = todoModelList.get(position);
        holder.tvId.setText("ID: " + todoModel.getId());
        holder.tvName.setText("Name: " + todoModel.getName());
        holder.tvDescription.setText("Description: " + todoModel.getDescription());
    }

    @Override
    public int getItemCount() {
        return todoModelList.size();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvName, tvDescription;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
