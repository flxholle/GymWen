/*
 * Copyright (c) 2020 Felix Hollederer
 *     This file is part of GymWenApp.
 *
 *     GymWenApp is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GymWenApp is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GymWenApp.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ulan.timetable.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.substitutionplan.SubstitutionEntry;
import com.asdoi.gymwen.substitutionplan.SubstitutionList;
import com.ulan.timetable.adapters.WeekAdapter;
import com.ulan.timetable.model.Week;
import com.ulan.timetable.utils.DbHelper;
import com.ulan.timetable.utils.FragmentHelper;

import java.util.ArrayList;

public class WeekdayFragment extends Fragment {
    public static final String KEY_MONDAY_FRAGMENT = "Monday";
    public static final String KEY_TUESDAY_FRAGMENT = "Tuesday";
    public static final String KEY_WEDNESDAY_FRAGMENT = "Wednesday";
    public static final String KEY_THURSDAY_FRAGMENT = "Thursday";
    public static final String KEY_FRIDAY_FRAGMENT = "Friday";
    public static final String KEY_SATURDAY_FRAGMENT = "Saturday";
    public static final String KEY_SUNDAY_FRAGMENT = "Sunday";

    private DbHelper db;
    private ListView listView;
    private WeekAdapter adapter;
    private View view;

    private SubstitutionList entries;
    private boolean senior;
    private String key;

    public WeekdayFragment(SubstitutionList entries, boolean senior, String key) {
        super();
        this.entries = entries;
        this.senior = senior;
        this.key = key;
    }

    public WeekdayFragment(String key) {
        this.key = key;
        senior = false;
        entries = new SubstitutionList(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.timetable_fragment_weekday, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupAdapter(view);
        setupListViewMultiSelect();
    }

    private void setupAdapter(View view) {
        db = new DbHelper(getActivity());
        listView = view.findViewById(R.id.timetable_daylist);
        adapter = new WeekAdapter((ActivityFeatures) getActivity(), listView, R.layout.timetable_listview_week_adapter, setupWeekList(db.getWeek(key)));
        listView.setAdapter(adapter);
    }

    private ArrayList<Week> setupWeekList(ArrayList<Week> weeks) {
        boolean empty = weeks.isEmpty();
        if (!entries.getNoInternet()) {
            for (int i = 0; i < entries.getEntries().size(); i++) {
                SubstitutionEntry entry = entries.getEntries().get(i);
                int color = ContextCompat.getColor(getContext(), entry.isNothing() ? R.color.notification_icon_background_omitted : R.color.notification_icon_background_substitution);
                String subject = senior ? entry.getCourse() : entry.getSubject();
                String teacher = entry.getTeacher();
                String room = entry.getRoom();
                String begin = entry.getMatchingBeginTime("-");
                if (begin.length() < 5)
                    begin = "0" + begin;

                String end = entry.getMatchingEndTime("-");
                if (end.length() < 5)
                    end = "0" + end;

                Week weekEntry = new Week(subject, teacher, room, begin, end, color, false);
                weekEntry.setMoreInfos(entry.getMoreInformation());
//                if (!entry.getMoreInformation().trim().isEmpty())
//                    subject = subject + " (" + entry.getMoreInformation() + ")";

                if (empty) {
                    weeks.add(weekEntry);
                } else {
                    for (int j = 0; j < weeks.size(); j++) {
                        Week week = weeks.get(j);

                        if (begin.equalsIgnoreCase(week.getFromTime())) {
                            if (end.equalsIgnoreCase(week.getToTime())) {
                                //Same times
                                weeks.remove(j);
                                weeks.add(j, weekEntry);
                            } else {
                                if (end.compareToIgnoreCase(week.getToTime()) < 0) {
                                    //Start same, ends ago
                                    week.setFromTime(end);
//                                    week.setEditable(false);
                                    weeks.set(j, week);
                                    weeks.add(j, weekEntry);
                                } else {
                                    //Start same, Ends after
                                    weeks.remove(j);
                                    weeks.add(j, weekEntry);
                                }
                            }
                        } else if (begin.compareToIgnoreCase(week.getFromTime()) <= 0) {
                            //Starts Ago -> Add before
                            if (end.compareToIgnoreCase(week.getToTime()) < 0) {
                                //Starts ago, ends ago
                                week.setFromTime(end);
                                week.setEditable(false);
                                weeks.set(j, week);
                                weeks.add(j, weekEntry);
                            } else {
                                //Starts ago, ends after or same
                                weeks.remove(j);
                                weeks.add(j, weekEntry);
                            }
                        } else if (end.compareToIgnoreCase(week.getToTime()) < 0 || begin.compareToIgnoreCase(week.getToTime()) < 0) {
                            //Starts after (but between), Ends ago, same, after
                            week.setToTime(begin);
//                            week.setEditable(false);
                            weeks.set(j, week);
                            weeks.add(j + 1, weekEntry);
                        } else if (j == weeks.size() - 1) {
                            //End of weeks
                            weeks.add(weekEntry);
                        } else {
                            continue;
                        }
                        break;
                    }
                }
            }
        }
        return weeks;
    }

    private void setupListViewMultiSelect() {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(FragmentHelper.setupListViewMultiSelect(getActivity(), listView, adapter, db));
    }

    public String getKey() {
        return key;
    }
}
