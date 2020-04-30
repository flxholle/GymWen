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

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
import com.ulan.timetable.fragments.SummarySettingsFragment;

public class SummarySettingsActivity extends ActivityFeatures {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_summary_settings);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, new SummarySettingsFragment())
                .commit();
    }

    @Override
    public void setupColors() {
        setToolbar(true);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SummaryActivity.class));
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
