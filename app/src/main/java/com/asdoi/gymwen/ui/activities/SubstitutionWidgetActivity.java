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

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.Profile;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.widgets.SubstitutionWidgetProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SubstitutionWidgetActivity extends ActivityFeatures {
    public static final String PROFILES = "profiles";
    public static final char divider = '%';
    public static final String PREF_PREFIX_KEY = "prefix_";


    public int appWidgetId;
    public ArrayList<Integer> selectedProfiles;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Set the view layout resource to use.
        setContentView(R.layout.activity_widget_profile_selection);

        if (!ApplicationFeatures.initSettings(false, true)) {
            finish();
            return;
        }

        selectedProfiles = loadPref(getContext(), appWidgetId);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        // If they gave us an intent without the widget id, just bail.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        if (ProfileManagement.isUninit())
            ProfileManagement.reload();

        ListView listView = findViewById(R.id.widget_creation_profile_list);
        listView.setAdapter(new ProfileListAdapter(getContext(), 0));
    }

    @Override
    public void onStart() {
        super.onStart();
        findViewById(R.id.fab).setOnClickListener((View v) -> {
            savePref();

            new Thread(() -> {
                ApplicationFeatures.downloadSubstitutionplanDocsAlways(true, true);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
                RemoteViews views = new RemoteViews(getContext().getPackageName(), R.layout.widget_substitution);
                SubstitutionWidgetProvider.updateWidget(getContext(), appWidgetManager, appWidgetId, views);
                appWidgetManager.updateAppWidget(appWidgetId, views);

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }).start();
        });
    }

    public void setupColors() {
//        setToolbar(true);
    }

    public void savePref() {
        if (selectedProfiles.size() == 0) {
            for (int i = 0; i < ProfileManagement.getProfileList().size(); i++) {
                selectedProfiles.add(i);
            }
            runOnUiThread(() -> {
                Toast.makeText(getContext(), R.string.selected_all_profiles, Toast.LENGTH_SHORT).show();
            });
        }

        StringBuilder s = new StringBuilder();
        for (int i : selectedProfiles) {
            s.append(i);
            s.append(divider);
        }
        s.deleteCharAt(s.length() - 1);

        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                .putString(PREF_PREFIX_KEY + appWidgetId, s.toString())
                .commit();
    }

    public static ArrayList<Integer> loadPref(Context context, int appWidgetId) {
        String s = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (s == null)
            return new ArrayList<>(0);
        else {
            ArrayList<Integer> arrayList = new ArrayList<>(0);
            String[] profiles = s.split(divider + "");
            for (String s1 : profiles) {
                arrayList.add(Integer.parseInt(s1));
            }
            return arrayList;
        }
    }

    class ProfileListAdapter extends ArrayAdapter<String[]> {

        ProfileListAdapter(@NonNull Context con, int resource) {
            super(con, resource);
        }

        @NotNull
        @Override
        public View getView(int position, @Nullable View convertView, @NotNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_profiles_entry, null);
            }

            return generateView(convertView, position);
        }

        @Override
        public int getCount() {
            return ProfileManagement.getSize();
        }

        @NonNull
        private View generateView(@NonNull View base, int position) {
            Profile p = ProfileManagement.getProfile(position);
            TextView name = base.findViewById(R.id.profilelist_name);
            name.setText(p.getName());

            TextView courses = base.findViewById(R.id.profilelist_courses);
            courses.setText(p.getCourses());

            ImageButton edit = base.findViewById(R.id.profilelist_edit);
            edit.setVisibility(View.GONE);

            ImageButton delete = base.findViewById(R.id.profilelist_delete);
            delete.setVisibility(View.GONE);

            ImageButton star = base.findViewById(R.id.profilelist_preferred);
            if (selectedProfiles.contains(position)) {
                star.setImageResource(R.drawable.ic_star_black_24dp);
            } else {
                star.setImageResource(R.drawable.ic_star_border_black_24dp);
            }
            star.setOnClickListener((View v) -> {
                if (selectedProfiles.contains(position)) {
                    selectedProfiles.remove(position);
                    star.setImageResource(R.drawable.ic_star_border_black_24dp);
                } else {
                    selectedProfiles.add(position);
                    star.setImageResource(R.drawable.ic_star_black_24dp);
                }
            });

            return base;
        }
    }
}
