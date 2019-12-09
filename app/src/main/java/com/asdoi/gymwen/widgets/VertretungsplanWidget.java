package com.asdoi.gymwen.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.main.MainActivity;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;

import androidx.core.content.ContextCompat;

/**
 * Implementation of App Widget functionality.
 */
public class VertretungsplanWidget extends AppWidgetProvider {
    private static Context context = ApplicationFeatures.getContext();
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

//        ApplicationFeatures.proofeNotification();
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
        RemoteViews rootView = new RemoteViews(context.getPackageName(), R.layout.widget_vertretungsplan);
//        rootView.removeAllViews(R.id.widget1_basic);


        // Get a handler that can be used to post to the main thread
        handler = new Handler(context.getMainLooper());

        init(rootView, appWidgetManager, appWidgetId);

    }

    private void init(final RemoteViews rootView, final AppWidgetManager awm, final int awID) {
        new Thread(() -> {
            ApplicationFeatures.downloadVertretungsplanDocs(true, true);

           /* Intent intent = new Intent(context, StackWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, awID);
            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
//            rootView.setRemoteAdapter(R.id.widget_list, intent);
*/

            generateTable(rootView);
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            rootView.setOnClickPendingIntent(R.id.widget1_basic, pendingIntent);

            int[] ids = awm.getAppWidgetIds(new ComponentName(context, VertretungsplanWidget.class));
            intent = new Intent();
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(VertretungsplanWidget.WIDGET_ID_KEY, ids);
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            rootView.setOnClickPendingIntent(R.id.widget1_refresh_button, pendingIntent);

            rootView.setImageViewBitmap(R.id.widget1_refresh_button, ApplicationFeatures.vectorToBitmap(R.drawable.ic_refresh_black_24dp));

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    awm.updateAppWidget(awID, rootView);
                } // This is your code
            };
            handler.post(myRunnable);
        }).start();
    }

    public class StackWidgetService extends RemoteViewsService {
        @Override
        public RemoteViewsFactory onGetViewFactory(Intent intent) {
            return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
        }
    }

    class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private Context mContext;
        private int mAppWidgetId;

        public StackRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        public void onCreate() {
        }


        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_entry);
            generateTable(rv);
            return rv;
        }

        @Override
        public int getCount() {
            return 1;
        }

        public void onDataSetChanged() {
        }

        public void onDestroy() {
            // In onDestroy() you should tear down anything that was setup for your data source,
            // eg. cursors, connections, etc.

        }

        public RemoteViews getLoadingView() {
            // You can create a custom loading view (for instance when getViewAt() is slow.) If you
            // return null here, you will get the default loading view.
            return null;
        }

        public int getViewTypeCount() {
            return 1;
        }

        public long getItemId(int position) {
            return position;
        }

        public boolean hasStableIds() {
            return true;
        }


    }

    private void generateTable(RemoteViews basic) {
        basic.addView(R.id.widget1_basic, generateTableDay(VertretungsPlanFeatures.getTodayTitle(), VertretungsPlanFeatures.getTodayArray(), VertretungsPlanFeatures.getOberstufe()));
        if (VertretungsPlanFeatures.getTodayArray() != null)
            basic.addView(R.id.widget1_basic, generateTableDay(VertretungsPlanFeatures.getTomorrowTitle(), VertretungsPlanFeatures.getTomorrowArray(), VertretungsPlanFeatures.getOberstufe()));
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
        if (inhalt == null || inhalt.length == 0)
            headline = null;
        addHead(title, headline, day, sonstig);

        if (inhalt == null) {
            return day;
        }

        if (inhalt.length == 0) {
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
            SpannableString content = new SpannableString(inhalt[3]);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            row.setTextViewText(R.id.widgetBody_text2, content);
            row.setTextColor(R.id.widgetBody_text2, ContextCompat.getColor(context, R.color.colorAccent));
            row.setTextViewTextSize(R.id.widgetBody_text2, TypedValue.COMPLEX_UNIT_SP, 18f);

            row.setTextViewText(R.id.widgetBody_text6, inhalt[0]);

            row.setViewVisibility(R.id.widgetBody_text3, View.GONE);
//            row.setViewVisibility(R.id.widgetBody_text4, View.GONE);
//            row.setViewVisibility(R.id.widgetBody_text5, View.GONE);
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

