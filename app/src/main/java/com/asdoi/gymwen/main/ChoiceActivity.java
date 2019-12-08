package com.asdoi.gymwen.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.main.Fragments.ChoiceActivityFragment;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class ChoiceActivity extends ActivityFeatures {

    public static String courses = "";
    public static boolean parents = false;
    public static  String courseFirstDigit = "";
    public static String courseMainDigit = "";

    private FloatingActionButton fab;

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

    public void setCourseFirstDigit(String value){
        courseFirstDigit = value;
    }

    public String getCourseFirstDigit(){
        return courseFirstDigit;
    }

    public void setCourseMainDigit(String value){
        courseMainDigit = value;
    }

    public String getCourseMainDigit(){
        return courseMainDigit;
    }


    public void setFragment(int nextStep) {
        Fragment fragment = null;

        if (nextStep > 0 && nextStep < 7) {
            fragment = new ChoiceActivityFragment(nextStep, fab);
        } else {
            //Finish
            setSettings();
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }


        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
        }
    }


    private void setSettings(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        VertretungsPlanFeatures.setup(false, courses.split("#"));

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("courses", courses);
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

}
