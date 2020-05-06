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

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.SettingsActivity;

import java.util.Objects;

public class SettingsFragmentDesign extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_design, rootKey);

        ((SettingsActivity) requireActivity()).loadedFragments++;

        ListPreference mp = findPreference("theme");
        Objects.requireNonNull(mp).setOnPreferenceChangeListener((preference, newValue) -> {
            mp.setValue(newValue + "");
            requireActivity().recreate();
            return false;
        });
        mp.setSummary(getThemeName());

        Preference myPref;
        myPref = findPreference("primaryColor");
        Objects.requireNonNull(myPref).setVisible(true);
        myPref.setOnPreferenceClickListener((Preference p) -> {
            new ColorChooserDialog.Builder(requireContext(), R.string.color_primary)
                    .accentMode(false)
                    .allowUserColorInput(true)
                    .allowUserColorInputAlpha(false)
                    .preselect(ApplicationFeatures.getPrimaryColor(requireContext()))
                    .show(requireActivity());
            return true;
        });

        myPref = findPreference("accentColor");
        Objects.requireNonNull(myPref).setVisible(true);
        myPref.setOnPreferenceClickListener((Preference p) -> {
            new ColorChooserDialog.Builder(requireContext(), R.string.color_accent)
                    .accentMode(true)
                    .allowUserColorInput(true)
                    .allowUserColorInputAlpha(false)
                    .preselect(ApplicationFeatures.getAccentColor(requireContext()))
                    .show(requireActivity());
            return true;
        });

        setBorder();
        myPref = findPreference("show_borders");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference preference) -> {
            setBorder();
            return true;
        });

        myPref = findPreference("old_vertretung");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference pref) -> {
            setOldTitle();
            return true;
        });

        setSwipeSpecific();
        myPref = findPreference("swipe_to_refresh");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference pref) -> {
            setSwipeSpecific();
            return true;
        });
    }

    private void setBorder() {
        boolean showBorder = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("show_borders", false) && !PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("hide_gesamt", false);
        findPreference("show_border_specific").setVisible(showBorder);
    }

    private String getThemeName() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        String selectedTheme = sharedPreferences.getString("theme", "switch");
        String[] values = getResources().getStringArray(R.array.theme_array_values);

        String[] names = getResources().getStringArray(R.array.theme_array);

        for (int i = 0; i < values.length; i++) {
            if (values[i].equalsIgnoreCase(selectedTheme)) {
                return names[i];
            }
        }

        return "";
    }

    private void setOldTitle() {
        boolean value = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("old_vertretung", false);
        ((SwitchPreference) Objects.requireNonNull(findPreference("old_vertretung_title"))).setChecked(value);
    }

    private void setSwipeSpecific() {
        boolean showSpecific = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("swipe_to_refresh", true);
        findPreference("swipe_to_refresh_filtered").setVisible(showSpecific);
    }
}
