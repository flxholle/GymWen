/*
 * Copyright (c) 2020 Felix Hollederer
 *     This file is part of GymWenApp.
 *
 *     GymWenApp is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GymWenApp is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GymWenApp.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.asdoi.gymwen.ui.settingsFragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

    private void setSummary(@NonNull SharedPreferences sp, @NonNull String settingskey, String defaulText) {
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
                                          @NonNull String key) {
        Preference pref = findPreference(key);
        if (pref instanceof EditTextPreference) {
            EditTextPreference etp = (EditTextPreference) pref;
            pref.setSummary(etp.getText());
        }
    }
}
