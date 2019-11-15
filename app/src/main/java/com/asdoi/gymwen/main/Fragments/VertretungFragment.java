package com.asdoi.gymwen.main.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.VertretungsplanInternal.VertretungsPlan;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class VertretungFragment extends Fragment implements View.OnClickListener {
    private static View root;
    private static Context context;
    public static boolean today;
    public static boolean all;
    public static boolean both;
    private createTable cT;


    public VertretungFragment() {
        super();
    }

    public VertretungFragment(boolean today, boolean all) {
        super();
        VertretungFragment.today = today;
        VertretungFragment.all = all;
        both = false;
    }

    public VertretungFragment(boolean both) {
        all = false;
        VertretungFragment.both = true;
        today = false;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        root = inflater.inflate(R.layout.fragment_vertretung, container, false);
        FloatingActionButton fab = getActivity().findViewById(R.id.main_fab);
        if (all) {
            fab.setEnabled(false);
        } else {
            fab.setEnabled(true);
        }
        fab.setOnClickListener(this);


        createLoadingPanel();


        if (isNetworkAvailable())
            refreshAndTable();
        else
            generateTable();


        return root;
    }

    void createLoadingPanel() {
        LinearLayout panel = new LinearLayout(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        panel.setLayoutParams(params);
        panel.setGravity(Gravity.CENTER);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setTag("vertretung_loading");

        ProgressBar bar = new ProgressBar(context);
        bar.setIndeterminate(true);
        params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bar.setLayoutParams(params);

        TextView textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setText("Lädt herunter...");

        panel.addView(bar);
        panel.addView(textView);

        ((ViewGroup) root.findViewById(R.id.vertretung_constraint)).addView(panel);
    }

    //TODO: Is Network Avaiable Check!

    public static class downloadDoc extends AsyncTask<String, Void, Document[]> {
        @Override
        protected Document[] doInBackground(String... strURL) {
            Document[] doc = new Document[strURL.length];
            if (!VertretungsPlan.areDocsDownloaded()) {
                for (int i = 0; i < strURL.length; i++) {

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
//                    e.printStackTrace();
                        VertretungsPlan.lastAuthString = authString + "f";
                        return null;
                    }
                }
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document[] result) {
//            new createTable().execute(result);

            //Set Document
            setDocs(result);
        }

        public void setDocs(Document[] values) {
            if (!VertretungsPlan.areDocsDownloaded()) {
                if (values == null) {
                    VertretungsPlan.setDocs(null, null);
                    return;
                }
                if (values.length == 2) {
                    VertretungsPlan.setDocs(values[0], values[1]);
                } else if (values.length == 1) {
                    VertretungsPlan.setTodayDoc(values[0]);
                }
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String shareMessage() {
        String message = "";
        String[][] inhalt = null;
        String title = "";

        if (both) {
            both = false;
            today = true;
            message += shareMessage();
            today = false;
            message += shareMessage();
            return message;
        } else if (today) {
            inhalt = VertretungsPlan.getTodayArray();
            title = VertretungsPlan.getTodayTitle();
        } else {
            inhalt = VertretungsPlan.getTomorrowArray();
            title = VertretungsPlan.getTomorrowTitle();
        }
        String classes = "";

        if (VertretungsPlan.getOberstufe()) {
            ArrayList<String> courses = new ArrayList<>();
            if (inhalt != null) {
                for (String[] line : inhalt) {
                    if (!courses.contains(line[0])) {
                        courses.add(line[0]);
                    }
                }
                for (int i = 0; i < courses.size() - 1; i++) {
                    classes += courses.get(i) + ", ";
                }
                classes += courses.get(courses.size() - 1);
            }
        } else {
            ArrayList<String> names = VertretungsPlan.getNames();
            for (int i = 0; i < names.size(); i++) {
                classes += names.get(i) + "";
            }
        }

        if (inhalt == null) {
            message += "Am " + title + " haben wir (" + classes + ") keine Vertretung.\n";
            return message;
        }
        if (VertretungsPlan.getOberstufe()) {
            message = "Am " + title + " haben wir (" + classes + ") folgende Vertretung:\n";
            for (String[] line : inhalt) {
                if (line[3].equals("entfällt")) {
                    message += line[1] + ". Stunde entfällt für Kurs " + line[0] + "\n";
                } else {
                    message += line[1] + ". Stunde für Kurs " + line[0] + " in Raum " + line[4] + " bei " + line[3] + " " + line[5] + "\n";
                }
            }
        } else {
            for (String[] line : inhalt) {
                if (line[3].equals("entfällt")) {
                    message += line[1] + ". Stunde entfällt\n";
                } else {
                    message += line[1] + ". Stunde " + line[2] + " bei " + line[3] + " in Raum " + line[4] + " " + line[5] + "\n";
                }
            }
        }
        return message;
    }

    private void refresh() {
        new downloadDoc().execute(VertretungsPlan.todayURL, VertretungsPlan.tomorrowURL);
    }

    private void share() {
        String message = shareMessage();
        String footprint = "Diese Nachricht wurde mit der GymWen App erzeugt.";
        message += footprint;

        if (VertretungsPlan.getTodayTitle().equals("Keine Internetverbindung!")) {
            //Toast.makeText(getActivity(), "Du bist nicht mit dem Internet verbunden!",Toast.LENGTH_LONG).show();
            Snackbar snackbar = Snackbar
                    .make(root.findViewById(R.id.vertretung_constraint), "Du bist nicht mit dem Internet verbunden!", Snackbar.LENGTH_LONG);
            snackbar.show();

            return;
        }

        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, message);
        i.setType("text/plan");
        startActivity(Intent.createChooser(i, "Vertretung senden an..."));
    }

    private void generateTable() {
        if (cT != null)
            cT.cancel(true);
        cT = new createTable();
        cT.execute();
    }

    private void refreshAndTable() {
        refresh();
        generateTable();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.main_fab) {
            share();
        }
    }

    private static class createTable extends AsyncTask<String, Void, Void> {
        private String[][] inhalt;
        private String title;
        private boolean oberstufe;

        @Override
        protected Void doInBackground(String... args) {
//            root.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            publishProgress();
            return (null);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onProgressUpdate(Void... item) {
            setTableParams();
            oberstufe = VertretungsPlan.getOberstufe();
        }

        @Override
        protected void onPostExecute(Void arg) {
            if (isCancelled()) {
                clear();
            }
        }

        public void clear() {
            ((ViewGroup) root.findViewById(R.id.vertretung_constraint)).removeView(root.findViewWithTag("vertretung_loading"));
            LinearLayout base = root.findViewById(R.id.vertretung_linear_layout_layer1);
            base.removeAllViews();
        }

        void setTableParams() {
            clear();
            if (both)
                generateScrollView();
            TextView titleView = createTitleLayout();
            TableLayout table = createTableLayout();

            oberstufe = VertretungsPlan.getOberstufe();
            if (both) {
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
            } else if (all) {
                if (today) {
                    inhalt = VertretungsPlan.getTodayArrayAll();
                    title = VertretungsPlan.getTodayTitle();
                } else {
                    inhalt = VertretungsPlan.getTomorrowArrayAll();
                    title = VertretungsPlan.getTomorrowTitle();
                }
                generateTableNormal(table);
                setTitle(titleView);
            } else {
                if (today) {
                    inhalt = VertretungsPlan.getTodayArray();
                    title = VertretungsPlan.getTodayTitle();
                } else {
                    inhalt = VertretungsPlan.getTomorrowArray();
                    title = VertretungsPlan.getTomorrowTitle();
                }
                generateTableSpecific(table);
                setTitle(titleView);
            }
        }

        void generateScrollView() {
            ScrollView s = new ScrollView(context);
            LinearLayout l = root.findViewById(R.id.vertretung_linear_layout_layer1);
            ViewGroup parent = (ViewGroup) l.getParent();
            parent.removeAllViews();
            parent.addView(s);
            s.addView(l);
        }

        TableLayout createTableLayout() {
            TableLayout table = new TableLayout(context);
            table.setStretchAllColumns(true);

            LinearLayout base = root.findViewById(R.id.vertretung_linear_layout_layer1);

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
            ((LinearLayout) root.findViewById(R.id.vertretung_linear_layout_layer1)).addView(textView);
            return textView;
        }

        void generateTableNormal(TableLayout table) {
//            for (int i = 0; i < table.getChildCount(); i++) {
//                if (root.findViewById(i + 130) != null) {
//                    table.removeView((TableRow) root.findViewById(i + 130));
//                    System.out.println("removed row " + i);
//                }
//            }

            if (inhalt == null) {
                generateTableNothing(table);
                return;
            }
            generateFirstRowAll(table);
            generateBodyRowsAll(table);

        }

        void generateBodyRowsAll(TableLayout table) {
            int columnNumber = inhalt[0].length;
            int rowNumber = inhalt.length;

            for (int i = 0; i < rowNumber; i++) {
                TableRow row = new TableRow(context);
                row.setId(i + 130);

                /*Button bt = new Button(context
                );
                bt.setText(inhalt[i][0]);
                bt.setTypeface(Typeface.DEFAULT_BOLD);
                bt.setGravity(Gravity.CENTER);
                row.addView(bt);*/


                for (int j = 0; j < columnNumber; j++) {
                    TextView tv = new TextView(context);
                    tv.setText(inhalt[i][j]);
                    tv.setTextSize(18);
                    tv.setTypeface(Typeface.DEFAULT_BOLD);
                    tv.setGravity(Gravity.CENTER);
                    row.addView(tv);

                }
                table.addView(row);
            }
        }


        void generateTableSpecific(TableLayout table) {
            for (int i = 0; i < table.getChildCount(); i++) {
                if (root.findViewById(i + 130) != null) {
                    table.removeView(root.findViewById(i + 130));
                    System.out.println("removed row " + i);
                }
            }

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
                tv.setBackgroundColor(context.getColor(R.color.colorAccent));
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
                    tv.setTextColor(context.getColor(R.color.colorAccent));
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
                    tv.setTextColor(context.getColor(R.color.colorAccent));
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
                tv.setBackgroundColor(context.getColor(R.color.colorAccent));
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
                    tv.setTextColor(context.getColor(R.color.colorAccent));
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
                    tv.setTextColor(context.getColor(R.color.colorAccent));
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

            if (all) {
                headline = new String[]{"Klasse", "Stunde", "Fach", "Lehrer", "Raum", "Sonstiges"};
            } else if (oberstufe) {
                headline = new String[]{"Stunde", "Kurs", "Lehrer", "Raum", sonstiges, "Fach"};
            } else {
                headline = new String[]{"Stunde", "Fach", "Lehrer", "Raum", sonstiges, "Klasse"};
            }


            if (root.findViewById(new Integer(129)) != null) {
                table.removeView(root.findViewById(new Integer(129)));
                System.out.println("removed row " + 129);
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

        void generateFirstRowAll(TableLayout table) {
            //generate first Row
            String[] headline = new String[6];
            String sonstiges = "";

            for (int i = 0; i < inhalt.length; i++) {
                if (!inhalt[i][5].trim().isEmpty()) {
                    sonstiges = "Sonstiges";
                    break;
                }
            }

            if (all) {
                headline = new String[]{"Klasse", "Stunde", "Fach", "Lehrer", "Raum", "Sonstiges"};
            } else if (oberstufe) {
                headline = new String[]{"Stunde", "Kurs", "Lehrer", "Raum", sonstiges, "Fach"};
            } else {
                headline = new String[]{"Stunde", "Fach", "Lehrer", "Raum", sonstiges, "Klasse"};
            }


            if (root.findViewById(new Integer(129)) != null) {
                table.removeView(root.findViewById(new Integer(129)));
                System.out.println("removed row " + 129);
            }


            TableRow row = new TableRow(context);
            row.setId(new Integer(129));
            for (int j = 0; j < headline.length; j++) {
                TextView tv = new TextView(context
                );
                tv.setText(headline[j]);
                tv.setTextSize(18);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                row.addView(tv);
            }
            table.addView(row);
        }

        void setTitle(TextView tV) {
            tV.setText(title);
        }
    }

}