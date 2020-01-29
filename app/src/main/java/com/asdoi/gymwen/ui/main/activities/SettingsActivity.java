package com.asdoi.gymwen.ui.main.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.Profile;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;

public class SettingsActivity extends ActivityFeatures {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setSettings();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setSettings();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void setSettings() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String courses = sharedPref.getString("courses", "");

        if (!ApplicationFeatures.coursesCheck())
            return;
        ProfileManagement.editProfile(ApplicationFeatures.getSelectedProfilePosition(), new Profile(courses, ApplicationFeatures.getSelectedProfile().getName()));
        ProfileManagement.save(true);

        boolean hours = sharedPref.getBoolean("hours", false);

        VertretungsPlanFeatures.setup(hours, courses.split("#"));

        String username = sharedPref.getString("username", "");
        String password = sharedPref.getString("password", "");
        VertretungsPlanFeatures.signin(username, password);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            setNotif();
            Preference myPref = findPreference("showNotification");
            myPref.setOnPreferenceClickListener((Preference preference) -> {
                setNotif();
                return true;
            });

            setBorder();
            myPref = findPreference("show_borders");
            myPref.setOnPreferenceClickListener((Preference preference) -> {
                setBorder();
                return true;
            });

            myPref = findPreference("alarm");
            myPref.setOnPreferenceClickListener((Preference p) -> {
                if (ApplicationFeatures.isAlarmOn()) {
                    createTimePicker((ActivityFeatures) getActivity());
                } else {
                    ApplicationFeatures.setAlarmTime(-1, -1, -1);
                }
                return true;
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences var1, String var2) {
            if (ApplicationFeatures.getAlarmTime()[0] == -1) {
                SharedPreferences sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("alarm", false);
                editor.apply();
            }
        }

        private void setNotif() {
            boolean showNotif = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("showNotification", false);
            findPreference("alwaysNotification").setEnabled(showNotif);
            findPreference("alarm").setEnabled(showNotif);
            findPreference("two_notifs").setEnabled(showNotif);
        }

        private void setBorder() {
            boolean showNotif = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("show_borders", false) && !PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("hide_gesamt", false);
            findPreference("show_border_specific").setEnabled(showNotif);
        }
    }
}