package com.ulan.timetable.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

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
    }
}
