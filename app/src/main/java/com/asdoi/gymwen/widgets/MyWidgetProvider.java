package com.asdoi.gymwen.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.MainActivity;

public class MyWidgetProvider extends AppWidgetProvider {
    public static final String WIDGET_ID_KEY = "mywidgetproviderwidgetids";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(WIDGET_ID_KEY)) {
            int[] ids = intent.getExtras().getIntArray(WIDGET_ID_KEY);
            this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
        } else
            super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        new Thread(() -> {
            ApplicationFeatures.downloadVertretungsplanDocs(true, true);
            for (int i = 0; i < appWidgetIds.length; i++) {
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_main);
                updateWidget(context, appWidgetManager, appWidgetIds[i], remoteViews);
                appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
            }
            super.onUpdate(context, appWidgetManager, appWidgetIds);
        }).start();
    }

    public void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, RemoteViews remoteViews) {
//            remoteViews.setInt(R.id.widget2_frame, "setBackgroundColor", ApplicationFeatures.getBackgroundColor(context));

        Intent intent = new Intent(context, StackWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(R.id.widget2_listview, intent);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget2_listview);

        //Set OnClick intent
        intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget2_open_button, pendingIntent);

        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, MyWidgetProvider.class));
        intent = new Intent();
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(WIDGET_ID_KEY, ids);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget2_refresh_button, pendingIntent);

        //Set Button Image
        remoteViews.setImageViewBitmap(R.id.widget2_refresh_button, ApplicationFeatures.vectorToBitmap(R.drawable.ic_refresh_black_24dp));
        remoteViews.setImageViewBitmap(R.id.widget2_open_button, ApplicationFeatures.vectorToBitmap(R.drawable.ic_open_in_browser_white_24dp));
    }
}
