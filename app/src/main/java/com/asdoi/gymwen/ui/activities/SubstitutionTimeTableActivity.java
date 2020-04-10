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

package com.asdoi.gymwen.ui.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlan;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures;
import com.ulan.timetable.TimeTableBuilder;

public class SubstitutionTimeTableActivity extends ActivityFeatures {
    @NonNull
    public static final String PROFILE_POSITION = "profilepos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_substitution_time_table);
    }

    @Override
    public void onStart() {
        super.onStart();
        createLoadingPanel(findViewById(R.id.substitution_time_frame));

        ProfileManagement.initProfiles();

        int pos = -1;
        if (getIntent().getExtras() != null) {
            pos = getIntent().getExtras().getInt(PROFILE_POSITION, -1);
            if (pos >= ProfileManagement.getSize())
                pos = -1;
        }

        if (pos == -1)
            pos = ProfileManagement.loadPreferredProfilePosition();

        final int finalPos = pos;
        new Thread(() -> {
            ApplicationFeatures.downloadSubstitutionplanDocs(false, true);
            if (finalPos >= ProfileManagement.getSize()) {
                runOnUiThread(this::finish);
            } else {
                SubstitutionPlan substitutionPlan = SubstitutionPlanFeatures.createTempSubstitutionplan(false, ProfileManagement.getProfile(finalPos).getCoursesArray());
                runOnUiThread(() -> {
                    new TimeTableBuilder(finalPos, substitutionPlan).start(this);
                    finish();
                });
            }
        }).start();
    }

    @Override
    public void setupColors() {
        setToolbar(false);
    }
}
