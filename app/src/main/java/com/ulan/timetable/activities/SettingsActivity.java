package com.ulan.timetable.activities;

import android.os.Bundle;

import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.Profile;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.ulan.timetable.fragments.SettingsFragment;

public class SettingsActivity extends ActivityFeatures {
    public static final String KEY_SEVEN_DAYS_SETTING = "sevendays";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_settings);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
    }

    @Override
    public void setupColors() {
        setToolbar(true);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Profile p = ApplicationFeatures.getSelectedProfile();
        String newCourses = PreferenceManager.getDefaultSharedPreferences(this).getString("courses", p.getCourses());
        if (!newCourses.trim().isEmpty()) {
            p.setCourses(newCourses);
            ProfileManagement.editProfile(ApplicationFeatures.getSelectedProfilePosition(), p);
            ProfileManagement.save(true);
        }
    }
}
