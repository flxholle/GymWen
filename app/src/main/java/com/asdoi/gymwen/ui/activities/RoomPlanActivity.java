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

import android.graphics.PointF;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.fragments.RoomPlanFragment;
import com.asdoi.gymwen.ui.fragments.RoomPlanSearchFragment;

import java.util.HashMap;
import java.util.Map;

public class RoomPlanActivity extends ActivityFeatures {

    public static String SELECT_ROOM = "selectroom";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_room_plan);

        RoomPlanFragment roomPlanFragment;

        Bundle extras = getIntent().getExtras();
        String room = null;
        if (extras != null) {
            room = extras.getString(SELECT_ROOM, null);
        }
        if (room != null) {
            roomPlanFragment = RoomPlanFragment.newInstance(room);
        } else
            roomPlanFragment = new RoomPlanFragment();


        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.room_plan_frame, new RoomPlanSearchFragment()).commit();
    }

    public void setupColors() {
        setToolbar(true);
    }

    public static Map<String, PointF> getRoomMarkers() {
        Map<String, PointF> roomMarks = new HashMap<String, PointF>(0);
        roomMarks.put("109", new PointF(409, 720));
        roomMarks.put("E006", new PointF(200, 200));
        roomMarks.put("108", new PointF(800, 500));
        return roomMarks;
    }

    public static String getMatchingFloor(String roomName) {
        if (roomName.length() < 2)
            return "";

        int level = 0;
        if (Character.isDigit(roomName.charAt(0))) {
            level = Integer.parseInt("" + roomName.charAt(0));
        } else if (Character.isDigit(roomName.charAt(1))) {
            level = Integer.parseInt("" + roomName.charAt(1));
        } else {
            return "";
        }

        switch (level) {
            case 0:
                return "Erdgeschoss";
            default:
                return level + ". Stock";
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_room_plan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_room_search) {
        }
        return super.onOptionsItemSelected(item);
    }
}
