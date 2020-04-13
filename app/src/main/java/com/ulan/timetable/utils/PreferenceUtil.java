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

package com.ulan.timetable.utils;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;

import static com.asdoi.gymwen.util.PreferenceUtil.getBooleanSettings;

public class PreferenceUtil {

    public static boolean isTimeTableSubstitution() {
        return ApplicationFeatures.getBooleanSettings("timetable_subs", true);
    }

    public static void setTimeTableSubstitution(@NonNull Context context, boolean value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("timetable_subs", value);
        editor.commit();
    }

    public static boolean isTimeTableNotification() {
        return ApplicationFeatures.getBooleanSettings("timetableNotif", true);
    }

    public static void setTimeTableAlarmTime(@NonNull int... times) {
        if (times.length != 3) {
            if (times.length > 0 && times[0] == 0) {
                setTimeTableAlarm(ApplicationFeatures.getContext(), false);
            } else {
                System.out.println("wrong parameters");
            }
            return;
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        setTimeTableAlarm(ApplicationFeatures.getContext(), true);
        editor.putInt("timetable_Alarm_hour", times[0]);
        editor.putInt("timetable_Alarm_minute", times[1]);
        editor.putInt("timetable_Alarm_second", times[2]);
        editor.commit();
    }

    @NonNull
    public static int[] getTimeTableAlarmTime() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
        return new int[]{sharedPref.getInt("timetable_Alarm_hour", 7), sharedPref.getInt("timetable_Alarm_minute", 55), sharedPref.getInt("timetable_Alarm_second", 0)};
    }

    private static void setTimeTableAlarm(@NonNull Context context, boolean value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("timetable_alarm", value);
        editor.commit();
    }


    public static boolean isTimeTableAlarmOn(@NonNull Context context) {
        return getBooleanSettings("timetable_alarm", true, context);
    }

    public static boolean doNotDisturbDontAskAgain() {
        return ApplicationFeatures.getBooleanSettings("do_not_disturb_dont_ask", false);
    }

    public static void setDoNotDisturbDontAskAgain(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("do_not_disturb_dont_ask", value).apply();
    }

    public static boolean isAutomaticDoNotDisturb() {
        return ApplicationFeatures.getBooleanSettings("automatic_do_not_disturb", true);
    }

    public static void setDoNotDisturb(Activity activity, boolean dontAskAgain) {
        NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if the notification policy access has been granted for the app.
            if (!notificationManager.isNotificationPolicyAccessGranted() && !dontAskAgain) {
                Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.ic_do_not_disturb_on_black_24dp);
                try {
                    Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
                    DrawableCompat.setTint(wrappedDrawable, ApplicationFeatures.getTextColorPrimary(activity));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                new MaterialDialog.Builder(activity)
                        .title(R.string.permission_required)
                        .content(R.string.do_not_disturb_permission_desc)
                        .onPositive((dialog, which) -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            activity.startActivity(intent);
                        })
                        .positiveText(R.string.permission_ok_button)
                        .negativeText(R.string.permission_cancel_button)
                        .onNegative(((dialog, which) -> dialog.dismiss()))
                        .icon(drawable)
                        .onNeutral(((dialog, which) -> setDoNotDisturbDontAskAgain(activity, true)))
                        .neutralText(R.string.dont_show_again)
                        .show();
            } else {
                DoNotDisturbReceiversKt.setDoNotDisturbReceivers(activity);
            }
        }
    }

    public static boolean isDoNotDisturbTurnOff() {
        return ApplicationFeatures.getBooleanSettings("do_not_disturb_turn_off", false);
    }
}
