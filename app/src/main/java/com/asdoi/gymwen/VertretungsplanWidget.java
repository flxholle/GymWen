package com.asdoi.gymwen;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.asdoi.gymwen.VertretungsplanInternal.VertretungsPlan;


/**
 * Implementation of App Widget functionality.
 */
public class VertretungsplanWidget extends AppWidgetProvider {
    private String[][] inhalt;
    private String title;
    private boolean oberstufe;
    private Context context;
    private LinearLayout basic_linear;
    private ScrollView scroll;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
//            Intent intent = new Intent(context, MainActivity.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vertretungsplan_widget);
//            remoteViews.setOnClickPendingIntent(R.id.textView, pendingIntent);

//            Document[] values = null;
//            try {
//                values = new VertretungFragment.downloadDoc().execute().get();
//                if (values == null) {
//                    VertretungsPlan.setDocs(null, null);
//                    return;
//                }
//                if (values.length == 2) {
//                    VertretungsPlan.setDocs(values[0], values[1]);
//                } else if (values.length == 1) {
//                    VertretungsPlan.setTodayDoc(values[0]);
//                }
//                new NotificationService();
//            } catch (Exception e) {
//                e.getStackTrace();
//            }
//
//            //Create Widget
//            this.context = context;
//            setTableParams();

            /*RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.vertretungsplan_widget); // xml with just a textview
            view.addView((RemoteViews) basic_linear);
            remoteViews.addView(R.id.layout, view); // id of the layout in widget.xml

            remoteViews.addView(R.id.vertretungwidget_relative, scroll);*/

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    void setTableParams() {
        generateScrollView();
        TextView titleView = createTitleLayout();
        TableLayout table = createTableLayout();

        oberstufe = VertretungsPlan.getOberstufe();
        inhalt = VertretungsPlan.getTodayArray();
        title = VertretungsPlan.getTodayTitle();
        setTitle(titleView);
        generateTableSpecific(table);

        titleView = createTitleLayout();
        table = createTableLayout();

        inhalt = VertretungsPlan.getTomorrowArray();
        title = VertretungsPlan.getTomorrowTitle();
        setTitle(titleView);
        generateTableSpecific(table);
    }

    void generateScrollView() {
        scroll = new ScrollView(context);
        basic_linear = new LinearLayout(context);
        scroll.addView(basic_linear);
    }

    TableLayout createTableLayout() {
        TableLayout table = new TableLayout(context);
        table.setStretchAllColumns(true);

        LinearLayout base = basic_linear;

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ScrollView s1 = new ScrollView(context);
        s1.setLayoutParams(params);

        HorizontalScrollView hs2 = new HorizontalScrollView(context);
        hs2.setLayoutParams(params);

        base.addView(s1);
        s1.addView(hs2);
        hs2.addView(table);


        return table;
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


    void generateTableSpecific(TableLayout table) {
        if (inhalt == null) {
            generateTableNothing(table);
            return;
        }
        generateFirstRow(table);
        if (oberstufe)
            generateBodyRowsOberstufe(table);
        else
            generateBodyRowsKlasse(table);
    }

    void generateBodyRowsOberstufe(TableLayout table) {
        int columnNumber = inhalt[0].length;
        int rowNumber = inhalt.length;


        for (int i = 0; i < rowNumber; i++) {
            TableRow row = new TableRow(context);
            row.setId(i + 130);

            //Stunde
            int hourMargin = 5;
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(hourMargin, hourMargin, hourMargin, hourMargin);

            TextView tv = new TextView(context);
            tv.setPadding(hourMargin, hourMargin, hourMargin, hourMargin);
            tv.setLayoutParams(params);
            tv.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            tv.setTextColor(Color.WHITE);
            tv.setText(inhalt[i][1]);
            tv.setTextSize(36);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            row.addView(tv, params);

            params.setMargins(0, 0, 0, 0);

            if (inhalt[i][3].equals("entfällt")) {
                //Kurs
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                SpannableString content = new SpannableString(inhalt[i][3]);
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                tv.setText(content);
                tv.setTextSize(24);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                row.addView(tv);

                for (int j = 2; j < columnNumber - 1; j++) {
                    tv = new TextView(context
                    );
                    tv.setText("");
                    tv.setTextSize(18);
                    tv.setTypeface(Typeface.DEFAULT_BOLD);
                    tv.setGravity(Gravity.CENTER);
                    row.addView(tv);
                }
                //Fach
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][0]);
                tv.setTextSize(12);
                tv.setTypeface(Typeface.DEFAULT);
                tv.setGravity(Gravity.RIGHT);
                row.addView(tv);
            } else {
                //Kurs
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][0]);
                tv.setTextSize(18);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                row.addView(tv);

