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

        setBorder();
        Preference myPref = findPreference("show_borders");
        myPref.setOnPreferenceClickListener((Preference preference) -> {
            setBorder();
            return true;
        });
    }

    private void setBorder() {
        boolean showNotif = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("show_borders", false) && !PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("hide_gesamt", false);
        findPreference("show_border_specific").setEnabled(showNotif);
    }
}
