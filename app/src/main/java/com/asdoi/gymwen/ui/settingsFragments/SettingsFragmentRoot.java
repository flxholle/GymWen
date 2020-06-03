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

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.SettingsActivity;
import com.asdoi.gymwen.util.PreferenceUtil;
import com.bytehamster.lib.preferencesearch.SearchConfiguration;
import com.bytehamster.lib.preferencesearch.SearchPreference;
import com.github.javiersantos.appupdater.enums.Display;

import java.util.Objects;

public class SettingsFragmentRoot extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_root, rootKey);

        ((SettingsActivity) requireActivity()).loadedFragments++;

        SearchPreference searchPreference = (SearchPreference) findPreference("searchPreference");
        SearchConfiguration config = searchPreference.getSearchConfiguration();
        config.setActivity((AppCompatActivity) getActivity());
        config.index(R.xml.preferences_root);
        config.index(R.xml.preferences_design);
        config.index(R.xml.preferences_hide_menu_items);
        config.index(R.xml.preferences_notification);
        config.index(R.xml.preferences_signin);
        config.index(R.xml.preferences_substitutionplan);
        config.index(R.xml.timetable_settings);

        Preference myPref = findPreference("language");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference p) -> {
            ApplicationFeatures.getLanguageSwitcher().showChangeLanguageDialog(requireActivity());
            return true;
        });
        myPref.setSummary(ApplicationFeatures.getLanguageSwitcher().getCurrentLocale().toString());

        myPref = findPreference("updates");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference p) -> {
            ((ActivityFeatures) requireActivity()).checkUpdates(Display.DIALOG, true);
            return true;
        });

        myPref = findPreference("shortcuts_array");
        if (Build.VERSION.SDK_INT >= 25) {
            Objects.requireNonNull(myPref).setVisible(true);
            String[] defaultValues = PreferenceUtil.isParents() ? requireContext().getResources().getStringArray(R.array.shortcuts_array_values_default_parent_mode) : requireContext().getResources().getStringArray(R.array.shortcuts_array_values_default);
            myPref.setDefaultValue(defaultValues);
        } else
            Objects.requireNonNull(myPref).setVisible(false);

        myPref = findPreference("open_timetable_settings");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference p) -> {
            requireActivity().startActivity(new Intent(requireContext(), com.ulan.timetable.activities.SettingsActivity.class));
            return true;
        });

        myPref = findPreference("show_more");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference p) -> {
            findPreference("show_more").setVisible(false);
            findPreference("language").setVisible(true);
            findPreference("updates").setVisible(true);
            findPreference("auto_update").setVisible(true);
            findPreference("offline_mode").setVisible(true);
            return true;
        });
    }
}