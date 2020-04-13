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

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.SettingsActivity;

public class SettingsFragmentSubstitution extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_substitutionplan, rootKey);

        ((SettingsActivity) getActivity()).loadedFragments++;

        setFullNames();
        Preference myPref = findPreference("show_full_names");
        myPref.setOnPreferenceClickListener((Preference preference) -> {
            setFullNames();
            return true;
        });

/*        setSummarize();
        myPref = findPreference("summarize");
        myPref.setOnPreferenceClickListener((Preference pref) -> {
            setSummarize();
            return true;
        });*/
    }

    private void setFullNames() {
        boolean showNotif = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("show_full_names", false) && !PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("hide_gesamt", false);
        findPreference("show_full_names_specific").setVisible(showNotif);
    }

    private void setSummarize() {
        boolean showNotif = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("summarize", true) && !PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("hide_gesamt", false);
        findPreference("summarize_old").setVisible(showNotif);
    }
}
