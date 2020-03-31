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

import android.content.Context;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.fragments.RoomPlanFragment;
import com.asdoi.gymwen.ui.fragments.RoomPlanSearchFragment;
import com.asdoi.gymwen.util.External_Const;
import com.pd.chocobar.ChocoBar;

import java.util.HashMap;
import java.util.Map;

public class RoomPlanActivity extends ActivityFeatures {

    public static String SELECT_ROOM = "selectroom";
    public static String SEARCH = "search";

    private boolean search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_room_plan);

        Bundle extras = getIntent().getExtras();
        String room = null;

        if (extras != null) {
            room = extras.getString(SELECT_ROOM, null);
            search = extras.getBoolean(SEARCH, false);
        }
        Fragment fragment = null;

        if (search) {
            fragment = new RoomPlanSearchFragment();
        } else {
            if (room != null) {
                fragment = RoomPlanFragment.newInstance(room);
            } else
                fragment = new RoomPlanFragment();
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.room_plan_frame, fragment).commit();
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
        Context context = ApplicationFeatures.getContext();
        if (roomName.length() < 2)
            return "";

        int level;
        if (Character.isDigit(roomName.charAt(0))) {
            level = Integer.parseInt("" + roomName.charAt(0));
        } else if (Character.isDigit(roomName.charAt(1))) {
            level = Integer.parseInt("" + roomName.charAt(1));
        } else {
            return context.getString(R.string.gym);
        }

        switch (level) {
            case 0:
                return context.getString(R.string.main_floor);
            default:
                return level + ". " + context.getString(R.string.floor);
        }
    }

    public void showRoom(String room) {
        Fragment fragment = RoomPlanFragment.newInstance(room);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.room_plan_frame, fragment).commit();
        search = false;
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_room_plan, menu);
        menu.findItem(R.id.action_room_search).setVisible(!search);
        menu.findItem(R.id.action_room_plan).setVisible(search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.action_room_search:
                fragmentManager.beginTransaction().replace(R.id.room_plan_frame, new RoomPlanSearchFragment()).commit();
                search = true;
                //Hide all Snackbars from other fragment
                ChocoBar.builder().setActivity(this).setBackgroundColor(ApplicationFeatures.getBackgroundColor(this)).setDuration(ChocoBar.LENGTH_SHORT).build().show();
                break;
            case R.id.action_room_plan:
                fragmentManager.beginTransaction().replace(R.id.room_plan_frame, new RoomPlanFragment()).commit();
                search = false;
                break;
            case R.id.action_navigation:
                Uri gymwenOnMap = Uri.parse(External_Const.location);
                showMap(gymwenOnMap);
                break;
        }
        invalidateOptionsMenu();
        return super.onOptionsItemSelected(item);
    }
}
