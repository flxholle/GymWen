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

import android.app.Activity;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlan;
import com.google.gson.Gson;
import com.ulan.timetable.TimeTableBuilder;

import org.jsoup.Jsoup;

public class DBUtil {
    public static final String database_prefix = "db_profile_";

    //Get DB Names from outside of builder
    public static String getDBName(Activity activity) {
        try {
            String name = activity.getIntent().getExtras().getString(TimeTableBuilder.DB_NAME, null);
            if (name == null)
                name = activity.getParentActivityIntent().getExtras().getString(TimeTableBuilder.DB_NAME, null);

            if (name == null) {
                return getDBNameFromSharedPreferences();
            } else {
                return name;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return getDBNameFromSharedPreferences();
    }

    public static String getDBNameFromSharedPreferences() {
        if (!ApplicationFeatures.coursesCheck(true))
            return "nullDatabase";

        if (!ProfileManagement.isUninit())
            ProfileManagement.reload();

        int pos = ProfileManagement.loadPreferredProfilePosition();

        return database_prefix + pos;
    }

    public static SubstitutionPlan getSubstitutionplanFromGSON(Activity activity) {
        try {
            String todayDoc = activity.getIntent().getExtras().getString(TimeTableBuilder.SUBSTITUTIONPLANDOC_TODAY, null);
            if (todayDoc == null)
                todayDoc = activity.getParentActivityIntent().getExtras().getString(TimeTableBuilder.SUBSTITUTIONPLANDOC_TODAY, null);

            String tomorrowDoc = activity.getIntent().getExtras().getString(TimeTableBuilder.SUBSTITUTIONPLANDOC_TOMORROW, null);
            if (tomorrowDoc == null)
                tomorrowDoc = activity.getParentActivityIntent().getExtras().getString(TimeTableBuilder.SUBSTITUTIONPLANDOC_TOMORROW, null);

            int pos = activity.getIntent().getExtras().getInt(TimeTableBuilder.PROFILE_POS, -1);
            if (pos == -1)
                pos = activity.getParentActivityIntent().getExtras().getInt(TimeTableBuilder.PROFILE_POS, -1);

            if (todayDoc == null && tomorrowDoc == null || pos == -1) {
                return null;
            } else {
                if (!ProfileManagement.isUninit())
                    ProfileManagement.reload();

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
}
