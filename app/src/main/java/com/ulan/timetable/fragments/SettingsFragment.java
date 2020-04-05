package com.ulan.timetable.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.SettingsActivity;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.timetable_settings, rootKey);

        Preference allPrefs = findPreference("allprefs");
        allPrefs.setOnPreferenceClickListener((Preference p) -> {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            getActivity().finish();
            return true;
        });

        setNotif();

        Preference myPref = findPreference("timetableNotif");
        myPref.setOnPreferenceClickListener((Preference preference) -> {
            setNotif();
            return true;
        });
    }

    private void setNotif() {
        boolean showNotif = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("timetableNotif", true);
        findPreference("alwaysNotification").setVisible(showNotif);
    }
}
