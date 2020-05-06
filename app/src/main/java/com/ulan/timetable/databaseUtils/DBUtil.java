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

package com.ulan.timetable.databaseUtils;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlan;
import com.ulan.timetable.TimeTableBuilder;
import com.ulan.timetable.utils.PreferenceUtil;

import org.jsoup.Jsoup;

import java.util.Calendar;
import java.util.Objects;

public class DBUtil {
    private static final String database_prefix = "db_profile_";
    private static final String odd_week_postfix = "_odd";

    @NonNull
    public static String getDBName(@NonNull Activity activity, @NonNull Calendar now) {
        String dbName = database_prefix + getProfilePosition(activity);
        if (PreferenceUtil.isEvenWeek(activity, now))
            return dbName;
        else
            return dbName + odd_week_postfix;
    }

    @NonNull
    public static String getDBName(@NonNull Context context, @NonNull Calendar now) {
        String dbName = database_prefix + getProfilePositionFromSharedPreferences();
        if (PreferenceUtil.isEvenWeek(context, now))
            return dbName;
        else
            return dbName + odd_week_postfix;
    }

    public static int getProfilePosition(@NonNull Activity activity) {
        int sharedPref = getProfilePositionFromSharedPreferences();
        try {
            if (activity.getIntent().getExtras() != null) {
                int themeId = activity.getIntent().getExtras().getInt(TimeTableBuilder.CUSTOM_THEME, -1);
                if (themeId != -1) {
                    activity.setTheme(themeId);
                }
            }
        } catch (Exception ignore) {
        }

        try {
            int name = Objects.requireNonNull(activity.getIntent().getExtras()).getInt(TimeTableBuilder.PROFILE_POS, -1);
            if (name == -1)
                name = Objects.requireNonNull(Objects.requireNonNull(activity.getParentActivityIntent()).getExtras()).getInt(TimeTableBuilder.PROFILE_POS, -1);

            if (name == -1 && sharedPref >= 0) {
                return sharedPref;
            } else {
                return name;
            }
        } catch (Exception ignore) {
        }

        if (sharedPref >= 0)
            return sharedPref;
        else {
            activity.finish();
            return sharedPref;
        }
    }

    private static int getProfilePositionFromSharedPreferences() {
        if (!ApplicationFeatures.initSettings(false, false))
            return -1;

        ProfileManagement.initProfiles();
        return ProfileManagement.loadPreferredProfilePosition();
    }

    @Nullable
    public static SubstitutionPlan getSubstitutionPlanFromActivity(@NonNull AppCompatActivity activity) {
        try {
            String todayDoc = Objects.requireNonNull(activity.getIntent().getExtras()).getString(TimeTableBuilder.SUBSTITUTIONPLANDOC_TODAY, null);
            if (todayDoc == null)
                todayDoc = Objects.requireNonNull(Objects.requireNonNull(activity.getParentActivityIntent()).getExtras()).getString(TimeTableBuilder.SUBSTITUTIONPLANDOC_TODAY, null);

            String tomorrowDoc = activity.getIntent().getExtras().getString(TimeTableBuilder.SUBSTITUTIONPLANDOC_TOMORROW, null);
            if (tomorrowDoc == null)
                tomorrowDoc = Objects.requireNonNull(Objects.requireNonNull(activity.getParentActivityIntent()).getExtras()).getString(TimeTableBuilder.SUBSTITUTIONPLANDOC_TOMORROW, null);

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

}
