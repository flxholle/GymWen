package com.asdoi.gymwen;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import com.asdoi.gymwen.VertretungsplanInternal.VertretungsPlan;
import com.asdoi.gymwen.main.MainActivity;

/**
 * Implementation of App Widget functionality.
 */
public class VertretungsplanWidget extends AppWidgetProvider {
    private static Context context = DummyApplication.getContext();
    private Handler handler;

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
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

//        DummyApplication.proofeNotification();
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews rootView = new RemoteViews(context.getPackageName(), R.layout.vertretungsplan_widget);
        rootView.removeAllViews(R.id.widget1_basic);


        // Get a handler that can be used to post to the main thread
        handler = new Handler(context.getMainLooper());

        init(rootView, appWidgetManager, appWidgetId);

    }

    private void init(final RemoteViews rootView, final AppWidgetManager awm, final int awID) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DummyApplication.downloadDocs(true);
                generateTable(rootView);
                Intent intent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                rootView.setOnClickPendingIntent(R.id.widget1_basic, pendingIntent);

                AppWidgetManager man = AppWidgetManager.getInstance(context);
                int[] ids = man.getAppWidgetIds(new ComponentName(context, VertretungsplanWidget.class));
                intent = new Intent();
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                intent.putExtra(VertretungsplanWidget.WIDGET_ID_KEY, ids);
                pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                rootView.setOnClickPendingIntent(R.id.widget1_refresh_button, pendingIntent);

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        awm.updateAppWidget(awID, rootView);
                    } // This is your code
                };
                handler.post(myRunnable);
            }
        }).start();
    }

    private void generateTable(RemoteViews basic) {
        basic.addView(R.id.widget1_basic, generateTableDay(VertretungsPlan.getTodayTitle(), VertretungsPlan.getTodayArray(), VertretungsPlan.getOberstufe()));
        basic.addView(R.id.widget1_basic, generateTableDay(VertretungsPlan.getTomorrowTitle(), VertretungsPlan.getTomorrowArray(), VertretungsPlan.getOberstufe()));
    }

    private RemoteViews generateTableDay(String title, String[][] inhalt, boolean oberstufe) {
        RemoteViews day = new RemoteViews(context.getPackageName(), R.layout.widget_day);

        //Create Headline
        String[] headline;
        String sonstiges = sonstigesString(inhalt);
        boolean sonstig = isSonstiges(inhalt);
        if (oberstufe) {
            headline = new String[]{context.getString(R.string.hours), context.getString(R.string.courses), context.getString(R.string.teacher), context.getString(R.string.room), sonstiges, context.getString(R.string.subject)};
        } else {
            headline = new String[]{context.getString(R.string.hours), context.getString(R.string.subject), context.getString(R.string.teacher), context.getString(R.string.room), sonstiges, context.getString(R.string.classes)};
        }
        if (inhalt == null)
            headline = null;
        addHead(title, headline, day, sonstig);

        if (inhalt == null) {
            day.addView(R.id.widget_day_basic, generateEmptyRow());
        } else {
            for (String[] s : inhalt) {
                day.addView(R.id.widget_day_basic, generateBodyRow(s, oberstufe, sonstig));
            }
        }

        return day;
    }

    private void addHead(String title, String[] headline, RemoteViews widget_day, boolean sonstiges) {
        widget_day.setTextViewText(R.id.widgetDay_title, title);

        if (headline == null) {
            widget_day.setViewVisibility(R.id.widgetDay_text1, View.GONE);
            widget_day.setViewVisibility(R.id.widgetDay_text2, View.GONE);
            widget_day.setViewVisibility(R.id.widgetDay_text3, View.GONE);
            widget_day.setViewVisibility(R.id.widgetDay_text4, View.GONE);
            widget_day.setViewVisibility(R.id.widgetDay_text5, View.GONE);
            widget_day.setViewVisibility(R.id.widgetDay_text6, View.GONE);
        } else {
            //Set Headline Strings
            widget_day.setTextViewText(R.id.widgetDay_text1, headline[0]);
            widget_day.setTextViewText(R.id.widgetDay_text2, headline[1]);
            widget_day.setTextViewText(R.id.widgetDay_text3, headline[2]);
            widget_day.setTextViewText(R.id.widgetDay_text4, headline[3]);

            if (!sonstiges) {
                widget_day.setViewVisibility(R.id.widgetDay_text5, View.GONE);
            } else {
                widget_day.setTextViewText(R.id.widgetDay_text5, headline[4]);
            }

            widget_day.setTextViewText(R.id.widgetDay_text6, headline[5]);
        }
    }

    private RemoteViews generateBodyRow(String[] inhalt, boolean oberstufe, boolean sonstiges) {
        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_body);

        row.setTextViewText(R.id.widgetBody_text1, inhalt[1]);
        if (inhalt[3].equals("entfällt")) {
            row.setTextViewText(R.id.widgetBody_text2, inhalt[3]);
            row.setTextColor(R.id.widgetBody_text2, ContextCompat.getColor(context, R.color.colorAccent));

            row.setTextViewText(R.id.widgetBody_text6, inhalt[0]);

            /*row.setViewVisibility(R.id.widgetBody_text3, View.GONE);
            row.setViewVisibility(R.id.widgetBody_text4, View.GONE);
            row.setViewVisibility(R.id.widgetBody_text5, View.GONE);*/
            return row;
        }
        if (oberstufe) {
            row.setTextViewText(R.id.widgetBody_text2, inhalt[0]);
            row.setTextViewText(R.id.widgetBody_text3, inhalt[3]);
            SpannableString content = new SpannableString(inhalt[4]);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            row.setTextViewText(R.id.widgetBody_text4, content);
            if (!sonstiges)
                row.setViewVisibility(R.id.widgetBody_text5, View.GONE);
            else
                row.setTextViewText(R.id.widgetBody_text5, inhalt[5]);
            row.setTextViewText(R.id.widgetBody_text6, inhalt[2]);
        } else {
            row.setTextViewText(R.id.widgetBody_text2, inhalt[2]);
            row.setTextViewText(R.id.widgetBody_text3, inhalt[3]);
            SpannableString content = new SpannableString(inhalt[4]);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            row.setTextViewText(R.id.widgetBody_text4, content);
            if (!sonstiges)
                row.setViewVisibility(R.id.widgetBody_text5, View.GONE);
            else
                row.setTextViewText(R.id.widgetBody_text5, inhalt[5]);
            row.setTextViewText(R.id.widgetBody_text6, inhalt[0]);
        }

        return row;
    }

    private RemoteViews generateEmptyRow() {
        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_body);
        row.setTextViewText(R.id.widgetBody_text3, context.getResources().getString(R.string.nothing));
        row.setViewVisibility(R.id.widgetBody_text1, View.GONE);
        row.setViewVisibility(R.id.widgetBody_text2, View.GONE);
        row.setViewVisibility(R.id.widgetBody_text4, View.GONE);
        row.setViewVisibility(R.id.widgetBody_text5, View.GONE);
        row.setViewVisibility(R.id.widgetBody_text6, View.GONE);
        return row;
    }

    private static String sonstigesString(String[][] inhalt) {
        String sonstiges = "";

        if (inhalt == null)
            return sonstiges;

        for (int i = 0; i < inhalt.length; i++) {
            if (!inhalt[i][5].trim().isEmpty()) {
                sonstiges = context.getString(R.string.other);
                break;
            }
        }
        return sonstiges;
    }

    private static boolean isSonstiges(String[][] inhalt) {
        boolean sonstiges = false;

        if (inhalt == null)
            return false;

        for (int i = 0; i < inhalt.length; i++) {
            if (!inhalt[i][5].trim().isEmpty()) {
                sonstiges = true;
                break;
            }
        }
        return sonstiges;
    }

}

