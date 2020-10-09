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
import com.pd.chocobar.ChocoBar;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class RoomPlanActivity extends ActivityFeatures {

    @NonNull
    public static final String SELECT_ROOM = "selectroom";
    @NonNull
    public static final String SELECT_ROOM_NAME = "selectroomName";
    @NonNull
    public static final String SEARCH_ROOM = "searchroom";

    private boolean search = false;

    public static Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_room_plan);

        String room = null;
        int roomIndex = -1;
        List<Room> roomMarkers = getRoomMarkers();
        try {
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                room = extras.getString(SELECT_ROOM_NAME, null);
                roomIndex = extras.getInt(SELECT_ROOM, -1);
            }

            search = Objects.requireNonNull(getIntent().getAction()).equals(SEARCH_ROOM);
        } catch (NullPointerException ignore) {
            //If intent is null
        }

        if (room != null && !room.trim().isEmpty()) {
            for (int i = 0; i < roomMarkers.size(); i++) {
                if (roomMarkers.get(i).getName().equalsIgnoreCase(room)) {
                    roomIndex = i;
                    break;
                }
            }

            if ((roomIndex < 0 || roomIndex > roomMarkers.size()) && !search) {
                RoomPlanActivity.snackbar = ChocoBar.builder().setActivity(this)
                        .setActionText(getString(R.string.ok))
                        .setText(getString(R.string.room) + " " + room + " " + getString(R.string.not_found))
                        .setDuration(ChocoBar.LENGTH_INDEFINITE)
                        .setIcon(R.drawable.ic_404_error)
                        .orange();
                RoomPlanActivity.snackbar.show();
            }
        }

        setIntent(null);

        Fragment fragment;

        if (search) {
            fragment = new RoomPlanSearchFragment();
        } else {
            if (roomIndex >= 0 && roomIndex < roomMarkers.size())
                fragment = RoomPlanFragment.newInstance(roomIndex);
            else
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
        rooms.add(new Room(context.getString(R.string.assembly_hall), 0, new PointF(1350, 1790)));
        rooms.add(new Room("H1", null, context.getString(R.string.gym), new PointF(1950, 1480)));
        rooms.add(new Room("H2", null, context.getString(R.string.gym), new PointF(2070, 1510)));
        rooms.add(new Room("H3", null, context.getString(R.string.gym), new PointF(2200, 1550)));

        //Main floor
        rooms.add(new Room("040", context.getString(R.string.music), new PointF(1280, 1550)));
        rooms.add(new Room("041", context.getString(R.string.music), new PointF(1500, 1620)));
        rooms.add(new Room("002", context.getString(R.string.programming), new PointF(1440, 2040)));
        rooms.add(new Room("003", new PointF(1360, 2010)));
        rooms.add(new Room(context.getString(R.string.wc), 0, new PointF(1240, 1970)));
        rooms.add(new Room("012", context.getString(R.string.lost_and_found), new PointF(1160, 1950)));
        rooms.add(new Room("013", new PointF(1110, 1940)));
        rooms.add(new Room("014", context.getString(R.string.homework_room), new PointF(1050, 1920)));
        rooms.add(new Room("015", new PointF(990, 1900)));
        rooms.add(new Room("016", new PointF(940, 1890)));
        rooms.add(new Room(context.getString(R.string.cafeteria), 0, new PointF(810, 1850)));
        rooms.add(new Room("021", context.getString(R.string.senior_room), new PointF(470, 1750)));
        rooms.add(new Room("E004", context.getString(R.string.music), new PointF(220, 1670)));
        rooms.add(new Room("E003", context.getString(R.string.engineering_room), new PointF(360, 1715)));
        rooms.add(new Room(context.getString(R.string.student_council), 0, new PointF(410, 1735)));
        rooms.add(new Room("E006", new PointF(180, 1380)));
        rooms.add(new Room("E011", context.getString(R.string.art), new PointF(400, 1300)));
        rooms.add(new Room("035", context.getString(R.string.art), new PointF(600, 1370)));
        rooms.add(new Room("036", context.getString(R.string.art), new PointF(740, 1410)));
        rooms.add(new Room("022", context.getString(R.string.ogts), new PointF(480, 1630)));
        rooms.add(new Room("023", context.getString(R.string.ogts), new PointF(510, 1540)));
        rooms.add(new Room("033", context.getString(R.string.art_working_room), new PointF(780, 1510)));
        rooms.add(new Room(context.getString(R.string.wc), context.getString(R.string.male_short), 0, new PointF(695, 1485)));
        rooms.add(new Room(context.getString(R.string.wc), context.getString(R.string.female_short), 0, new PointF(640, 1685)));

        //First floor
        rooms.add(new Room("105", context.getString(R.string.library), new PointF(1400, 1410)));
        rooms.add(new Room("106", context.getString(R.string.library), new PointF(1200, 1350)));
        rooms.add(new Room("112", new PointF(1090, 1330)));
        rooms.add(new Room("113", new PointF(1030, 1313)));
        rooms.add(new Room("114", new PointF(970, 1296)));
        rooms.add(new Room("115", new PointF(910, 1279)));
        rooms.add(new Room("116", new PointF(850, 1262)));
        rooms.add(new Room("117", new PointF(790, 1245)));
        rooms.add(new Room("118", new PointF(730, 1228)));
        rooms.add(new Room("119", new PointF(670, 1211)));
        rooms.add(new Room("132", new PointF(720, 1105)));
        rooms.add(new Room("133", new PointF(770, 945)));
        rooms.add(new Room("121", new PointF(460, 1150)));
        rooms.add(new Room("E108", new PointF(210, 700)));
        rooms.add(new Room("E107", new PointF(185, 765)));
        rooms.add(new Room("E105", new PointF(140, 940)));
        rooms.add(new Room("E104", new PointF(120, 1000)));
        rooms.add(new Room("E103", new PointF(150, 1065)));
        rooms.add(new Room("E112", context.getString(R.string.senior_helpers), new PointF(420, 730)));
        rooms.add(new Room("124", new PointF(540, 880)));
        rooms.add(new Room("123", new PointF(490, 1050)));
        rooms.add(new Room("122", new PointF(500, 985)));
        rooms.add(new Room("142", new PointF(1180, 940)));
        rooms.add(new Room("141", new PointF(1120, 922)));
        rooms.add(new Room("140", new PointF(1060, 905)));
        rooms.add(new Room("139", new PointF(1000, 888)));
        rooms.add(new Room("138", new PointF(940, 871)));
        rooms.add(new Room("137", new PointF(880, 854)));
        rooms.add(new Room("136", new PointF(820, 837)));
        rooms.add(new Room("135", new PointF(760, 820)));
        rooms.add(new Room("149", context.getString(R.string.first_aid), new PointF(1210, 990)));
        rooms.add(new Room("108", new PointF(1170, 1080)));
        rooms.add(new Room("109", new PointF(1140, 1190)));
        rooms.add(new Room("150", context.getString(R.string.director_office), new PointF(1310, 1030)));
        rooms.add(new Room("151", context.getString(R.string.office), new PointF(1410, 1060)));
        rooms.add(new Room("152", context.getString(R.string.director_office), new PointF(1520, 1090)));
        rooms.add(new Room(context.getString(R.string.teachers_room), 1, new PointF(1520, 1260)));
        rooms.add(new Room(context.getString(R.string.wc), 1, new PointF(270, 1110)));
        rooms.add(new Room(context.getString(R.string.wc), 1, new PointF(1180, 900)));
        rooms.add(new Room(context.getString(R.string.wc), context.getString(R.string.male_short), 1, new PointF(700, 925)));
        rooms.add(new Room(context.getString(R.string.wc), context.getString(R.string.female_short), 1, new PointF(655, 1085)));

        //Second floor
        rooms.add(new Room("206", new PointF(1245, 785)));
        rooms.add(new Room("205", new PointF(1375, 820)));
        rooms.add(new Room("204", new PointF(1490, 850)));
        rooms.add(new Room("203", new PointF(1520, 740)));
        rooms.add(new Room("202", new PointF(1560, 600)));
        rooms.add(new Room("246", new PointF(1340, 420)));
        rooms.add(new Room("247", new PointF(1440, 440)));
        rooms.add(new Room("248", new PointF(1520, 470)));
        rooms.add(new Room("208", new PointF(1180, 480)));
        rooms.add(new Room("209", new PointF(1150, 600)));
        rooms.add(new Room("212", context.getString(R.string.chemistry), new PointF(1000, 720)));
        rooms.add(new Room("215", context.getString(R.string.chemistry), new PointF(810, 660)));
        rooms.add(new Room("216", context.getString(R.string.programming), new PointF(630, 620)));
        rooms.add(new Room("221", context.getString(R.string.programming), new PointF(540, 590)));
        rooms.add(new Room("E203", new PointF(165, 485)));
        rooms.add(new Room("E204", new PointF(115, 440)));
        rooms.add(new Room("E205", new PointF(140, 350)));
        rooms.add(new Room("E208", context.getString(R.string.chemistry), new PointF(195, 160)));
        rooms.add(new Room("E209", context.getString(R.string.biology), new PointF(330, 140)));
        rooms.add(new Room("E210", context.getString(R.string.biology), new PointF(540, 200)));
        rooms.add(new Room("223", new PointF(500, 460)));
        rooms.add(new Room("222", new PointF(510, 400)));
        rooms.add(new Room("224", new PointF(535, 305)));
        rooms.add(new Room("233", new PointF(780, 370)));
        rooms.add(new Room("232", new PointF(740, 530)));
        rooms.add(new Room("235", context.getString(R.string.biology), new PointF(630, 220)));
        rooms.add(new Room("236", context.getString(R.string.biology), new PointF(820, 270)));
        rooms.add(new Room("237", context.getString(R.string.physics), new PointF(860, 290)));
        rooms.add(new Room("238", context.getString(R.string.physics), new PointF(910, 300)));
        rooms.add(new Room("240", context.getString(R.string.physics), new PointF(1080, 340)));
        rooms.add(new Room(context.getString(R.string.wc), 2, new PointF(260, 510)));
        rooms.add(new Room(context.getString(R.string.wc), 2, new PointF(1200, 350)));
        rooms.add(new Room(context.getString(R.string.wc), context.getString(R.string.male_short), 2, new PointF(710, 350)));
        rooms.add(new Room(context.getString(R.string.wc), context.getString(R.string.female_short), 2, new PointF(670, 510)));

        return sortRooms(rooms);
    }

    @NonNull
    private static List<Room> sortRooms(@NonNull List<Room> rooms) {
        Map<String, Room> map = new TreeMap<>();
        for (Room room : rooms) {
            char nr = 1;
            String name = room.getName();
            while (map.containsKey(name)) {
                name = room.getName() + nr;
                nr++;
            }
            map.put(name, room);
        }

        List<Room> sortedRooms = new LinkedList<>();
        for (String i : map.keySet()) {
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

        return getMatchingFloor(level);
    }

    @NonNull
    private static String getMatchingFloor(int level) {
        Context context = ApplicationFeatures.getContext();
        switch (level) {
            case 0:
                return context.getString(R.string.main_floor);
            default:
                return level + ". " + context.getString(R.string.floor);
        }
    }

    public void showRoom(int index) {
        Fragment fragment = RoomPlanFragment.newInstance(index);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.room_plan_frame, fragment).commit();
        search = false;
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        if (search) {
            Fragment fragment = new RoomPlanFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.room_plan_frame, fragment).commit();
            search = false;
            invalidateOptionsMenu();
        } else
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
        private final String name;
        @Nullable
        private final String description;
        private final PointF location;
        private final String floor;

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

        Room(String name, int floor, PointF location) {
            this.name = name;
            this.description = "";
            this.location = location;
            this.floor = getMatchingFloor(floor);
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

        Room(String name, @Nullable String description, int floor, PointF location) {
            this.name = name;
            if (description == null)
                this.description = "";
            else
                this.description = description;
            this.location = location;
            this.floor = getMatchingFloor((floor));
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
            return !Objects.requireNonNull(description).trim().isEmpty();
        }
    }
}
