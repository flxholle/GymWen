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
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
import com.onlylemi.mapview.library.MapView;
import com.onlylemi.mapview.library.MapViewListener;
import com.onlylemi.mapview.library.layer.BitmapLayer;
import com.onlylemi.mapview.library.layer.MarkLayer;
import com.pd.chocobar.ChocoBar;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RoomPlanFragment extends Fragment {
    private View root;
    private Map<String, PointF> roomMarks;

    private String selectRoom;
    private boolean shouldSelectRoom = false;
    public static String SELECT_ROOM = "selectroom";

    private MapView mapView;

    public static RoomPlanFragment newInstance(String selectRoom) {

        Bundle args = new Bundle();
        args.putString(SELECT_ROOM, selectRoom);

        RoomPlanFragment fragment = new RoomPlanFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            generateMarks();
            selectRoom = getArguments().getString(SELECT_ROOM);
            if (selectRoom != null && !selectRoom.trim().isEmpty()) {
                if (getMarksName().contains(selectRoom))
                    shouldSelectRoom = true;
                else
                    ChocoBar.builder().setActivity(getActivity())
                            .setActionText(getString(R.string.ok))
                            .setText(getString(R.string.room) + " " + selectRoom + " " + getString(R.string.not_found))
                            .setDuration(ChocoBar.LENGTH_INDEFINITE)
                            .orange()
                            .show();
            }
        } catch (Exception e) {
            e.printStackTrace();

            //No Arguments set
            shouldSelectRoom = false;
        }
        generateMarks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_room_plan, container, false);

        mapView = root.findViewById(R.id.mapview);
        mapView.setVisibility(View.GONE);

        ((ActivityFeatures) getActivity()).createLoadingPanel((ViewGroup) root);

        loadMap();
        return root;
    }

    public void loadMap() {

        new Thread(() -> {
            Bitmap bitmap = null;
            Bitmap bmp = null;
            try {
                bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.roomplan);
                if (shouldSelectRoom)
                    bmp = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.mark_touch);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Bitmap finalBitmap = bitmap;
            Bitmap finalBmp = bmp;

            getActivity().runOnUiThread(() -> {
                ((ActivityFeatures) getActivity()).removeLoadingPanel((ViewGroup) root);
                mapView.setVisibility(View.VISIBLE);
                mapView.loadMap(finalBitmap);
                mapView.setMapViewListener(new MapViewListener() {
                    @Override
                    public void onMapLoadSuccess() {
                        List<PointF> marks = getMarks();
                        List<String> marksName = getMarksName();

                        if (shouldSelectRoom) {
                            BitmapLayer bitmapLayer = new BitmapLayer(mapView, finalBmp);
                            bitmapLayer.setLocation(getMarks().get(0));
                            bitmapLayer.setOnBitmapClickListener(layer -> Toast.makeText(getContext(), getString(R.string.room) + " " + marksName.get(0), Toast.LENGTH_LONG).show());
                            mapView.addLayer(bitmapLayer);
                            ChocoBar.builder().setActivity(getActivity())
                                    .setText(getString(R.string.room) + " " + selectRoom)
                                    .setTextTypefaceStyle(Typeface.BOLD)
                                    .setIcon(R.mipmap.mark_touch)
                                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                    .setBackgroundColor(Color.GRAY)
                                    .build()
                                    .show();
                        } else {
                            MarkLayer markLayer = new MarkLayer(mapView, marks, marksName);
                            markLayer.setMarkIsClickListener(num -> Toast.makeText(getContext(), getString(R.string.room) + " " + marksName.get(num), Toast.LENGTH_LONG).show());
                            markLayer.setNum(0);
                            mapView.addLayer(markLayer);
                        }

                        mapView.setCurrentRotateDegrees(0);
                        mapView.setRotateable(false);
                        mapView.refresh();
                        mapView.performClick();

                    }

                    @Override
                    public void onMapLoadFail() {
                    }

                });
            });
        }).start();
    }

    private void generateMarks() {
        roomMarks = new HashMap<String, PointF>(0);
        roomMarks.put("109", new PointF(409, 720));
        roomMarks.put("E006", new PointF(200, 200));
        roomMarks.put("108", new PointF(800, 500));

        if (shouldSelectRoom) {
            PointF room = roomMarks.get(selectRoom);
            roomMarks = new HashMap<>();
            roomMarks.put(selectRoom, room);
        }
    }

    private List<PointF> getMarks() {
        if (roomMarks == null)
            generateMarks();

        List<PointF> list = new LinkedList<>();
        for (String key : roomMarks.keySet()) {
            list.add(roomMarks.get(key));
        }
        return list;
    }

    private List<String> getMarksName() {
        if (roomMarks == null)
            generateMarks();

        return new LinkedList<>(roomMarks.keySet());
    }
}
