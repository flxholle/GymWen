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
import com.ulan.timetable.TimeTableBuilder;

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
}
