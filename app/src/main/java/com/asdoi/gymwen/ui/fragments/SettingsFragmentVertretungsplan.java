package com.asdoi.gymwen.ui.fragments;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;

public class SettingsFragmentVertretungsplan extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_vertretungsplan, rootKey);

        setFullNames();
        Preference myPref = findPreference("show_full_names");
        myPref.setOnPreferenceClickListener((Preference preference) -> {
            setFullNames();
            return true;
        });
    }

    private void setFullNames() {
        boolean showNotif = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("show_full_names", false) && !PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("hide_gesamt", false);
        findPreference("show_full_names_specific").setEnabled(showNotif);
    }
}
