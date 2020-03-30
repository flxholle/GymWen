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

import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.RoomPlanActivity;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RoomPlanSearchFragment extends Fragment {
    private ListView listView;
    private Map<String, PointF> content;
    private String[] contentKeys;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_room_plan_search, container, false);

        content = RoomPlanActivity.getRoomMarkers();
        contentKeys = content.keySet().toArray(new String[]{});

        listView = root.findViewById(R.id.room_plan_search_list);
        listView.setAdapter(new SearchListAdapter(getContext(), 0));

        ((EditText) root.findViewById(R.id.room_plan_search_input)).addTextChangedListener(new TextWatcher() {
            @NonNull
            String before = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(@NonNull CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(before)) {
                    if (charSequence.length() > 0) {
                        search("" + charSequence);
                    } else {
                        content = RoomPlanActivity.getRoomMarkers();
                    }
                    before = charSequence.toString();
                    contentKeys = content.keySet().toArray(new String[]{});
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        return root;
    }

    private void search(String query) {
        Map<String, PointF> all = RoomPlanActivity.getRoomMarkers();
        Map<String, PointF> matches = new HashMap<>(0);
        for (String n : all.keySet()) {
            if (n.toUpperCase().contains(query.toUpperCase())) {
                matches.put(n, all.get(n));
            }
        }
        content = matches;
    }

    private class SearchListAdapter extends ArrayAdapter<String[]> {

        SearchListAdapter(@NonNull Context con, int resource) {
            super(con, resource);
        }

        @NotNull
        @Override
        public View getView(int position, @Nullable View convertView, @NotNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_room_search_entry, null);
            }

            return createView(convertView, position);
        }

        @Override
        public int getCount() {
            return content.size();
        }

        private View createView(View base, int position) {
            base.setOnClickListener((View v) -> {
                ((RoomPlanActivity) getActivity()).showRoom(contentKeys[position]);
            });

            base.findViewById(R.id.room_search_button).setOnClickListener((View v) -> {
                ((RoomPlanActivity) getActivity()).showRoom(contentKeys[position]);
            });

            TextView room = base.findViewById(R.id.room_search_room);
            room.setText(contentKeys[position]);

            TextView level = base.findViewById(R.id.room_search_floor);
            level.setText(RoomPlanActivity.getMatchingFloor(contentKeys[position]));

            return base;
        }
    }
}
