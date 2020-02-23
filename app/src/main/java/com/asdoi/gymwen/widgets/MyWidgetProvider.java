package com.asdoi.gymwen.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
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
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_main);

            remoteViews.setInt(R.id.widget2_frame, "setBackgroundColor", ApplicationFeatures.getBackgroundColor(context));

            // Set up the intent that starts the StackViewService, which will
            // provide the views for this collection.
            Intent intent = new Intent(context, StackWidgetService.class);
            // Add the app widget ID to the intent extras.
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            // Set up the RemoteViews object to use a RemoteViews adapter.
            // This adapter connects
            // to a RemoteViewsService  through the specified intent.
            // This is how you populate the data.
            remoteViews.setRemoteAdapter(R.id.widget2_listview, intent);

            //Set OnClick intent
            intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget2_frame, pendingIntent);

//            int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, VertretungsplanWidget.class));
//            intent = new Intent();
//            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//            intent.putExtra(WIDGET_ID_KEY, ids);
//            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            remoteViews.setOnClickPendingIntent(R.id.widget2_refresh_button, pendingIntent);

            //Set Button Image
            remoteViews.setImageViewBitmap(R.id.widget2_refresh_button, ApplicationFeatures.vectorToBitmap(R.drawable.ic_refresh_black_24dp));

            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
