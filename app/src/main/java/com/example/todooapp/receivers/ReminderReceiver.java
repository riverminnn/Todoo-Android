package com.example.todooapp.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.todooapp.R;
import com.example.todooapp.MainActivity;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "todo_reminders";
    private static final String TODO_ID = "todo_id";
    private static final String TODO_TITLE = "todo_title";
    private static final String TODO_CONTENT = "todo_content";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ReminderReceiver", "Alarm triggered");

        long todoId = intent.getLongExtra(TODO_ID, -1);
        String title = intent.getStringExtra(TODO_TITLE);
        String content = intent.getStringExtra(TODO_CONTENT);

        // Create notification intent
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra(TODO_ID, todoId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) todoId,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create notification
        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)  // Create a notification icon in resources
                .setContentTitle(title)
                .setContentText(content != null && !content.isEmpty() ? content : "Reminder for your task")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int) todoId, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Todo Reminders";
            String description = "Reminder notifications for todos";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.createNotificationChannel(channel);
        }
    }
}