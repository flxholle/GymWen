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

    private String courses = "";
    private boolean parents = false;
    public String courseFirstDigit = "";
    public String courseMainDigit = "";
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
            setName(extras.getString("name", getContext().getString(R.string.profile_empty_name) + (ProfileManagement.sizeProfiles() + 1)));
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
