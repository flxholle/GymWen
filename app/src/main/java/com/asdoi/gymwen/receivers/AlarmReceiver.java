package com.asdoi.gymwen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.asdoi.gymwen.ApplicationFeatures;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
//                ActivityFeatures.alarm();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (intent.getAction() != null && context != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                // Set the alarm here.
//                LocalData localData = new LocalData(context);
//                NotifyScheduler.setReminder(context, AlarmReceiver.class, localData.get_hour(), localData.get_min());
                int[] times = ApplicationFeatures.getAlarmTime();
                ApplicationFeatures.setReminder(context, AlarmReceiver.class, times[0], times[1], times[2]);
                return;
            }
        }

        //Trigger the notification
        System.out.println("trigger notif");
        ApplicationFeatures.sendNotification();
    }
}
