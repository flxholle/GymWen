package com.ulan.timetable.activities;

import android.os.Bundle;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
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
}
