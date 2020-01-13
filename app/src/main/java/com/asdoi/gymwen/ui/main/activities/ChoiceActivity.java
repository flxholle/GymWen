package com.asdoi.gymwen.ui.main.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.Profile;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.ui.main.fragments.ChoiceActivityFragment;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ChoiceActivity extends ActivityFeatures {

    private String courses = "";
    private boolean parents = false;
    public String courseFirstDigit = "";
    public String courseMainDigit = "";
    private String name = "";

    private FloatingActionButton fab;

//    public static ChoiceActivity newInstance(String name){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("Kurs-/Klassenwahl");

        fab = findViewById(R.id.choice_fab);
        fab.setEnabled(false);
        fab.bringToFront();
        fab.setVisibility(View.VISIBLE);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            setParents(extras.getBoolean("parents", false));
            setName(extras.getString("name", ""));
        }

        setFragment(1);
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
            //Finish
            if (parents) {
                ProfileManagement.addProfile(new Profile(courses, name));
                parents = false;
                name = "";
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                finish();
            } else {
                setSettings();
                finish();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }


        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_choice, fragment).commit();
        }
    }

    //Finished and setSettings
    private void setSettings() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        VertretungsPlanFeatures.setup(false, courses.split("#"));

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("courses", courses);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

}
