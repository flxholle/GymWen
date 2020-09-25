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

package com.ulan.timetable;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.asdoi.gymwen.substitutionplan.SubstitutionPlan;
import com.ulan.timetable.activities.MainActivity;

public class TimeTableBuilder {
    public static final String CUSTOM_THEME = "customTheme";
    public static final String PROFILE_POS = "profilepos";
    public static final String SUBSTITUTIONPLANDOC_TODAY = "substitutionplandoctoday";
    public static final String SUBSTITUTIONPLANDOC_TOMORROW = "substitutionplandoctomorrow";
    public static final String DO_NOT_DOWNLOAD_DOCS_ACTION = "downloaddocs";

    private int customTheme = -1;
    private SubstitutionPlan substitutionPlan;
    private int profilePos;


    public TimeTableBuilder(int pos, SubstitutionPlan substitutionPlan) {
        setProfilePos(pos);
        setSubstitutionplan(substitutionPlan);
    }

    @NonNull
    public TimeTableBuilder withActivityTheme(int theme) {
        customTheme = theme;
        return this;
    }

    @NonNull
    public TimeTableBuilder setProfilePos(int value) {
        profilePos = value;
        return this;
    }

    @NonNull
    public TimeTableBuilder setSubstitutionplan(SubstitutionPlan value) {
        substitutionPlan = value;
        return this;
    }

    /**
     * intent() method to build and create the intent with the set params
     *
     * @return the intent to start the activity
     */
    @NonNull
    private Intent intent(Context context, Class cl) {
        Intent i = new Intent(context, cl);
        i.putExtra(CUSTOM_THEME, customTheme);
        i.putExtra(PROFILE_POS, profilePos);
        if (substitutionPlan != null && substitutionPlan.getTodayFiltered() != null) {
            i.putExtra(SUBSTITUTIONPLANDOC_TODAY, substitutionPlan.getTodayDocument().toString());
            i.putExtra(SUBSTITUTIONPLANDOC_TOMORROW, substitutionPlan.getTomorrowDocument().toString());
        }
        i.setAction(DO_NOT_DOWNLOAD_DOCS_ACTION);

        return i;
    }

    /**
     * start() method to start the application
     */
    public void start(@NonNull Context ctx) {
        Intent i = intent(ctx, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    public void start(@NonNull Context ctx, Class cls) {
        Intent i = intent(ctx, cls);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }
}
