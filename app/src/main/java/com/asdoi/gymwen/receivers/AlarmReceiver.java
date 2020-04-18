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

package com.asdoi.gymwen.receivers;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.util.PreferenceUtil;

public class AlarmReceiver extends BroadcastReceiver {
    public static final int AlarmReceiverID = 500;

    @Override
    public void onReceive(@Nullable Context context, @NonNull Intent intent) {
        if (intent.getAction() != null && context != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                // Set the alarm here.
                if (PreferenceUtil.isAlarmOn(context)) {
                    int[] times = PreferenceUtil.getAlarmTime();
                    ApplicationFeatures.setRepeatingAlarm(context, AlarmReceiver.class, times[0], times[1], times[2], AlarmReceiverID, AlarmManager.INTERVAL_DAY);
                } else
                    ApplicationFeatures.cancelAlarm(context, AlarmReceiver.class, AlarmReceiver.AlarmReceiverID);
                ApplicationFeatures.sendNotifications(true);
                return;
            }
        }

        if (!PreferenceUtil.isAlarmOn(context)) {
            ApplicationFeatures.cancelAlarm(context, AlarmReceiver.class, AlarmReceiver.AlarmReceiverID);
        } else {
            //Trigger the notification
            ApplicationFeatures.sendNotifications(true);
        }
    }
}
