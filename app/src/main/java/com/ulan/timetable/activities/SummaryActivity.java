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

package com.ulan.timetable.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
import com.github.tlaabs.timetableview.Schedule;
import com.github.tlaabs.timetableview.Time;
import com.github.tlaabs.timetableview.TimetableView;
import com.ulan.timetable.databaseUtils.DbHelper;
import com.ulan.timetable.fragments.WeekdayFragment;
import com.ulan.timetable.model.Week;
import com.ulan.timetable.utils.AlertDialogsHelper;
import com.ulan.timetable.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import me.yaoandy107.ntut_timetable.CourseTableLayout;
import me.yaoandy107.ntut_timetable.model.CourseInfo;
import me.yaoandy107.ntut_timetable.model.StudentCourse;


public class SummaryActivity extends ActivityFeatures {
    private int lessonDuration;
    private String schoolStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_summary);

        lessonDuration = PreferenceUtil.getPeriodLength(this);
        int[] oldTimes = PreferenceUtil.getStartTime(this);
        schoolStart = oldTimes[0] + ":" + oldTimes[1];

        findViewById(R.id.courseTable).setVisibility(View.GONE);

        if (PreferenceUtil.isSummaryLibrary1())
            setupCourseTableLibrary1();
        else
            setupTimetableLibrary2();
    }

    public void setupColors() {
        setToolbar(true);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.timetable_summary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_changeSummary) {
            PreferenceUtil.setSummaryLibrary(this, !PreferenceUtil.isSummaryLibrary1());
            recreate();
        } else if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SummarySettingsActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Setup the course Table with Library: https://github.com/asdoi/TimetableUI
     */
    private void setupCourseTableLibrary1() {
        DbHelper dbHelper = new DbHelper(this);

        CourseTableLayout courseTable = findViewById(R.id.courseTable);
        courseTable.setVisibility(View.VISIBLE);
        courseTable.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        StudentCourse studentCourse = new StudentCourse();
        ArrayList<CourseInfo> courseInfoList = new ArrayList<>();

        List<List<Week>> weeks = new ArrayList<>();
        weeks.add(dbHelper.getWeek(WeekdayFragment.KEY_MONDAY_FRAGMENT));
        weeks.add(dbHelper.getWeek(WeekdayFragment.KEY_TUESDAY_FRAGMENT));
        weeks.add(dbHelper.getWeek(WeekdayFragment.KEY_WEDNESDAY_FRAGMENT));
        weeks.add(dbHelper.getWeek(WeekdayFragment.KEY_THURSDAY_FRAGMENT));
        weeks.add(dbHelper.getWeek(WeekdayFragment.KEY_FRIDAY_FRAGMENT));
        weeks.add(dbHelper.getWeek(WeekdayFragment.KEY_SATURDAY_FRAGMENT));
        weeks.add(dbHelper.getWeek(WeekdayFragment.KEY_SUNDAY_FRAGMENT));

        List<List<Integer>> durations = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            durations.add(new ArrayList<>());
        }

        List<List<String>> durationStrings = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            durationStrings.add(new ArrayList<>());
        }

        for (int j = 0; j < weeks.size(); j++) {
            for (int i = 0; i < weeks.get(j).size(); i++) {
                int ago = 0;
                for (int k = i - 1; k >= 0; k--) {
                    ago += durations.get(j).get(k);
                }

                int difference_to_week_before;
                Week newW = new Week();
                if (i != 0) {
                    newW.setFromTime(weeks.get(j).get(i - 1).getToTime());
                    newW.setToTime(weeks.get(j).get(i).getFromTime());
                } else {
                    newW.setFromTime(schoolStart);
                    newW.setToTime(weeks.get(j).get(i).getFromTime());
                }
                difference_to_week_before = getDurationOfWeek(newW, true);

                durations.get(j).add(i, getDurationOfWeek(weeks.get(j).get(i)) + difference_to_week_before);
                durationStrings.get(j).add(i, generateLessonsString(getDurationOfWeek(weeks.get(j).get(i)), ago + difference_to_week_before));
            }
        }

        for (int j = 0; j < weeks.size(); j++) {
            for (int i = 0; i < weeks.get(j).size(); i++) {
                Week w = weeks.get(j).get(i);

                String[] courseTimes = new String[7];
                Arrays.fill(courseTimes, "");
                courseTimes[j] = "" + durationStrings.get(j).get(i);

                CustomCourseInfo courseInfo = new CustomCourseInfo(w);
                courseInfo.setCourseTime(courseTimes);
                courseInfoList.add(courseInfo);
            }
        }

        // Set timetable
        studentCourse.setCourseList(courseInfoList);

        courseTable.setHeader(getResources().getStringArray(R.array.timetable_header));
        courseTable.setTextSize(14);
        courseTable.setStudentCourse(studentCourse);

        courseTable.setOnCourseClickListener(view -> {
            CustomCourseInfo item = (CustomCourseInfo) view.getTag();
            final View alertLayout = getLayoutInflater().inflate(R.layout.timetable_dialog_add_subject, null);
            AlertDialogsHelper.getEditSubjectDialog(this, alertLayout, this::recreate, item.getWeek());
        });
    }

    private int getDurationOfWeek(Week w) {
        return getDurationOfWeek(w, false);
    }

    private int getDurationOfWeek(Week w, boolean countOnlyIfFitsLessonsTime) {
        Calendar weekCalendarStart = Calendar.getInstance();
        int startHour = Integer.parseInt(w.getFromTime().substring(0, w.getFromTime().indexOf(":")));
        weekCalendarStart.set(Calendar.HOUR_OF_DAY, startHour);
        int startMinute = Integer.parseInt(w.getFromTime().substring(w.getFromTime().indexOf(":") + 1));
        weekCalendarStart.set(Calendar.MINUTE, startMinute);

        Calendar weekCalendarEnd = Calendar.getInstance();
        int endHour = Integer.parseInt(w.getToTime().substring(0, w.getToTime().indexOf(":")));
        weekCalendarEnd.set(Calendar.HOUR_OF_DAY, endHour);
        int endMinute = Integer.parseInt(w.getToTime().substring(w.getToTime().indexOf(":") + 1));
        weekCalendarEnd.set(Calendar.MINUTE, endMinute);

        long differencesInMillis = weekCalendarEnd.getTimeInMillis() - weekCalendarStart.getTimeInMillis();
        int inMinutes = (int) (differencesInMillis / 1000 / 60);

        if (inMinutes < lessonDuration && countOnlyIfFitsLessonsTime)
            return 0;

        int multiplier;
        if (inMinutes % lessonDuration > 0 && !countOnlyIfFitsLessonsTime) {
            multiplier = inMinutes / lessonDuration + 1;
        } else
            multiplier = inMinutes / lessonDuration;

        return multiplier;
    }

    private static String generateLessonsString(int duration, int hoursBefore) {
        StringBuilder durationString = new StringBuilder();
        for (int i = 1; i <= duration; i++) {
            durationString.append(i + hoursBefore).append(" ");
        }

        return durationString.toString();
    }

    private static class CustomCourseInfo extends CourseInfo {
        private Week week;

        public CustomCourseInfo(Week w) {
            super();
            week = w;

            StringBuilder name = new StringBuilder(w.getSubject());
            if (w.getTeacher() != null && !w.getTeacher().trim().isEmpty())
                name.append("\n").append(w.getTeacher());
            if (w.getRoom() != null && !w.getTeacher().trim().isEmpty())
                name.append("\n").append(w.getRoom());

            setName(name.toString());

            setColor(w.getColor());
        }

        public Week getWeek() {
            return week;
        }
    }


    /**
     * Setup the course Table with Library: https://github.com/asdoi/TimetableView
     */
    private void setupTimetableLibrary2() {
        List<List<Week>> weeks = new ArrayList<>();
        DbHelper dbHelper = new DbHelper(this);
        weeks.add(dbHelper.getWeek(WeekdayFragment.KEY_MONDAY_FRAGMENT));
        weeks.add(dbHelper.getWeek(WeekdayFragment.KEY_TUESDAY_FRAGMENT));
        weeks.add(dbHelper.getWeek(WeekdayFragment.KEY_WEDNESDAY_FRAGMENT));
        weeks.add(dbHelper.getWeek(WeekdayFragment.KEY_THURSDAY_FRAGMENT));
        weeks.add(dbHelper.getWeek(WeekdayFragment.KEY_FRIDAY_FRAGMENT));
        weeks.add(dbHelper.getWeek(WeekdayFragment.KEY_SATURDAY_FRAGMENT));
        weeks.add(dbHelper.getWeek(WeekdayFragment.KEY_SUNDAY_FRAGMENT));

        List<String> done = new ArrayList<>();
        ArrayList<String> colors = new ArrayList<>();
        List<ArrayList<Schedule>> timetableContent = new ArrayList<>();

        int rows = 0;

        for (int j = 0; j < weeks.size(); j++) {
            for (int i = 0; i < weeks.get(j).size(); i++) {
                ArrayList<Schedule> schedules = new ArrayList<>();
                Week w = weeks.get(j).get(i);
                String subject = w.getSubject();

                if (done.contains(subject))
                    continue;

                int i1 = i + 1;
                for (int j1 = j; j1 < weeks.size(); j1++) {
                    for (; i1 < weeks.get(j1).size(); i1++) {
                        if (weeks.get(j1).get(i1).getSubject().equalsIgnoreCase(subject)) {
                            CustomSchedule schedule = new CustomSchedule(weeks.get(j1).get(i1), j1);
                            schedules.add(schedule);
                            if (schedule.getStartTime().getHour() > rows)
                                rows = schedule.getStartTime().getHour();
                        }
                    }
                    i1 = 0;
                }

                CustomSchedule schedule = new CustomSchedule(w, j);
                schedules.add(schedule);
                if (w.getColor() != -1)
                    colors.add(String.format("#%06X", (0xFFFFFF & w.getColor())));
                else
                    colors.add(String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, R.color.grey))));

                if (schedule.getStartTime().getHour() > rows)
                    rows = schedule.getStartTime().getHour();

                done.add(subject);
                timetableContent.add(schedules);
            }
        }

        int startHour = Integer.parseInt(schoolStart.substring(0, schoolStart.indexOf(":")));

        String[] header = new String[8];
        String[] resource = getResources().getStringArray(R.array.timetable_header);
        for (int i = 1; i < header.length; i++) {
            header[i] = resource[i - 1];
        }

        TimetableView timetable = new TimetableView.Builder(this)
                .setColumnCount(6 + (PreferenceUtil.isSevenDays() ? 2 : 0))
                .setRowCount(10)
                .setStartTime(startHour)
                .setHeaderTitle(header)
                .setStickerColors(colors.toArray(new String[]{}))
                .build();
        timetable.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        for (ArrayList<Schedule> schedules : timetableContent) {
            timetable.add(schedules);
        }

        ((LinearLayout) findViewById(R.id.summary_linear)).addView(timetable);

