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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.ulan.timetable.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.Collections;

public class WeekdayFragment extends Fragment {
    public static final String KEY_MONDAY_FRAGMENT = "Monday";
    public static final String KEY_TUESDAY_FRAGMENT = "Tuesday";
    public static final String KEY_WEDNESDAY_FRAGMENT = "Wednesday";
    public static final String KEY_THURSDAY_FRAGMENT = "Thursday";
    public static final String KEY_FRIDAY_FRAGMENT = "Friday";
    public static final String KEY_SATURDAY_FRAGMENT = "Saturday";
    public static final String KEY_SUNDAY_FRAGMENT = "Sunday";

    @Nullable
    private DbHelper db;
    private ListView listView;
    @Nullable
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
        super();
        this.key = key;
        senior = false;
        entries = new SubstitutionList(true);
    }

    public WeekdayFragment() {
        super();
        this.key = KEY_MONDAY_FRAGMENT;
        senior = false;
        entries = new SubstitutionList(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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

    private void setupAdapter(@NonNull View view) {
        db = new DbHelper(getActivity());
        listView = view.findViewById(R.id.timetable_daylist);
        ArrayList<Week> weeks = db.getWeek(key);
        if (PreferenceUtil.isTimeTableSubstitution() && !entries.getNoInternet()) {
            weeks = setupWeekList(weeks);
        }
        adapter = new WeekAdapter((ActivityFeatures) getActivity(), listView, R.layout.timetable_listview_week_adapter, weeks);
        listView.setAdapter(adapter);
    }

    @NonNull
    public ArrayList<Week> setupWeekList(@NonNull ArrayList<Week> weeks) {
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
                        boolean beginIsBeforeFrom = begin.compareToIgnoreCase(week.getFromTime()) < 0;
                        boolean beginIsFrom = begin.equalsIgnoreCase(week.getFromTime());
                        boolean beginIsBeforeTo = begin.compareToIgnoreCase(week.getToTime()) < 0;

                        boolean endIsAfterFrom = end.compareToIgnoreCase(week.getFromTime()) > 0;
                        boolean endIsTo = end.equalsIgnoreCase(week.getToTime());
                        boolean endIsAfterTo = end.compareToIgnoreCase(week.getToTime()) > 0;

                        if (beginIsBeforeFrom) {
                            if (endIsAfterFrom) {
                                if (endIsAfterTo) {
                                    //Check next, remove
                                    checkNext(weeks, begin, end, weekEntry, j);
                                } else {
                                    if (endIsTo) {
                                        //replace
                                        weeks.remove(j);
                                        weeks.add(j, weekEntry);
                                    } else {
                                        //split
                                        split(weeks, begin, end, weekEntry, j, week, beginIsFrom);
                                    }
                                }
                            } else {
                                //add before
                                weeks.add(j, weekEntry);
                            }
                        } else {
                            if (beginIsFrom) {
                                if (endIsAfterTo) {
                                    //Check next, remove
                                    checkNext(weeks, begin, end, weekEntry, j);
                                } else {
                                    if (endIsTo) {
                                        //replace
                                        weeks.remove(j);
                                        weeks.add(j, weekEntry);
                                    } else {
                                        //split
                                        split(weeks, begin, end, weekEntry, j, week, true);
                                    }
                                }
                            } else {
                                if (beginIsBeforeTo) {
                                    if (endIsAfterTo) {
                                        //check next, split
                                        checkNext(weeks, begin, end, weekEntry, j);

                                        week.setToTime(begin);
//                                        week.setEditable(false);
                                        weeks.set(j, week);
                                    } else {
                                        if (endIsTo) {
                                            //split
                                            split(weeks, begin, end, weekEntry, j, week, false);
                                        } else {
                                            //split3
                                            Week week2 = new Week(week.getSubject(), week.getTeacher(), week.getRoom(), week.getFromTime(), week.getToTime(), week.getColor(), false);
                                            week.setToTime(begin);
                                            week2.setFromTime(end);

                                            weeks.set(j, week);
                                            weeks.add(j + 1, weekEntry);
                                            weeks.add(j + 2, week2);
                                        }
                                    }
                                } else {
                                    //check next
                                    if (j >= weeks.size() - 1) {
                                        weeks.add(weekEntry);
                                        break;
                                    }
                                    continue;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        return sortWeekList(weeks);
    }

    private static void split(@NonNull ArrayList<Week> weeks, String begin, String end, Week weekEntry, int j, @NonNull Week week, boolean beginIsFrom) {
        if (beginIsFrom) {
            week.setFromTime(end);
//            week.setEditable(false);
            weeks.set(j, weekEntry);
            weeks.add(j + 1, week);
        } else {
            week.setToTime(begin);
//            week.setEditable(false);
            weeks.set(j, week);
            weeks.add(j + 1, weekEntry);
        }
    }

    private static void checkNext(@NonNull ArrayList<Week> weeks, String begin, @NonNull String end, Week weekEntry, int j) {
        for (int j2 = j; j2 < weeks.size(); j2++) {
            Week week = weeks.get(j2);
            boolean endIsAfterFrom = end.compareToIgnoreCase(week.getFromTime()) > 0;
            boolean endIsBeforeTo = end.compareToIgnoreCase(week.getFromTime()) < 0;

            if (j2 >= weeks.size() - 1) {
                weeks.add(weekEntry);
                break;
            }

            if (endIsAfterFrom) {
                if (endIsBeforeTo) {
                    split(weeks, begin, end, weekEntry, j, week, true);
                } else {
                    //check next, remove
                    weeks.remove(j2);
                    continue;
                }
            } else {
                //add before
                weeks.add(j2, weekEntry);
            }
            break;
        }
    }

    @NonNull
    public ArrayList<Week> sortWeekList(@NonNull ArrayList<Week> weeks) {
        Collections.sort(weeks, (o1, o2) -> o1.getFromTime().compareToIgnoreCase(o2.getFromTime()));
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
