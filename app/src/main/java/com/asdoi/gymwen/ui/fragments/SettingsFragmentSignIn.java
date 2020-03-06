package com.asdoi.gymwen.ui.fragments;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.util.External_Const;

public class SettingsFragmentSignIn extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_signin, rootKey);

        Preference myPref = findPreference("today_url");
        myPref.setDefaultValue(External_Const.todayURL);

        myPref = findPreference("tomorrow_url");
        myPref.setDefaultValue(External_Const.tomorrowURL);
    }
}
