package com.asdoi.gymwen.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.widget.RemoteViews;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.MainActivity;

public class SubstitutionWidgetProvider extends AppWidgetProvider {
    public static final String WIDGET_ID_KEY = "mywidgetproviderwidgetids";
    @ColorInt
    protected static int textColorSecondary = Color.GRAY;
    @ColorInt
    protected static int textColorPrimary = Color.BLACK;
    @ColorInt
    protected static int backgroundColor = Color.WHITE;

    private final static int light = 1;
    private final static int dark = 2;
    private final static int black = 3;

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
        setColors(getThemeInt(context), context);
        new Thread(() -> {
            ApplicationFeatures.downloadSubstitutionplanDocsAlways(true, true);
            for (int i = 0; i < appWidgetIds.length; i++) {
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_main);
                updateWidget(context, appWidgetManager, appWidgetIds[i], remoteViews);
                appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
            }
            super.onUpdate(context, appWidgetManager, appWidgetIds);
        }).start();
    }

    public void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, RemoteViews remoteViews) {
        remoteViews.setInt(R.id.widget2_frame, "setBackgroundColor", backgroundColor);

        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(R.id.widget2_listview, intent);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget2_listview);

        //Set OnClick intent
        intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget2_open_button, pendingIntent);

        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, SubstitutionWidgetProvider.class));
        intent = new Intent();
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(WIDGET_ID_KEY, ids);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget2_refresh_button, pendingIntent);

        //Set Button Image
        remoteViews.setImageViewBitmap(R.id.widget2_refresh_button, ApplicationFeatures.vectorToBitmap(R.drawable.ic_refresh_black_24dp));
        remoteViews.setImageViewBitmap(R.id.widget2_open_button, ApplicationFeatures.vectorToBitmap(R.drawable.ic_open_in_browser_white_24dp));
    }

    protected static void setColors(int mode, Context context) {
        switch (mode) {
            default:
            case light:
                textColorPrimary = Color.BLACK;
                textColorSecondary = Color.GRAY;
                backgroundColor = ContextCompat.getColor(context, R.color.widget_white);
                break;
            case dark:
                textColorPrimary = Color.WHITE;
                textColorSecondary = Color.LTGRAY;
                backgroundColor = ContextCompat.getColor(context, R.color.widget_dark);
                break;
            case black:
                textColorPrimary = Color.WHITE;
                textColorSecondary = Color.LTGRAY;
                backgroundColor = ContextCompat.getColor(context, R.color.widget_black);
                break;
        }
    }

    protected static int getThemeInt(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = sharedPref.getString("theme", "switch");
        return getThemeResFromPrefValue(theme, context);
    }

    protected static int getThemeResFromPrefValue(String themePrefValue, Context context) {
        switch (themePrefValue) {
            case "dark":
                return dark;
            case "black":
                return black;
            case "switch":
                int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        return dark;
                    default:
                    case Configuration.UI_MODE_NIGHT_NO:
                        return light;
                }
            case "light":
            default:
                return light;
        }
    }
}
