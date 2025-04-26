package com.example.todooapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.example.todooapp.MainActivity;
import com.example.todooapp.R;

public class TodoWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            // Create different layouts based on widget size
            int layoutId = R.layout.widget_small; // Default is small

            // Get the width and height to determine size
            int width = appWidgetManager.getAppWidgetOptions(appWidgetId)
                    .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            int height = appWidgetManager.getAppWidgetOptions(appWidgetId)
                    .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

            // Choose layout based on size
            if (width >= 300 && height >= 300) {
                layoutId = R.layout.widget_large; // 4x4
            } else if ((width >= 150 && height >= 300) || (width >= 300 && height >= 150)) {
                layoutId = R.layout.widget_medium; // 2x4
            }

            updateWidget(context, appWidgetManager, appWidgetId, layoutId);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, android.os.Bundle newOptions) {
        // Update widget when resized
        onUpdate(context, appWidgetManager, new int[]{appWidgetId});
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager,
                              int appWidgetId, int layoutId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);

        // Connect the ListView to the TodoWidgetService
        Intent serviceIntent = new Intent(context, TodoWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
// Make the intent unique across different widget instances
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.widget_list_view, serviceIntent);

// Empty view for when there's no data
        views.setEmptyView(R.id.widget_list_view, R.id.widget_empty_view);

        // Set up intent for clicking on the widget header to open app
        Intent openAppIntent = new Intent(context, MainActivity.class);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(context, 0,
                openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_header, openAppPendingIntent);

        // Set up intent for clicking on the add button
        Intent addIntent = new Intent(context, MainActivity.class);
        addIntent.putExtra("open_form", true);
        PendingIntent addPendingIntent = PendingIntent.getActivity(context, 1,
                addIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_add_button, addPendingIntent);

        // In TodoWidget.java - updateWidget method
        Intent clickIntent = new Intent(context, MainActivity.class);
        // Add action to make intent unique and ensure extras are preserved
        clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        PendingIntent clickPendingIntent = PendingIntent.getActivity(context, 2,
                clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        views.setPendingIntentTemplate(R.id.widget_list_view, clickPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (appWidgetIds != null) {
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
            }
        }
    }
}