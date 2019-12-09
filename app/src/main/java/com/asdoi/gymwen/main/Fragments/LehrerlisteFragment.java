package com.asdoi.gymwen.main.Fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
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

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class LehrerlisteFragment extends Fragment {
    private static ListView teacherListView;
    private static String[][] teacherList;
    private static ViewGroup root;


    public LehrerlisteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_lehrerliste, container, false);

        ActivityFeatures.createLoadingPanel(root.findViewById(R.id.teacher_frame));

        FloatingActionButton fab = getActivity().findViewById(R.id.main_fab);
        fab.setVisibility(View.GONE);

        LehrerlisteFragment.root = (ViewGroup) root;

        if (ApplicationFeatures.isNetworkAvailable()) {
            new Thread(() -> {
                ApplicationFeatures.downloadLehrerDoc();
                createLayout();
            }).start();
        } else {
            createLayout();
        }

        return root;
    }


    private void createLayout() {
        getActivity().runOnUiThread(() -> {
            clear();
            teacherList = Lehrerliste.liste();

            if (teacherList == null) {
                TextView title = createTitleLayout();
                title.setText(getContext().getString(R.string.noInternetConnection));
                return;
            }

            teacherListView = new ListView(getContext());
            teacherListView.setAdapter(new TeacherListAdapter(getContext(), 0));
            root.addView(teacherListView);
        });
    }

    private void clear() {
        root.removeAllViews();
    }

    TextView createTitleLayout() {
        TextView textView = new TextView(ApplicationFeatures.getContext());
        textView.setTextColor(Color.BLACK);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
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
                convertView = getLayoutInflater().inflate(R.layout.list_lehrerliste_entry, null);
            }

            TextView kürzel = convertView.findViewById(R.id.teacher_kürzel);
            kürzel.setText(teacherList[position][0]);

            TextView nname = convertView.findViewById(R.id.teacher_nname);
            nname.setText(teacherList[position][1]);

            TextView vname = convertView.findViewById(R.id.teacher_vname);
            vname.setText(" " + teacherList[position][2]);

            TextView hour = convertView.findViewById(R.id.teacher_hour);
            hour.setText(teacherList[position][3]);
            hour.setVisibility(View.GONE);


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
                hour.setVisibility(View.VISIBLE);
            });

            return convertView;
        }

        @Override
        public int getCount() {
            return teacherList.length;
        }
    }

}
