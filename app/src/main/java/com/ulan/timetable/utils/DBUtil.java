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

package com.ulan.timetable.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlan;
import com.google.gson.Gson;
import com.ulan.timetable.TimeTableBuilder;
import com.ulan.timetable.fragments.WeekdayFragment;
import com.ulan.timetable.model.Week;

import org.jsoup.Jsoup;

import java.util.ArrayList;

public class DBUtil {
    public static final String database_prefix = "db_profile_";

    //Get DB Names from outside of builder
    @NonNull
    public static String getDBName(@NonNull AppCompatActivity activity) {
        return database_prefix + getProfilePosition(activity);
    }

    public static int getProfilePosition(@NonNull AppCompatActivity activity) {
        int sharedPref = getProfilePositionFromSharedPreferences();
        try {
            int name = activity.getIntent().getExtras().getInt(TimeTableBuilder.PROFILE_POS, -1);
            if (name == -1)
                name = activity.getParentActivityIntent().getExtras().getInt(TimeTableBuilder.PROFILE_POS, -1);

            if (name == -1 && sharedPref > 0) {
                return sharedPref;
            } else {
                return name;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sharedPref > 0)
            return sharedPref;
        else {
            activity.finish();
            return sharedPref;
        }
    }

    @NonNull
    public static String getDBNameFromSharedPreferences() {
        return database_prefix + getProfilePositionFromSharedPreferences();
    }

    private static int getProfilePositionFromSharedPreferences() {
        if (!ApplicationFeatures.coursesCheck(true))
            return -1;

        ProfileManagement.initProfiles();
        return ProfileManagement.loadPreferredProfilePosition();
    }

    @Nullable
    public static SubstitutionPlan getSubstitutionplanFromGSON(@NonNull AppCompatActivity activity) {
        try {
            String todayDoc = activity.getIntent().getExtras().getString(TimeTableBuilder.SUBSTITUTIONPLANDOC_TODAY, null);
            if (todayDoc == null)
                todayDoc = activity.getParentActivityIntent().getExtras().getString(TimeTableBuilder.SUBSTITUTIONPLANDOC_TODAY, null);

            String tomorrowDoc = activity.getIntent().getExtras().getString(TimeTableBuilder.SUBSTITUTIONPLANDOC_TOMORROW, null);
            if (tomorrowDoc == null)
                tomorrowDoc = activity.getParentActivityIntent().getExtras().getString(TimeTableBuilder.SUBSTITUTIONPLANDOC_TOMORROW, null);

            int pos = getProfilePosition(activity);

            if (todayDoc == null && tomorrowDoc == null || pos == -1) {
                return null;
            } else {
                ProfileManagement.initProfiles();

                SubstitutionPlan plan = new SubstitutionPlan(false, ProfileManagement.getProfile(pos).getCoursesArray());
                plan.setDocs(Jsoup.parse(todayDoc), Jsoup.parse(tomorrowDoc));

                if (plan.getTitle(true).getNoInternet() && plan.getTitle(false).getNoInternet())
                    return null;

                return plan;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static SubstitutionPlan getSubstitutionplanFromGSON(String gsonString) {
        Gson gson = new Gson();
        return gson.fromJson(gsonString, SubstitutionPlan.class);
    }

    public static ArrayList<Week> getAllWeeks(DbHelper dbHelper) {
        return getWeeks(dbHelper, new String[]{WeekdayFragment.KEY_MONDAY_FRAGMENT,
                WeekdayFragment.KEY_TUESDAY_FRAGMENT,
                WeekdayFragment.KEY_WEDNESDAY_FRAGMENT,
                WeekdayFragment.KEY_THURSDAY_FRAGMENT,
                WeekdayFragment.KEY_FRIDAY_FRAGMENT,
                WeekdayFragment.KEY_SATURDAY_FRAGMENT,
                WeekdayFragment.KEY_SUNDAY_FRAGMENT});
    }

    public static ArrayList<Week> getWeeks(DbHelper dbHelper, String[] keys) {
        ArrayList<Week> weeks = new ArrayList<>();
        for (String key : keys) {
            weeks.addAll(dbHelper.getWeek(key));
        }
        return weeks;
    }

    public static String getNextOccurenceOfSubject(DbHelper dbHelper, String subject) {
/*        ArrayList<Week> weeks = new ArrayList<Week>();

        Calendar calendar = Calendar.getInstance();
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                break;
            case Calendar.TUESDAY:
                break;
            case Calendar.WEDNESDAY:
                break;
            case Calendar.THURSDAY:
                break;
            case Calendar.FRIDAY:
                break;
            case Calendar.SATURDAY:
                break;
            case Calendar.SUNDAY:
                break;
        }

        String.format("%02d-%02d-%02d", year, month + 1, dayOfMonth)*/
        return ""; //TODO
    }
}
