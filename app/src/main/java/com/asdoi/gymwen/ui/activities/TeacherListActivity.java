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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.teacherlist.TeacherlistFeatures;
import com.asdoi.gymwen.ui.fragments.SubstitutionFragment;
import com.asdoi.gymwen.ui.fragments.TeacherListFragment;

public class TeacherListActivity extends ActivityFeatures {
    @NonNull
    public static final String SEARCH_TEACHER = "searchteacher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacherlist);

        String teacherQuery = null;
        try {
            Bundle extras = getIntent().getExtras();

            if (extras != null)
                teacherQuery = extras.getString(SEARCH_TEACHER, null);

        } catch (NullPointerException e) {
            //If intent is null
            e.printStackTrace();
        }

        setIntent(null);

        Fragment fragment;
        if (teacherQuery != null) {
            fragment = TeacherListFragment.newInstance(teacherQuery);
        } else
            fragment = new TeacherListFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.teacherlist_frame, fragment).commit();
    }

    public void setupColors() {
        setToolbar(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_teacherlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onOptionsItemSelected(item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    public void onOptionsItemSelected(int id) {
        if (id == R.id.action_refresh) {
            ApplicationFeatures.deleteOfflineTeacherlistDoc();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.teacherlist_frame, new TeacherListFragment()).commit();
        }
    }

    public static void removeTeacherClick(@NonNull View view, Context context) {
        view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        view.setBackgroundResource(0);
        view.setClickable(false);
        view.setOnClickListener(null);
    }

    //TeacherSearch
    public static void teacherClick(@NonNull TextView view, @NonNull String teacherQuery, boolean fullNames, Activity activity) {
        if (TeacherlistFeatures.isAOL(teacherQuery))
            return;
        TypedValue outValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        view.setBackgroundResource(outValue.resourceId);


        if (fullNames) {
            new Thread(() -> {
                ApplicationFeatures.downloadTeacherlistDoc();
                try {
                    activity.runOnUiThread(() -> {
                        String match = SubstitutionFragment.getMatchingTeacher(teacherQuery);
                        if (match != null)
                            view.setText(match);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            view.setText(teacherQuery);
        }


        view.setClickable(true);
        view.setOnClickListener((View v) -> {
            //TeacherList Activity
            Intent i = new Intent(activity, TeacherListActivity.class);
            i.putExtra(SEARCH_TEACHER, teacherQuery);
            activity.startActivity(i);
        });
    }
}