/*        timetable.setOnStickerSelectEventListener((idx, schedules1) -> {
            CustomSchedule schedule = (CustomSchedule) schedules1.get(idx);
            Week week = schedule.getWeek();
            final View alertLayout = this.getLayoutInflater().inflate(R.layout.dialog_add_subject, null);
            AlertDialogsHelper.getEditSubjectDialog(this, alertLayout, () -> setupTimetableLibrary2(), week);
        });*/

    }

    private static class CustomSchedule extends Schedule {
        private Week week;

        CustomSchedule(Week w, int day) {
            super();
            this.week = w;

            int startHour = Integer.parseInt(w.getFromTime().substring(0, w.getFromTime().indexOf(":")));
            int startMinute = Integer.parseInt(w.getFromTime().substring(w.getFromTime().indexOf(":") + 1));

            int endHour = Integer.parseInt(w.getToTime().substring(0, w.getToTime().indexOf(":")));
            int endMinute = Integer.parseInt(w.getToTime().substring(w.getToTime().indexOf(":") + 1));

            setClassTitle(w.getSubject()); // sets subject
            setClassPlace(w.getRoom() + "\n" + w.getTeacher()); // sets place
            setProfessorName(""); // sets professor
            setStartTime(new Time(startHour, startMinute)); // sets the beginning of class time (hour,minute)
            setEndTime(new Time(endHour, endMinute)); // sets the end of class time (hour,minute)
            setDay(day);
        }

        public Week getWeek() {
            return week;
        }
    }
}
