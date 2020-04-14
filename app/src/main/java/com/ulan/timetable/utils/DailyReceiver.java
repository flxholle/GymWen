package com.ulan.timetable.utils;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.asdoi.gymwen.ApplicationFeatures;

/**
 * Created by Ulan on 28.01.2019.
 */
public class DailyReceiver extends BroadcastReceiver {

    public static final int DailyReceiverID = 10000;

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
                // Set the alarm here.
                int[] times = PreferenceUtil.getTimeTableAlarmTime();
                ApplicationFeatures.setRepeatingAlarm(context, DailyReceiver.class, times[0], times[1], times[2], DailyReceiverID, AlarmManager.INTERVAL_DAY);
            }
        }

        if (!PreferenceUtil.isTimeTableAlarmOn(context)) {
            ApplicationFeatures.cancelAlarm(context, DailyReceiver.class, DailyReceiverID);
        }

        NotificationUtil.sendNotificationSummary(context, true);
    }

}