package com.asdoi.gymwen.ui.main.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ProfileManagement;
import com.asdoi.gymwen.ProfileManagement.Profile;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.main.fragments.ChoiceActivityFragment;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ChoiceActivity extends ActivityFeatures {

    public String courses = "";
    public boolean parents = false;
    public String courseFirstDigit = "";
    public String courseMainDigit = "";

    public String name = "";

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
                ProfileManagement.addProfile(new ProfileManagement.Profile(courses, name));
                fragment = new ChoiceActivityFragment(8, fab);
            } else {
                setSettings();
                finish();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }


        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
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

    private class ProfileListAdapter extends ArrayAdapter<String[]> {

        public ProfileListAdapter(Context con, int resource) {
            super(con, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_parentchoice_entry, null);
            }

            return generateView(convertView, position);
        }

        @Override
        public int getCount() {
            return ProfileManagement.profileQuantity();
        }

        private View generateView(View base, int position) {
            Profile p = ProfileManagement.getProfile(position);
            TextView name = base.findViewById(R.id.parentlist_name);
            name.setText(p.getName());

            TextView courses = base.findViewById(R.id.parentlist_courses);
            courses.setText(p.getCourses());

            return base;
        }
    }

}
