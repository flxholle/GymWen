package com.ulan.timetable.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.substitutionplan.SubstitutionEntry;
import com.asdoi.gymwen.substitutionplan.SubstitutionList;
import com.ulan.timetable.adapters.WeekAdapter;
import com.ulan.timetable.model.Week;
import com.ulan.timetable.utils.DbHelper;
import com.ulan.timetable.utils.FragmentHelper;

import java.util.ArrayList;


public class FridayFragment extends Fragment {

    public static final String KEY_FRIDAY_FRAGMENT = "Friday";
    private DbHelper db;
    private ListView listView;
    private WeekAdapter adapter;

    private SubstitutionList entries = null;
    private boolean senior;

    public FridayFragment(SubstitutionList entries, boolean senior) {
        super();
        this.entries = entries;
        this.senior = senior;
    }

    public FridayFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.timetable_fragment_friday, container, false);
        setupAdapter(view);
        setupListViewMultiSelect();
        return view;
    }

    private void setupAdapter(View view) {
        db = new DbHelper(getActivity());
        listView = view.findViewById(R.id.fridaylist);

        ArrayList<Week> weeks = db.getWeek(KEY_FRIDAY_FRAGMENT);
        if (!entries.getNoInternet()) {
            for (int i = 0; i < entries.getEntries().size(); i++) {
                SubstitutionEntry entry = entries.getEntries().get(i);
                Week weekEntry;
                int color = ContextCompat.getColor(getContext(), entry.isNothing() ? R.color.notification_icon_background_omitted : R.color.notification_icon_background_substitution);
                String subject = senior ? entry.getCourse() : entry.getSubject();
                String teacher = entry.getTeacher();
                String room = entry.getRoom();
                String begin = entry.getMatchingBeginTime();
                String end = entry.getMatchingEndTime();


                if (i < weeks.size()) {
                    Week week = weeks.get(i);
                    if (subject.trim().isEmpty())
                        subject = week.getSubject();
                    if (teacher.trim().isEmpty())
                        teacher = week.getTeacher();
                    if (room.trim().isEmpty())
                        room = week.getRoom();
                    if (begin.trim().isEmpty())
                        begin = week.getToTime();
                    if (end.trim().isEmpty())
                        end = week.getFromTime();
                    weeks.remove(i);
                }
          /*      weekEntry = new Week(subject, teacher, room, begin, end, color);
                weeks.add(i, weekEntry);*/
            }
        }

        adapter = new WeekAdapter(getActivity(), listView, R.layout.timetable_listview_week_adapter, weeks);
        listView.setAdapter(adapter);
    }

    private void setupListViewMultiSelect() {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(FragmentHelper.setupListViewMultiSelect(getActivity(), listView, adapter, db));
    }
}
