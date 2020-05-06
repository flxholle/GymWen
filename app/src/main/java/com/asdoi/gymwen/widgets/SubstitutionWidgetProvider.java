/*
 * Copyright (c) 2020 Felix Hollederer
 *     This file is part of GymWenApp.
 *
 *     GymWenApp is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GymWenApp is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GymWenApp.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.MainActivity;
import com.asdoi.gymwen.ui.activities.SubstitutionWidgetActivity;

import java.util.ArrayList;
import java.util.Objects;

public class SubstitutionWidgetProvider extends AppWidgetProvider {
    public static final String WIDGET_ID_KEY = "mywidgetproviderwidgetids";
    public static final String OPEN_APP = "openapp";

    @ColorInt
    protected static int textColorSecondary = Color.GRAY;
    @ColorInt
    protected static int textColorPrimary = Color.BLACK;
    @ColorInt
    private static int backgroundColor = Color.WHITE;

    private final static int light = 1;
    private final static int dark = 2;
    private final static int black = 3;

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        if (intent.hasExtra(WIDGET_ID_KEY)) {
            int[] ids = Objects.requireNonNull(intent.getExtras()).getIntArray(WIDGET_ID_KEY);
            this.onUpdate(context, AppWidgetManager.getInstance(context), Objects.requireNonNull(ids));
        } else if (OPEN_APP.equals(intent.getAction())) {
            Intent openapp = new Intent(context, MainActivity.class);
            openapp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(openapp);
        } else
            super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, @NonNull int[] appWidgetIds) {
        setColors(getThemeInt(context));
        new Thread(() -> {
            ApplicationFeatures.downloadSubstitutionplanDocsAlways(true, true);
            for (int appWidgetId : appWidgetIds) {
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_substitution);
                updateWidget(context, appWidgetManager, appWidgetId, remoteViews);
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            }
            super.onUpdate(context, appWidgetManager, appWidgetIds);
        }).start();
    }

    public static void updateWidget(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, int appWidgetId, @NonNull RemoteViews remoteViews) {
        remoteViews.setInt(R.id.widget_substitution_frame, "setBackgroundColor", backgroundColor);

        //Setup listview
        Intent intent = new Intent(context, SubstitutionWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        ArrayList<Integer> arrayList = SubstitutionWidgetActivity.loadPref(context, appWidgetId);
        if (arrayList.size() == 0)
            arrayList.add(0);
        int[] array = new int[arrayList.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = arrayList.get(i);
        }

        intent.putExtra(SubstitutionWidgetActivity.PROFILES, array);


        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(R.id.widget_substitution_listview, intent);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_substitution_listview);

        //Set OpenApp Button intent
        intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_substitution_open_button, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.widget_substitution_frame, pendingIntent);

        Intent listviewClickIntent = new Intent(context, SubstitutionWidgetProvider.class);
        listviewClickIntent.setAction(OPEN_APP);
        PendingIntent listviewPendingIntent = PendingIntent.getBroadcast(context, 0, listviewClickIntent, 0);
        remoteViews.setPendingIntentTemplate(R.id.widget_substitution_listview, listviewPendingIntent);

        //Setup Refresh Button Intent
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, SubstitutionWidgetProvider.class));
        intent = new Intent();
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(WIDGET_ID_KEY, ids);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_substiution_refresh_button, pendingIntent);

        //Set Button Image
        remoteViews.setImageViewBitmap(R.id.widget_substiution_refresh_button, ApplicationFeatures.vectorToBitmap(R.drawable.ic_refresh_black_24dp));
        remoteViews.setImageViewBitmap(R.id.widget_substitution_open_button, ApplicationFeatures.vectorToBitmap(R.drawable.ic_open_in_browser_white_24dp));

        //Remove loading view
        remoteViews.setViewVisibility(R.id.widget_substiution_loading, View.GONE);
    }

    private static void setColors(int mode) {
        switch (mode) {
            default:
            case light:
                backgroundColor = Color.parseColor("#D9FFFFFF");
                textColorPrimary = Color.BLACK;
                textColorSecondary = Color.GRAY;
                break;
            case dark:
                backgroundColor = Color.parseColor("#D9212121");
                textColorPrimary = Color.WHITE;
                textColorSecondary = Color.LTGRAY;
                break;
            case black:
                backgroundColor = Color.parseColor("#D9000000");
                textColorPrimary = Color.WHITE;
                textColorSecondary = Color.LTGRAY;
                break;
        }
    }

    private static int getThemeInt(@NonNull Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = sharedPref.getString("theme", "switch");
        return getThemeResFromPrefValue(theme, context);
    }

    private static int getThemeResFromPrefValue(@NonNull String themePrefValue, @NonNull Context context) {
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
