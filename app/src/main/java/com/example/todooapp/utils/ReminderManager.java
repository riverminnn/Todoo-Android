package com.example.todooapp.utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;

import com.example.todooapp.data.model.Todo;
import com.example.todooapp.viewmodel.TodoViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

public class ReminderManager {
    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    private final TodoViewModel todoViewModel;

    public ReminderManager(Context context, LifecycleOwner lifecycleOwner, TodoViewModel todoViewModel) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.todoViewModel = todoViewModel;
    }

    public void showReminderDialog(String todoId, Consumer<Todo> onNewTodoCreate) {
        // Create a calendar instance with current date/time
        final Calendar calendar = Calendar.getInstance();

        // Create DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    // Set date to calendar
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // After date is selected, show time picker
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            context,
                            (view1, hourOfDay, minute) -> {
                                // Set time to calendar
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);

                                // Save reminder time to todo
                                setReminder(todoId, calendar.getTimeInMillis(), onNewTodoCreate);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void setReminder(String todoId, long reminderTime, Consumer<Todo> onNewTodoCreate) {
        // Make sure we have a valid todoId
        if (todoId == null) {
            // Create a new Todo with reminder
            Todo newTodo = new Todo();
            newTodo.setTimestamp(System.currentTimeMillis());
            newTodo.setCreationDate(System.currentTimeMillis());
            newTodo.setHasReminder(true);
            newTodo.setReminderTime(reminderTime);

            // Pass the Todo back for customization and saving
            onNewTodoCreate.accept(newTodo);
            return;
        }

        try {
            long id = Long.parseLong(todoId);
            // Use a one-time observer pattern to avoid multiple toast messages
            final boolean[] handled = {false};
            todoViewModel.getTodoById(id).observe(lifecycleOwner, todo -> {
                if (todo != null && !handled[0]) {
                    handled[0] = true;  // Mark as handled

                    todo.setHasReminder(true);
                    todo.setReminderTime(reminderTime);
                    todoViewModel.update(todo);

                    // Schedule the reminder
                    ReminderHelper.scheduleReminder(context, todo);

                    // Format date for user feedback
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
                    String formattedDate = sdf.format(new Date(reminderTime));
                    Toast.makeText(context, "Reminder set for " + formattedDate, Toast.LENGTH_LONG).show();

                    // Remove the observer
                    todoViewModel.getTodoById(id).removeObservers(lifecycleOwner);
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Error setting reminder", Toast.LENGTH_SHORT).show();
        }
    }
}