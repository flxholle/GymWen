package com.asdoi.gymwen.main.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.lehrerliste.Lehrerliste;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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


        new Thread(() -> {
            ApplicationFeatures.downloadLehrerDoc();
            createLayout();
        }).start();


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

            return ActivityFeatures.getTeacherView(convertView, teacherList[position]);
        }

        @Override
        public int getCount() {
            return teacherList.length;
        }
    }

}
