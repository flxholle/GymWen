package com.asdoi.gymwen.ui.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.asdoi.gymwen.R;

public class SettingsFragmentSignIn extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_signin, rootKey);
    }
}
