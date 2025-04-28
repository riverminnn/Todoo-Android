package com.example.todooapp.utils.todoForm.reminder;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.todooapp.MainActivity;
import com.example.todooapp.R;
import com.example.todooapp.data.model.Todo;

import java.util.Date;

public class ReminderHelper {
    private static final String CHANNEL_ID = "todo_reminder_channel";
    private static final String TAG = "ReminderHelper";

    // Create notification channel (required for Android 8.0+)
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Todo Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for todo reminders");

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Schedule reminder using AlarmManager
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleReminder(Context context, Todo todo) {
        if (!todo.hasReminder() || todo.getReminderTime() <= System.currentTimeMillis()) {
            Log.d(TAG, "Invalid reminder time");
            return;
        }

        // Create the notification channel
        createNotificationChannel(context);

        // Set up the alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("TODO_ID", todo.getId());
        intent.putExtra("TODO_TITLE", todo.getTitle());
        intent.putExtra("TODO_CONTENT", todo.getContent());

        // Create unique request code based on todo ID to allow multiple reminders
        int requestCode = (int) todo.getId();

        // Update flag to include FLAG_IMMUTABLE for Android 12+
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, flags);

        // Schedule exact alarm if possible, otherwise use inexact
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    todo.getReminderTime(),
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    todo.getReminderTime(),
                    pendingIntent
            );
        }

        Log.d(TAG, "Reminder scheduled for " + new Date(todo.getReminderTime()).toString());
    }

    // Cancel a scheduled reminder
    public static void cancelReminder(Context context, Todo todo) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, (int) todo.getId(), intent, flags);

        alarmManager.cancel(pendingIntent);
    }

    // BroadcastReceiver to handle alarm trigger
    public static class ReminderReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long todoId = intent.getLongExtra("TODO_ID", -1);
            String title = intent.getStringExtra("TODO_TITLE");
            String content = intent.getStringExtra("TODO_CONTENT");

            if (todoId == -1 || title == null) {
                Log.e(TAG, "Invalid reminder data");
                return;
            }

            // Create intent for when notification is tapped
            Intent notificationIntent = new Intent(context, MainActivity.class);
            notificationIntent.putExtra("TODO_ID", todoId);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, (int) todoId, notificationIntent, flags);

            // Build notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_search) // Replace with an appropriate icon
                    .setContentTitle(title)
                    .setContentText(content != null && !content.isEmpty() ? content : "Reminder")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            // Show notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            try {
                notificationManager.notify((int) todoId, builder.build());
                Log.d(TAG, "Notification shown for Todo ID: " + todoId);
            } catch (SecurityException e) {
                Log.e(TAG, "Permission denied for showing notification", e);
                // This can happen when missing POST_NOTIFICATIONS permission on Android 13+
            }
        }
    }
}