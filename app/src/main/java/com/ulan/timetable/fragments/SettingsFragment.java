package com.ulan.timetable.fragments;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.receivers.AlarmReceiver;
import com.asdoi.gymwen.ui.activities.SettingsActivity;
import com.ulan.timetable.utils.DailyReceiver;
import com.ulan.timetable.utils.PreferenceUtil;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.timetable_settings, rootKey);

        Preference allPrefs = findPreference("allprefs");
        allPrefs.setOnPreferenceClickListener((Preference p) -> {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
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
                        ApplicationFeatures.setAlarm(getContext(), DailyReceiver.class, hourOfDay, minute, 0, AlarmReceiver.AlarmReceiverID);
                    }, oldTimes[0], oldTimes[1], true);
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
            return true;
        });
    }

    private void setNotif() {
        boolean showNotif = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("timetableNotif", true);
        findPreference("alwaysNotification").setVisible(showNotif);
        findPreference("timetable_alarm").setVisible(showNotif);
    }
}
