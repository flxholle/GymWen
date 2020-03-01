package com.asdoi.gymwen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.util.PreferenceUtil;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("triggered alarm");

        if (intent.getAction() != null && context != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                // Set the alarm here.
                int[] times = PreferenceUtil.getAlarmTime();
                ApplicationFeatures.setAlarm(context, AlarmReceiver.class, times[0], times[1], times[2]);
                return;
            }
        }

        //Trigger the notification
        ApplicationFeatures.sendNotification();
    }
}
