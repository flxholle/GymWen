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
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.Profile;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.ui.fragments.ChoiceActivityFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ChoiceActivity extends ActivityFeatures {

    //ChoiceActivity -> Step 5
    public static final String[][] choiceCourseNames = new String[][]{{ApplicationFeatures.getContext().getString(R.string.math), ApplicationFeatures.getContext().getString(R.string.mathShort)},
            {ApplicationFeatures.getContext().getString(R.string.german), ApplicationFeatures.getContext().getString(R.string.germanShort)},
            {ApplicationFeatures.getContext().getString(R.string.history), ApplicationFeatures.getContext().getString(R.string.historyShort)},
            {ApplicationFeatures.getContext().getString(R.string.social_education), ApplicationFeatures.getContext().getString(R.string.social_educationShort)},
            {ApplicationFeatures.getContext().getString(R.string.PE), ApplicationFeatures.getContext().getString(R.string.PEShort)},
            {ApplicationFeatures.getContext().getString(R.string.Religious_education), ApplicationFeatures.getContext().getString(R.string.Religious_educationShort)},
            {ApplicationFeatures.getContext().getString(R.string.english), ApplicationFeatures.getContext().getString(R.string.englishShort)},
            {ApplicationFeatures.getContext().getString(R.string.france), ApplicationFeatures.getContext().getString(R.string.franceShort)},
            {ApplicationFeatures.getContext().getString(R.string.latin), ApplicationFeatures.getContext().getString(R.string.latinShort)},
            {ApplicationFeatures.getContext().getString(R.string.spanish), ApplicationFeatures.getContext().getString(R.string.spanishShort)},
            {ApplicationFeatures.getContext().getString(R.string.biology), ApplicationFeatures.getContext().getString(R.string.biologyShort)},
            {ApplicationFeatures.getContext().getString(R.string.chemistry), ApplicationFeatures.getContext().getString(R.string.chemistryShort)},
            {ApplicationFeatures.getContext().getString(R.string.physics), ApplicationFeatures.getContext().getString(R.string.physicsShort)},
            {ApplicationFeatures.getContext().getString(R.string.programming), ApplicationFeatures.getContext().getString(R.string.programmingShort)},
            {ApplicationFeatures.getContext().getString(R.string.geography), ApplicationFeatures.getContext().getString(R.string.geographyShort)},
            {ApplicationFeatures.getContext().getString(R.string.finance), ApplicationFeatures.getContext().getString(R.string.financeShort)},
            {ApplicationFeatures.getContext().getString(R.string.art), ApplicationFeatures.getContext().getString(R.string.artShort)},
            {ApplicationFeatures.getContext().getString(R.string.music), ApplicationFeatures.getContext().getString(R.string.musicShort)},
            {ApplicationFeatures.getContext().getString(R.string.W_Seminar), ApplicationFeatures.getContext().getString(R.string.W_SeminarShort)},
            {ApplicationFeatures.getContext().getString(R.string.P_Seminar), ApplicationFeatures.getContext().getString(R.string.P_SeminarShort)},
            {ApplicationFeatures.getContext().getString(R.string.profile_subject), ApplicationFeatures.getContext().getString(R.string.profile_subjectShort)},
            {ApplicationFeatures.getContext().getString(R.string.additum), ApplicationFeatures.getContext().getString(R.string.additumShort)}
    };
    private String courses = "";
    private boolean parents = false;
    private String courseFirstDigit = "";
    private String courseMainDigit = "";
    private String name = "";
    private boolean profileAdd = false;

    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);



        fab = findViewById(R.id.choice_fab);
        fab.setEnabled(false);
        fab.bringToFront();
        fab.setVisibility(View.VISIBLE);
        fab.setBackgroundTintList(ColorStateList.valueOf(ApplicationFeatures.getAccentColor(this)));
        Bundle extras = getIntent().getExtras();
        name = getString(R.string.profile_default_name);
        if (extras != null) {
            setParents(extras.getBoolean("parents", false));
            this.name = extras.getString("name",
                    getContext().getString(R.string.profile_empty_name) + (ProfileManagement.getSize() + 1));
            profileAdd = extras.getBoolean("profileAdd", false);
        }

        setFragment(1);
    }

    public void setupColors() {
        setToolbar(false);
    }

    public void setCourses(String value) {
        courses = value;
    }

    public String getCourses() {
        return courses;
    }

    public void setParents(boolean value) {
        parents = value;
    }

    public boolean getParents() {
        return parents;
    }

    public void setCourseFirstDigit(String value) {
        courseFirstDigit = value;
    }

    public String getCourseFirstDigit() {
        return courseFirstDigit;
    }

    public void setCourseMainDigit(String value) {
        courseMainDigit = value;
    }

    public String getCourseMainDigit() {
        return courseMainDigit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFragment(int nextStep) {
        Fragment fragment = null;

        if (nextStep > 0 && nextStep < 7) {
            fragment = new ChoiceActivityFragment(nextStep, fab);
        } else {
            finishChoice();
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_choice, fragment).commit();
        }
    }

    private void finishChoice() {
        //Finish
        ProfileManagement.addProfile(new Profile(courses, name));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("parents", profileAdd ? sharedPref.getBoolean("parents", false) : parents);
        editor.apply();

        Intent intent;
        if (parents || profileAdd)
            intent = new Intent(this, ProfileActivity.class);
        else
            intent = new Intent(this, MainActivity.class);

        ProfileManagement.save(true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

}
