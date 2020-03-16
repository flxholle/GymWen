package com.asdoi.gymwen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.util.PreferenceUtil;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(@Nullable Context context, @NonNull Intent intent) {
        if (intent.getAction() != null && context != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
                // Set the alarm here.
                int[] times = PreferenceUtil.getAlarmTime();
                ApplicationFeatures.setAlarm(context, AlarmReceiver.class, times[0], times[1], times[2]);
                return;
            }
        }

        //Trigger the notification
        ApplicationFeatures.sendNotifications(true);
    }
}
