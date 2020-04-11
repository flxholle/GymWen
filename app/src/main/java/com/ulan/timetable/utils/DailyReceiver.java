package com.ulan.timetable.utils;

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

    Context context;

    @Override
    public void onReceive(@NonNull Context context, Intent intent) {
        this.context = context;

        if (intent.getAction() != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
                // Set the alarm here.
                int[] times = PreferenceUtil.getTimeTableAlarmTime();
                ApplicationFeatures.setAlarm(context, DailyReceiver.class, times[0], times[1], times[2], DailyReceiverID);
            }
        }

        if (!PreferenceUtil.isTimeTableAlarmOn(context)) {
            ApplicationFeatures.cancelAlarm(context, DailyReceiver.class, DailyReceiverID);
        }

        NotificationUtil.sendNotificationSummary(context, true);
    }

}