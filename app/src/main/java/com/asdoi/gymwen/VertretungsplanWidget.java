package com.asdoi.gymwen;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.RemoteViews;

import com.asdoi.gymwen.VertretungsplanInternal.VertretungsPlan;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Implementation of App Widget functionality.
 */
public class VertretungsplanWidget extends AppWidgetProvider {
    private static Context context;
    private RemoteViews rootView;
    private AppWidgetManager awm;
    private int awID;
    private Handler handler;

    void updateAppWidget(Context context2, AppWidgetManager appWidgetManager, int appWidgetId) {
        context = context2;
        // Construct the RemoteViews object
        rootView = new RemoteViews(context.getPackageName(), R.layout.vertretungsplan_widget);
        awm = appWidgetManager;
        awID = appWidgetId;

        // Get a handler that can be used to post to the main thread
        handler = new Handler(context.getMainLooper());

        init();
//        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_textview);
//        rootView.addView(R.id.widget1_basic, rv);
//        awm.updateAppWidget(awID, rootView);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
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

    private void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //DownloadDocs
                if (!VertretungsPlan.areDocsDownloaded() && isNetworkAvailable()) {
                    if (!DummyApplication.setSettings()) {
                        return;
                    }
                    String[] strURL = new String[]{VertretungsPlan.todayURL, VertretungsPlan.tomorrowURL};
                    Document[] doc = new Document[strURL.length];
                    for (int i = 0; i < 2; i++) {

                        String authString = VertretungsPlan.strUserId + ":" + VertretungsPlan.strPasword;

                        String lastAuthString = VertretungsPlan.lastAuthString;
                        //Check if already tried logging in with this authentication and if it failed before, return null
                        if (lastAuthString.length() > 1 && lastAuthString.substring(0, lastAuthString.length() - 1).equals(authString) && lastAuthString.charAt(lastAuthString.length() - 1) == 'f') {
                            System.out.println("failed before with same authString");
                            //return doc;
                        }

                        String encodedString =
                                new String(Base64.encodeBase64(authString.getBytes()));

                        try {
                            doc[i] = Jsoup.connect(strURL[i])
                                    .header("Authorization", "Basic " + encodedString)
                                    .get();

                            System.out.println("Logged in using basic authentication");
                            VertretungsPlan.lastAuthString = authString + "t";


                        } catch (IOException e) {
                            e.printStackTrace();
                            VertretungsPlan.lastAuthString = authString + "f";
                            return;
                        }
                    }
                    VertretungsPlan.setDocs(doc[0], doc[1]);
                }
                generateTable(rootView);
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

    /*private void addBody(String[] inhalt, boolean oberstufe, RemoteViews widget_day) {
        RemoteViews rV;

        if (inhalt == null) {
            rV = new RemoteViews(context.getPackageName(), R.layout.widget_textview);
            rV.setTextViewTextSize(R.id.widget_textview, 18, 0);
            rV.setTextViewText(R.id.widget_textview, context.getResources().getString(R.string.nothing));
            widget_day.addView(R.id.widgetDay_head3, rV);
            return;
        }

        //Stunde
        rV = new RemoteViews(context.getPackageName(), R.layout.widget_textview);
        int hourMargin = 5;
        rV.setViewPadding(R.id.widget_textview, hourMargin, hourMargin, hourMargin, hourMargin);
        rV.setTextColor(R.id.widget_textview, Color.WHITE);
        rV.setTextViewText(R.id.widget_textview, inhalt[1]);
        rV.setTextViewTextSize(R.id.widget_textview, 36, 0);
        widget_day.addView(R.id.widgetDay_head1, rV);


        if (inhalt[3].equals("entfällt")) {
            //Kurs
            rV = new RemoteViews(context.getPackageName(), R.layout.widget_textview);
            SpannableString content = new SpannableString(inhalt[3]);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            rV.setTextViewText(R.id.widget_textview, content);
            rV.setTextViewTextSize(R.id.widget_textview, 24, 0);
            rV.setTextColor(R.id.widget_textview, ContextCompat.getColor(context, R.color.colorAccent));
            widget_day.addView(R.id.widgetDay_head2, rV);

            //Fach
            rV = new RemoteViews(context.getPackageName(), R.layout.widget_textview);
            rV.setTextViewTextSize(R.id.widget_textview, 12, 0);
            rV.setTextViewText(R.id.widget_textview, inhalt[0]);
            widget_day.addView(R.id.widgetDay_head6, rV);
            return;
        }

        //Kurs
        rV = new RemoteViews(context.getPackageName(), R.layout.widget_textview);
        rV.setTextViewTextSize(R.id.widget_textview, 18, 0);
        rV.setTextViewText(R.id.widget_textview, oberstufe ? inhalt[0] : inhalt[2]);
        widget_day.addView(R.id.widgetDay_head2, rV);

        //Lehrer
        rV = new RemoteViews(context.getPackageName(), R.layout.widget_textview);
        rV.setTextViewTextSize(R.id.widget_textview, 18, 0);
        rV.setTextViewText(R.id.widget_textview, inhalt[3]);
        widget_day.addView(R.id.widgetDay_head3, rV);

        //Raum
        rV = new RemoteViews(context.getPackageName(), R.layout.widget_textview);
        rV.setTextViewTextSize(R.id.widget_textview, 24, 0);
        SpannableString content = new SpannableString(inhalt[4]);
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        rV.setTextViewText(R.id.widget_textview, content);
        rV.setTextColor(R.id.widget_textview, ContextCompat.getColor(context, R.color.colorAccent));
        widget_day.addView(R.id.widgetDay_head4, rV);

        //Sonstiges
        rV = new RemoteViews(context.getPackageName(), R.layout.widget_textview);
        rV.setTextViewTextSize(R.id.widget_textview, 18, 0);
        rV.setTextViewText(R.id.widget_textview, inhalt[5]);
        widget_day.addView(R.id.widgetDay_head5, rV);

        //Fach
        rV = new RemoteViews(context.getPackageName(), R.layout.widget_textview);
        rV.setTextViewTextSize(R.id.widget_textview, 12, 0);
        rV.setTextViewText(R.id.widget_textview, oberstufe ? inhalt[2] : inhalt[0]);
        widget_day.addView(R.id.widgetDay_head6, rV);

    }*/


    //Old Widget Creation Method
   /* private void generateTable(RemoteViews basic) {
        generateTableDay(VertretungsPlan.getTodayTitle(), VertretungsPlan.getTodayArray(), VertretungsPlan.getOberstufe(), basic);
        generateTableDay(VertretungsPlan.getTomorrowTitle(), VertretungsPlan.getTomorrowArray(), VertretungsPlan.getOberstufe(), basic);

        // Instruct the widget manager to update the widget
        awm.updateAppWidget(awID, rootView);
    }

    private void generateTableDay(String title, String[][] inhalt, boolean oberstufe, RemoteViews basic) {
        //Create Headline
        String[] headline;
        String sonstiges = sonstigesString(inhalt);
        if (oberstufe) {
            headline = new String[]{context.getString(R.string.hours), context.getString(R.string.courses), context.getString(R.string.teacher), context.getString(R.string.room), sonstiges, context.getString(R.string.subject)};
        } else {
            headline = new String[]{context.getString(R.string.hours), context.getString(R.string.subject), context.getString(R.string.teacher), context.getString(R.string.room), sonstiges, context.getString(R.string.classes)};
        }
        RemoteViews head = generateTableHeadDay(title, headline);
        basic.addView(R.id.widget1_basic, head);

        //Create Body Rows
        if (inhalt != null) {
            for (String[] con : inhalt) {
                RemoteViews row = generateBodyRow(con, oberstufe);
                basic.addView(R.id.widget1_basic, row);
            }
        } else {
            RemoteViews row = generateEmptyRow();
            basic.addView(R.id.widget1_basic, row);
        }

    }

    private RemoteViews generateTableHeadDay(String title, String[] headline) {
        RemoteViews head = new RemoteViews(context.getPackageName(), R.layout.widget_head);
        head.setTextViewText(R.id.widgetHead_title, title);

        //Set Headline Strings
        head.setTextViewText(R.id.widgetHead_text1, headline[0]);
        head.setTextViewText(R.id.widgetHead_text2, headline[1]);
        head.setTextViewText(R.id.widgetHead_text3, headline[2]);
        head.setTextViewText(R.id.widgetHead_text4, headline[3]);
        head.setTextViewText(R.id.widgetHead_text5, headline[4]);
        head.setTextViewText(R.id.widgetHead_text6, headline[5]);

        return head;
    }*/

    private RemoteViews generateBodyRow(String[] inhalt, boolean oberstufe, boolean sonstiges) {
        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_body);

        row.setTextViewText(R.id.widgetBody_text1, inhalt[1]);
        if (inhalt[3].equals("entfällt")) {
            row.setTextViewText(R.id.widgetBody_text2, inhalt[3]);
            row.setTextColor(R.id.widgetBody_text2, context.getResources().getColor(R.color.colorAccent));
            row.setTextViewTextSize(R.id.widgetBody_text2, 24, 0);

            row.setTextViewText(R.id.widgetHead_text6, inhalt[0]);

            row.setViewVisibility(R.id.widgetBody_text3, View.GONE);
            row.setViewVisibility(R.id.widgetBody_text4, View.GONE);
            row.setViewVisibility(R.id.widgetBody_text5, View.GONE);
            return row;
        }
        if (oberstufe) {
            row.setTextViewText(R.id.widgetBody_text2, inhalt[0]);
            row.setTextViewText(R.id.widgetBody_text3, inhalt[3]);
            SpannableString content = new SpannableString(inhalt[4]);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            row.setTextViewText(R.id.widgetBody_text4, content);
            row.setTextViewTextSize(R.id.widgetBody_text4, 24, 0);
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
            row.setTextViewTextSize(R.id.widgetBody_text4, 24, 0);
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

