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

package com.asdoi.gymwen.ui.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.RoomPlanActivity;
import com.onlylemi.mapview.library.MapView;
import com.onlylemi.mapview.library.MapViewListener;
import com.onlylemi.mapview.library.layer.BitmapLayer;
import com.onlylemi.mapview.library.layer.MarkLayer;
import com.pd.chocobar.ChocoBar;

import java.util.ArrayList;
import java.util.List;

public class RoomPlanFragment extends Fragment {
    private static Bitmap roomPlanBitmap;
    private static Bitmap markerBitmap;

    private View root;

    private RoomPlanActivity.Room selectRoom;
    private boolean shouldSelectRoom = false;

    private MapView mapView;

    @NonNull
    public static RoomPlanFragment newInstance(int index) {
        Bundle args = new Bundle();
        args.putInt(RoomPlanActivity.SELECT_ROOM, index);

        RoomPlanFragment fragment = new RoomPlanFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            generateMarks();
            shouldSelectRoom = false;

            int roomName = requireArguments().getInt(RoomPlanActivity.SELECT_ROOM, -1);
            if (roomName >= 0 && roomName < getRooms().size()) {
                RoomPlanActivity.Room r = getRooms().get(roomName);
                selectRoom = r;
                shouldSelectRoom = true;
            }
        } catch (Exception ignore) {
            //No Arguments set
            shouldSelectRoom = false;
        }
        generateMarks();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_room_plan, container, false);

        mapView = root.findViewById(R.id.mapview);
        mapView.setVisibility(View.GONE);

        ((ActivityFeatures) requireActivity()).createLoadingPanel((ViewGroup) root);

        loadMap();
        return root;
    }

    private void loadMap() {

        new Thread(() -> {
            //Load Bitmaps
            final Bitmap roomPlan;
            final Bitmap marker;
            if (roomPlanBitmap == null) {
                roomPlan = BitmapFactory.decodeResource(requireContext().getResources(), R.drawable.roomplan);
            } else {
                roomPlan = roomPlanBitmap;
            }
            if (shouldSelectRoom && markerBitmap == null) {
                marker = BitmapFactory.decodeResource(requireContext().getResources(), R.drawable.marker_bitmap);
            } else {
                marker = markerBitmap;
            }

            roomPlanBitmap = roomPlan;
            markerBitmap = marker;

            //Load Indoor Map
            requireActivity().runOnUiThread(() -> {
                ActivityFeatures.removeLoadingPanel((ViewGroup) root);
                mapView.setVisibility(View.VISIBLE);
                mapView.loadMap(roomPlan);
                mapView.setMapViewListener(new MapViewListener() {
                    @Override
                    public void onMapLoadSuccess() {
                        List<PointF> marks = getRoomLocations();
                        List<String> marksName = getRoomNames();

                        requireActivity().runOnUiThread(() -> {
                            if (shouldSelectRoom) {
                                BitmapLayer bitmapLayer = new BitmapLayer(mapView, marker);
                                bitmapLayer.setLocation(marks.get(0));
                                mapView.addLayer(bitmapLayer);
                                RoomPlanActivity.snackbar = ChocoBar.builder().setActivity(requireActivity())
                                        .setText(selectRoom.getName() + " (" + selectRoom.getFloor() + (selectRoom.hasDescription() ? ", " + selectRoom.getDescription() : "") + ")")
                                        .setTextTypefaceStyle(Typeface.BOLD)
                                        .setIcon(R.mipmap.mark_touch)
                                        .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                        .setBackgroundColor(Color.GRAY)
                                        .build();
                                RoomPlanActivity.snackbar.show();
                            } else {
                                MarkLayer markLayer = new MarkLayer(mapView, marks, marksName);
                                markLayer.setMarkIsClickListener((int num) -> requireActivity().runOnUiThread(() -> {
                                    RoomPlanActivity.snackbar = ChocoBar.builder().setActivity(requireActivity())
                                            .setText(getRoomNames().get(num) + " (" + getRooms().get(num).getFloor() + (getRooms().get(num).hasDescription() ? ", " + getRooms().get(num).getDescription() : "") + ")")
                                            .setTextTypefaceStyle(Typeface.BOLD)
                                            .setIcon(R.mipmap.mark_touch)
                                            .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                            .setBackgroundColor(Color.GRAY)
                                            .build();
                                    RoomPlanActivity.snackbar.show();
                                }));

                                markLayer.setNum(0);
                                mapView.addLayer(markLayer);
                            }
                            mapView.setCurrentRotateDegrees(0);
                            mapView.setRotateable(false);
                            mapView.refresh();
                            mapView.performClick();
                        });
                    }

                    @Override
                    public void onMapLoadFail() {
                        requireActivity().runOnUiThread(() -> {
                            RoomPlanActivity.snackbar = ChocoBar.builder().setActivity(requireActivity())
                                    .setText(R.string.cannot_load_room_plan)
                                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                    .setActionText(R.string.ok)
                                    .setActionClickListener((View v) -> requireActivity().finish())
                                    .red();
                            RoomPlanActivity.snackbar.show();
                        });
                    }
                });
            });
        }).start();
    }

    private List<RoomPlanActivity.Room> roomMarks;

    private void generateMarks() {
        roomMarks = RoomPlanActivity.getRoomMarkers();

        if (shouldSelectRoom) {
            roomMarks = new ArrayList<>(0);
            roomMarks.add(selectRoom);
        }
    }

    private List<RoomPlanActivity.Room> getRooms() {
        if (roomMarks == null)
            generateMarks();

        return roomMarks;
    }

    @NonNull
    private List<String> getRoomNames() {
        List<String> names = new ArrayList<>(0);
        for (RoomPlanActivity.Room r : getRooms()) {
            names.add(r.getName());
        }
        return names;
    }

    @NonNull
    private List<PointF> getRoomLocations() {
        List<PointF> locations = new ArrayList<>(0);
        for (RoomPlanActivity.Room r : getRooms()) {
            locations.add(r.getLocation());
        }
        return locations;
    }
}
