package com.asdoi.gymwen;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.asdoi.gymwen.VertretungsplanInternal.VertretungsPlan;
import com.asdoi.gymwen.main.Fragments.VertretungFragment;
import com.asdoi.gymwen.main.MainActivity;

import org.jsoup.nodes.Document;

/**
 * Implementation of App Widget functionality.
 */
public class VertretungWidget extends AppWidgetProvider {
    private String[][] inhalt;
    private String title;
    private boolean oberstufe;
    private Context context;
    private LinearLayout basic_linear;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.vertretung_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vertretungsplan_widget);

            Document[] values = null;
            try {
                values = new VertretungFragment.downloadDoc().execute().get();
                if (values == null) {
                    VertretungsPlan.setDocs(null, null);
                    return;
                }
                if (values.length == 2) {
                    VertretungsPlan.setDocs(values[0], values[1]);
                } else if (values.length == 1) {
                    VertretungsPlan.setTodayDoc(values[0]);
                }
                new NotificationService();
            } catch (Exception e) {
                e.getStackTrace();
            }

            //Create Widget
            this.context = context;
            basic_linear = new LinearLayout(context);
            basic_linear.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            setTableParams();
            remoteViews.addView(R.id.widget_vertical,new RemoteViews(context.getPackageName(),R.layout.vertretungsplan_widget));

//            view.addView((RemoteViews) basic_linear);
//            remoteViews.addView(R.id.layout, view); // id of the layout in widget.xml
//
//            remoteViews.addView(R.id.vertretungwidget_relative, scroll);


            Intent launchMain = new Intent(context, MainActivity.class);
            PendingIntent pendingMainIntent = PendingIntent.getActivity(context, 0, launchMain, 0);
            remoteViews.setOnClickPendingIntent(R.id.vertretung_widget, pendingMainIntent);

            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    void setTableParams() {
        oberstufe = VertretungsPlan.getOberstufe();
        inhalt = VertretungsPlan.getTodayArray();
        title = VertretungsPlan.getTodayTitle();
        TextView titleView = createTitleLayout();
        setTitle(titleView);
        generateTable(basic_linear);

        titleView = createTitleLayout();
//        table = createTableLayout();

        inhalt = VertretungsPlan.getTomorrowArray();
        title = VertretungsPlan.getTomorrowTitle();
        setTitle(titleView);
//        generateTableSpecific(table);
    }

    TextView createTitleLayout() {
        TextView textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
//            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,(int) root.getResources().getDimension(R.dimen.headline_size));
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        basic_linear.addView(textView);
        return textView;
    }

    void setTitle(TextView tV) {
        tV.setText(title);
    }

    void generateTable(LinearLayout vertical) {
//        if (inhalt == null) {
//            generateTableNothing(vertical);
//            return;
//        }
//        if (oberstufe)
            generateTableOberstufe(vertical);
//        else
//            generateTableKlasse(vertical);
    }

    void generateTableOberstufe(LinearLayout vertical) {
        LinearLayout table = new LinearLayout(context);
        table.setWeightSum(5f);
        LinearLayout.LayoutParams basicParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        table.setLayoutParams(basicParams);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        for (int i = 0; i < 6; i++) {
            LinearLayout row = new LinearLayout(context);

            String[] headline = generateFirstHeadline();
            for (int j = 0; j < 5; j++) {
                TextView tv = new TextView(context
                );
                tv.setText(headline[j]);
                tv.setTextSize(18);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                row.addView(tv);

            }
            TextView tv = new TextView(context
            );
            tv.setText(headline[5]);
            tv.setTextSize(12);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.RIGHT);
            row.addView(tv);
            table.addView(row);
            row.setLayoutParams(params);
            for (String[] in : inhalt) {
                row.addView(generateBodyTextView(in[i]));
            }
            table.addView(row);
        }
    }

    TextView generateBodyTextView(String conString) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        TextView tv = new TextView(context);
        tv.setLayoutParams(params);
//        SpannableString content = new SpannableString(conString);
//        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        tv.setText(conString);
        tv.setTextSize(24);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        return tv;
    }

    String[] generateFirstHeadline() {
        //generate first Row
        String[] headline = new String[6];
        String sonstiges = "";

        for (int i = 0; i < inhalt.length; i++) {
            if (!inhalt[i][5].trim().isEmpty()) {
                sonstiges = "Sonstiges";
                break;
            }
        }

        if (oberstufe) {
            headline = new String[]{"Stunde", "Kurs", "Lehrer", "Raum", sonstiges, "Fach"};
        } else {
            headline = new String[]{"Stunde", "Fach", "Lehrer", "Raum", sonstiges, "Klasse"};
        }

        return headline;
    }
}

