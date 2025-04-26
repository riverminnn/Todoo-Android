package com.example.todooapp.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.todooapp.R;
import com.example.todooapp.data.model.Todo;
import com.example.todooapp.data.repository.TodoRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TodoRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    class TodoRemoteViewsFactory implements RemoteViewsFactory {
        private Context context;
        private int appWidgetId;
        private List<Todo> todoList = new ArrayList<>();
        private TodoRepository repository;

        public TodoRemoteViewsFactory(Context context, Intent intent) {
            this.context = context;
            this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            repository = new TodoRepository(context.getApplicationContext());
        }

        @Override
        public void onCreate() {
            // Nothing to do here
        }

        @Override
        public void onDataSetChanged() {
            // This is called when the widget is updated
            // Get the latest todos synchronously for the widget
            todoList = repository.getActiveTodosSync();
        }

        @Override
        public void onDestroy() {
            todoList.clear();
        }

        @Override
        public int getCount() {
            return todoList.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= todoList.size()) {
                return null;
            }

            Todo todo = todoList.get(position);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.item_todo_widget);

            views.setTextViewText(R.id.widget_item_title, todo.getTitle());

            // Format date
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
            String formattedDate = sdf.format(new Date(todo.getTimestamp()));
            views.setTextViewText(R.id.widget_item_date, formattedDate);

            // Set up the click intent
            Bundle extras = new Bundle();
            extras.putString("todoId", String.valueOf(todo.getId()));
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            views.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null; // Use default loading view
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position < todoList.size() ? todoList.get(position).getId() : position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}