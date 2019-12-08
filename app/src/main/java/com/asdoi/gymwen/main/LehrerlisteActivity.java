package com.asdoi.gymwen.main;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.lehrerliste.Lehrerliste;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.widget.Toolbar;

public class LehrerlisteActivity extends ActivityFeatures {
    private static ListView teacherListView;
    private static String[][] teacherList;
    private static ViewGroup root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leherliste);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((View view) -> {
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });

        root = findViewById(R.id.teacher_basic);
        createLoadingPanel(findViewById(R.id.teacher_basic));

        createLayout();
    }

    private void createLayout() {
        new Thread(() -> {
            if (ApplicationFeatures.isNetworkAvailable()) {
                ApplicationFeatures.downloadLehrerDoc();
            }
            runOnUiThread(() -> {
                clear();
                teacherList = Lehrerliste.liste();

                TextView title = createTitleLayout();
                if (teacherList == null) {
                    title.setText(getContext().getString(R.string.noInternetConnection));
                    root.addView(title);
                    return;
                } else {
                    //Create Title Layout
                }

                teacherListView = new ListView(getContext());
                teacherListView.setAdapter(new TeacherListAdapter(getContext(), 0));
                root.addView(teacherListView);
            });
        }).start();
    }

    private void clear() {
//        ((ViewGroup) root.findViewById(R.id.teacher_basic)).removeView(root.findViewWithTag("vertretung_loading"));
        root.removeAllViews();
    }

    TextView createTitleLayout() {
        TextView textView = new TextView(ApplicationFeatures.getContext());
        textView.setTextColor(Color.BLACK);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
//            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,(int) root.getResources().getDimension(R.dimen.headline_size));
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(textView);
        return textView;
    }

    private class TeacherListAdapter extends ArrayAdapter<String[]> {

        public TeacherListAdapter(Context con, int resource) {
            super(con, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.lehrerliste_entry, null);
            }

            TextView kürzel = convertView.findViewById(R.id.teacher_kürzel);
            kürzel.setText(teacherList[position][0]);

            TextView nname = convertView.findViewById(R.id.teacher_name);
            nname.setText(teacherList[position][1]);

            Button mailButton = convertView.findViewById(R.id.teacher_mail);
            mailButton.setOnClickListener((View v) -> {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + teacherList[position][0] + "@gym-wendelstein.de"));
                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    Snackbar.make(v, getContext().getString(R.string.no_email_app), Snackbar.LENGTH_LONG).show();
                }
            });

            FrameLayout root = convertView.findViewById(R.id.teacher_rootLayout);
            root.setOnClickListener((View v) -> {
                //TODO: Expand View
            });

            return convertView;
        }

        @Override
        public int getCount() {
            return teacherList.length;
        }
    }

}
