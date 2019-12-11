package com.asdoi.gymwen.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.core.content.ContextCompat;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;

public class VertretungsplanWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    public class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private Context context;
        private int mAppWidgetId;

        public StackRemoteViewsFactory(Context context, Intent intent) {
            this.context = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        public void onCreate() {
        }


        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_entry);
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

        private String sonstigesString(String[][] inhalt) {
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

        private boolean isSonstiges(String[][] inhalt) {
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
}
