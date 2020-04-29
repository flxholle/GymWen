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

import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.ulan.timetable.receivers.DailyReceiver;
import com.ulan.timetable.utils.PreferenceUtil;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.timetable_settings, rootKey);

        tintIcons(getPreferenceScreen(), ApplicationFeatures.getTextColorPrimary(getContext()));

        Preference allPrefs = findPreference("allprefs");
        allPrefs.setOnPreferenceClickListener((Preference p) -> {
            startActivity(new Intent(getActivity(), com.asdoi.gymwen.ui.activities.SettingsActivity.class));
            getActivity().finish();
            return true;
        });

        setNotif();

        Preference myPref = findPreference("timetableNotif");
        myPref.setOnPreferenceClickListener((Preference preference) -> {
            setNotif();
            return true;
        });

        myPref = findPreference("timetable_alarm");
        myPref.setOnPreferenceClickListener((Preference p) -> {
            int[] oldTimes = PreferenceUtil.getTimeTableAlarmTime();
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                    (view, hourOfDay, minute) -> {
                        PreferenceUtil.setTimeTableAlarmTime(hourOfDay, minute, 0);
                        ApplicationFeatures.setRepeatingAlarm(getContext(), DailyReceiver.class, hourOfDay, minute, 0, DailyReceiver.DailyReceiverID, AlarmManager.INTERVAL_DAY);
                    }, oldTimes[0], oldTimes[1], true);
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
            return true;
        });
        int[] oldTimes = PreferenceUtil.getTimeTableAlarmTime();
        myPref.setSummary(oldTimes[0] + ":" + oldTimes[1]);

        setTurnOff();
        myPref = findPreference("automatic_do_not_disturb");
        myPref.setOnPreferenceClickListener((Preference p) -> {
            PreferenceUtil.setDoNotDisturb(getActivity(), false);
            setTurnOff();
            return true;
        });


        setCourses();
        myPref = findPreference("timetable_subs");
        myPref.setOnPreferenceClickListener((Preference p) -> {
            setCourses();
            return true;
        });
    }

    private void setNotif() {
        boolean show = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("timetableNotif", true);
        findPreference("alwaysNotification").setVisible(show);
        findPreference("timetable_alarm").setVisible(show);
    }

    private void setTurnOff() {
        boolean show = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("automatic_do_not_disturb", true);
        findPreference("do_not_disturb_turn_off").setVisible(show);
    }

    private void setCourses() {
        boolean show = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("timetable_subs", true);
        findPreference("courses").setVisible(show);
    }

    private static void tintIcons(Preference preference, int color) {
        if (preference instanceof PreferenceGroup) {
            PreferenceGroup group = ((PreferenceGroup) preference);
            for (int i = 0; i < group.getPreferenceCount(); i++) {
                tintIcons(group.getPreference(i), color);
            }
        } else {
            Drawable icon = preference.getIcon();
            if (icon != null) {
                DrawableCompat.setTint(icon, color);
            }
        }
    }
}
