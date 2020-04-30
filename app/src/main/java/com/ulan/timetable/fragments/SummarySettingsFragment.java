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

package com.ulan.timetable.fragments;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.asdoi.gymwen.R;
import com.ulan.timetable.utils.PreferenceUtil;


public class SummarySettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.timetable_settings_summary, rootKey);

        Preference myPref = findPreference("start_time");
        Preference finalMyPref = myPref;
        myPref.setOnPreferenceClickListener((Preference p) -> {
            int[] oldTimes = PreferenceUtil.getStartTime(getContext());
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                    (view, hourOfDay, minute) -> {
                        PreferenceUtil.setStartTime(getContext(), hourOfDay, minute, 0);
                        finalMyPref.setSummary(hourOfDay + ":" + minute);
                    }, oldTimes[0], oldTimes[1], true);
            timePickerDialog.setTitle(R.string.start_of_school);
            timePickerDialog.show();
            return true;
        });
        int[] oldTimes = PreferenceUtil.getStartTime(getContext());
        myPref.setSummary(oldTimes[0] + ":" + oldTimes[1]);


        myPref = findPreference("set_period_length");
        Preference finalMyPref1 = myPref;
        myPref.setOnPreferenceClickListener((Preference p) -> {
            NumberPicker numberPicker = new NumberPicker(getContext());
            numberPicker.setMaxValue(180);
            numberPicker.setMinValue(1);
            numberPicker.setValue(PreferenceUtil.getPeriodLength(getContext()));
            numberPicker.setOnValueChangedListener((NumberPicker np, int i, int i1) -> {
                int value = np.getValue();
                PreferenceUtil.setPeriodLength(getContext(), value);
                finalMyPref1.setSummary(value + " " + getString(R.string.minutes));
            });
            new MaterialDialog.Builder(getContext())
                    .customView(numberPicker, false)
                    .show();
            return true;
        });
        myPref.setSummary(PreferenceUtil.getPeriodLength(getContext()) + " " + getString(R.string.minutes));
    }
}
