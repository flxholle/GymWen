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

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.fragments.RoomPlanFragment;

public class RoomPlanActivity extends ActivityFeatures {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_room_plan);

        RoomPlanFragment roomPlanFragment;

        Bundle extras = getIntent().getExtras();
        String room = null;
        if (extras != null) {
            room = extras.getString(RoomPlanFragment.SELECT_ROOM, null);
        }
        if (room != null) {
            roomPlanFragment = RoomPlanFragment.newInstance(room);
        } else
            roomPlanFragment = new RoomPlanFragment();


        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.room_plan_frame, roomPlanFragment).commit();
    }

    public void setupColors() {
        setToolbar(true);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
