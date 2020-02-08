package com.asdoi.gymwen.ui.fragments;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;

public class SettingsFragmentNotification extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_notification, rootKey);
        setNotif();

        Preference myPref = findPreference("showNotification");
        myPref.setOnPreferenceClickListener((Preference preference) -> {
            setNotif();
            return true;
        });

        myPref = findPreference("alarm");
        myPref.setOnPreferenceClickListener((Preference p) -> {
            ApplicationFeatures.setAlarmTime(0);
            ((ActivityFeatures) getActivity()).createTimePicker((ActivityFeatures) getActivity());
            return true;
        });
    }

    private void setNotif() {
        boolean showNotif = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("showNotification", false);
        findPreference("alwaysNotification").setEnabled(showNotif);
        findPreference("alarm").setEnabled(showNotif);
        findPreference("two_notifs").setEnabled(showNotif);
    }
}
