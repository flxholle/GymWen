package com.asdoi.gymwen.ui.settingsFragments;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.SettingsActivity;
import com.asdoi.gymwen.util.PreferenceUtil;

import java.util.Objects;

public class SettingsFragmentNotification extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_notification, rootKey);

        ((SettingsActivity) Objects.requireNonNull(getActivity())).loadedFragments++;

        setNotif();

        Preference myPref = findPreference("showNotification");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference preference) -> {
            setNotif();
            return true;
        });

        myPref = findPreference("alarm");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference p) -> {
            PreferenceUtil.setAlarmTime(0);
            ((ActivityFeatures) getActivity()).createTimePicker();
            return true;
        });
    }

    private void setNotif() {
        boolean showNotif = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("showNotification", false);
        Objects.requireNonNull(findPreference("alwaysNotification")).setEnabled(showNotif);
        Objects.requireNonNull(findPreference("alarm")).setEnabled(showNotif);
        Objects.requireNonNull(findPreference("two_notifs")).setEnabled(showNotif);
    }
}