                //Lehrer
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][3]);
                tv.setTextSize(18);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                row.addView(tv);

                //Raum
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                SpannableString content = new SpannableString(inhalt[i][4]);
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                tv.setText(content);
                tv.setTextSize(24);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                row.addView(tv);

                //Sonstiges
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][5]);
                tv.setTextSize(18);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                row.addView(tv);

                //Fach
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][2]);
                tv.setTextSize(12);
                tv.setTypeface(Typeface.DEFAULT);
                tv.setGravity(Gravity.RIGHT);
                row.addView(tv);
            }


            table.addView(row);
        }
    }

    void generateBodyRowsKlasse(TableLayout table) {
        int columnNumber = inhalt[0].length;
        int rowNumber = inhalt.length;


        for (int i = 0; i < rowNumber; i++) {
            TableRow row = new TableRow(context
            );
            row.setId(i + 130);

            //Stunde
            int hourMargin = 5;
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(hourMargin, hourMargin, hourMargin, hourMargin);

            TextView tv = new TextView(context
            );
            tv.setPadding(hourMargin, hourMargin, hourMargin, hourMargin);
            tv.setLayoutParams(params);
            tv.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            tv.setTextColor(Color.WHITE);
            tv.setText(inhalt[i][1]);
            tv.setTextSize(36);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            row.addView(tv, params);

            params.setMargins(0, 0, 0, 0);

            if (inhalt[i][3].equals("entfällt")) {
                //Klasse
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                SpannableString content = new SpannableString(inhalt[i][3]);
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                tv.setText(content);
                tv.setTextSize(24);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                row.addView(tv);

                for (int j = 2; j < columnNumber; j++) {
                    tv = new TextView(context
                    );
                    tv.setText("");
                    tv.setTextSize(18);
                    tv.setTypeface(Typeface.DEFAULT_BOLD);
                    tv.setGravity(Gravity.CENTER);
                    row.addView(tv);
                }
                //Klasse
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][0]);
                tv.setTextSize(12);
                tv.setTypeface(Typeface.DEFAULT);
                tv.setGravity(Gravity.RIGHT);
                row.addView(tv);

            } else {
                //Fach
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][2]);
                tv.setTextSize(18);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                row.addView(tv);

                //Lehrer
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][3]);
                tv.setTextSize(18);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                row.addView(tv);

                //Raum
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                SpannableString content = new SpannableString(inhalt[i][4]);
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                tv.setText(content);
                tv.setTextSize(24);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                row.addView(tv);

                //Sonstiges
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][5]);
                tv.setTextSize(18);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                row.addView(tv);

                //Klasse
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][0]);
                tv.setTextSize(12);
                tv.setTypeface(Typeface.DEFAULT);
                tv.setGravity(Gravity.RIGHT);
                row.addView(tv);
            }


            table.addView(row);
        }
    }

    void generateTableNothing(TableLayout table) {
        TableRow row = new TableRow(context
        );
        row.setId(new Integer(130));
        for (int j = 0; j < 2; j++) {
            TextView tv = new TextView(context
            );
            tv.setText("");
            tv.setTextSize(24);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            row.addView(tv);
        }
        TextView tv = new TextView(context
        );
        tv.setText("Es entfällt nichts");
        tv.setTextSize(20);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER);
        row.addView(tv);
        table.addView(row);
        for (int j = 0; j < 3; j++) {
            tv = new TextView(context
            );
            tv.setText("");
            tv.setTextSize(18);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            row.addView(tv);
        }
    }

    void generateFirstRow(TableLayout table) {
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


        TableRow row = new TableRow(context
        );
        row.setId(new Integer(129));
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
    }

    void setTitle(TextView tV) {
        tV.setText(title);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

