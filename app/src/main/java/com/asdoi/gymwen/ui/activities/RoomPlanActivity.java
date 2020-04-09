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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.fragments.RoomPlanFragment;
import com.asdoi.gymwen.ui.fragments.RoomPlanSearchFragment;
import com.asdoi.gymwen.util.External_Const;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RoomPlanActivity extends ActivityFeatures {

    @NonNull
    public static String SELECT_ROOM = "selectroom";
    @NonNull
    public static String SEARCH = "search";

    private boolean search = false;

    public static Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_room_plan);

        String room = null;
        try {
            Bundle extras = getIntent().getExtras();

            if (extras != null)
                room = extras.getString(SELECT_ROOM, null);

            search = getIntent().getAction().equals(SEARCH);
        } catch (NullPointerException e) {
            //If intent is null
            e.printStackTrace();
        }

        setIntent(null);

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

    @NonNull
    public static List<Room> getRoomMarkers() {
        Context context = ApplicationFeatures.getContext();
        List<Room> rooms = new ArrayList<>(0);
        rooms.add(new Room("109", new PointF(1125, 1195)));
        rooms.add(new Room("221", context.getString(R.string.programming), new PointF(540, 570)));
        rooms.add(new Room("H1", null, context.getString(R.string.gym), new PointF(1920, 1440)));
        rooms.add(new Room("H2", null, context.getString(R.string.gym), new PointF(2080, 1480)));
        rooms.add(new Room("Aula", null, context.getString(R.string.main_floor), new PointF(1350, 1790)));
        rooms.add(new Room("040", context.getString(R.string.music), new PointF(1260, 1550)));
        rooms.add(new Room("041", context.getString(R.string.music), new PointF(1520, 1600)));
        rooms.add(new Room("205", new PointF(1280, 800)));
        rooms.add(new Room("E103", new PointF(150, 880)));
        return sortRooms(rooms);
    }

    @NonNull
    private static List<Room> sortRooms(@NonNull List<Room> rooms) {
        Map<Integer, Room> map = new TreeMap<Integer, Room>();
        int failed = 0;
        for (Room room : rooms) {
            int roomNr;

            try {
                if (Character.isDigit(room.getName().charAt(0))) {
                    roomNr = Integer.parseInt(room.getName());
                } else if (Character.isDigit(room.getName().charAt(1))) {
                    roomNr = Integer.parseInt(room.getName().substring(1));
                } else {
                    failed--;
                    roomNr = failed;
                    failed--;
                }
            } catch (Exception e) {
                e.printStackTrace();
                failed--;
                roomNr = failed;
            }

            map.put(roomNr, room);
        }

        List<Room> sortedRooms = new LinkedList<>();
        for (int i : map.keySet()) {
            sortedRooms.add(map.get(i));
        }

        return sortedRooms;
    }

    @NonNull
    private static String getMatchingFloor(@NonNull String roomName) {
        Context context = ApplicationFeatures.getContext();
        if (roomName.length() < 2)
            return "";

        int level;
        if (Character.isDigit(roomName.charAt(0))) {
            level = Integer.parseInt("" + roomName.charAt(0));
        } else if (Character.isDigit(roomName.charAt(1))) {
            level = Integer.parseInt("" + roomName.charAt(1));
        } else {
            return context.getString(R.string.unkown_floor);
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
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
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
                if (snackbar != null)
                    snackbar.dismiss();
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

    public static class Room {
        private String name;
        @Nullable
        private String description;
        private PointF location;
        private String floor;

        Room(@NonNull String name, PointF location) {
            this.name = name;
            this.description = "";
            this.location = location;
            floor = getMatchingFloor(name);
        }

        Room(@NonNull String name, @Nullable String description, PointF location) {
            this.name = name;
            if (description == null)
                this.description = "";
            else
                this.description = description;
            this.location = location;
            floor = getMatchingFloor(name);
        }

        Room(String name, @Nullable String description, String floor, PointF location) {
            this.name = name;
            if (description == null)
                this.description = "";
            else
                this.description = description;
            this.location = location;
            this.floor = floor;
        }

        public String getName() {
            return name;
        }

        @Nullable
        public String getDescription() {
            return description;
        }

        public String getFloor() {
            return floor;
        }

        public PointF getLocation() {
            return location;
        }

        public boolean hasDescription() {
            return !description.trim().isEmpty();
        }
    }
}
