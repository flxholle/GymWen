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

import android.os.Build;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.SettingsActivity;
import com.asdoi.gymwen.util.PreferenceUtil;
import com.github.javiersantos.appupdater.enums.Display;

public class SettingsFragmentRoot extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_root, rootKey);

        ((SettingsActivity) getActivity()).loadedFragments++;

        Preference myPref = findPreference("language");
        myPref.setOnPreferenceClickListener((Preference p) -> {
            ApplicationFeatures.getLanguageSwitcher().showChangeLanguageDialog(getActivity());
            return true;
        });
        myPref.setSummary(ApplicationFeatures.getLanguageSwitcher().getCurrentLocale().toString());

        myPref = findPreference("updates");
        myPref.setOnPreferenceClickListener((Preference p) -> {
            ((ActivityFeatures) getActivity()).checkUpdates(Display.DIALOG, true);
            return true;
        });

        myPref = findPreference("shortcuts_array");
        if (Build.VERSION.SDK_INT >= 25) {
            myPref.setVisible(true);
            String[] defaultValues = PreferenceUtil.isParents() ? getContext().getResources().getStringArray(R.array.shortcuts_array_values_default_parent_mode) : getContext().getResources().getStringArray(R.array.shortcuts_array_values_default);
            myPref.setDefaultValue(defaultValues);
        } else
            myPref.setVisible(false);
    }
}