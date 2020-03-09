package com.asdoi.gymwen.ui.settingsFragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.SettingsActivity;
import com.asdoi.gymwen.util.External_Const;

public class SettingsFragmentSignIn extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_signin, rootKey);

        ((SettingsActivity) getActivity()).loadedFragments++;

        Preference myPref = findPreference("today_url");
        myPref.setDefaultValue(External_Const.todayURL);

        myPref = findPreference("tomorrow_url");
        myPref.setDefaultValue(External_Const.tomorrowURL);

        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        setSummary(sp, "username", getString(R.string.set_desc_username));
        setSummary(sp, "password", getString(R.string.set_desc_password));
        setSummary(sp, "today_url", getString(R.string.set_desc_today_url));
        setSummary(sp, "tomorrow_url", getString(R.string.set_desc_tomorrow_url));
        setSummary(sp, "teacherlist_url", getString(R.string.set_desc_teacherlist_url));
    }

    public void setSummary(SharedPreferences sp, String settingskey, String defaulText) {
        EditTextPreference editTextPref = findPreference(settingskey);
        editTextPref.setSummary(sp.getString(settingskey, defaulText));
    }

    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference pref = findPreference(key);
        if (pref instanceof EditTextPreference) {
            EditTextPreference etp = (EditTextPreference) pref;
            pref.setSummary(etp.getText());
        }
    }
}
