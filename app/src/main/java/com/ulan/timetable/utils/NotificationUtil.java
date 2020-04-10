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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver;
import com.asdoi.gymwen.substitutionplan.SubstitutionList;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlan;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures;
import com.asdoi.gymwen.ui.activities.SubstitutionTimeTableActivity;
import com.ulan.timetable.model.Week;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class NotificationUtil {
    private static final int NOTIFICATION_ID = 9090;
    private static final String CHANNEL_ID = "timetable_notification";

    public static void sendNotification(@NonNull Context context, boolean alert) {
        new Thread(() -> {
            ProfileManagement.initProfiles();
            ApplicationFeatures.downloadSubstitutionplanDocs(false, true);
            SubstitutionPlan substitutionPlan = SubstitutionPlanFeatures.createTempSubstitutionplan(false, ProfileManagement.getProfile(ProfileManagement.loadPreferredProfilePosition()).getCoursesArray());

            SubstitutionList substitutionlist;
            if (!substitutionPlan.getToday().getNoInternet()) {
                if (substitutionPlan.getTodayTitle().isTitleCodeToday())
                    substitutionlist = substitutionPlan.getToday();
                else if (substitutionPlan.getTomorrowTitle().isTitleCodeToday())
                    substitutionlist = substitutionPlan.getTomorrow();
                else
                    substitutionlist = new SubstitutionList(true);
            } else
                substitutionlist = new SubstitutionList(true);

            DbHelper db = new DbHelper(context);
            ArrayList<Week> weeks = WeekUtils.compareSubstitutionAndWeeks(context, db.getWeek(getCurrentDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))), substitutionlist, ProfileManagement.getProfile(ProfileManagement.loadPreferredProfilePosition()).isSenior());

            sendNotification(context, alert, weeks, context.getString(R.string.notification_title));
        }).start();
    }

    public static void sendNotification(@NonNull Context context, boolean alert, ArrayList<Week> weeks, String title) {
        String message = getLessons(weeks, context);
        if (message == null || !PreferenceUtil.isTimeTableNotification())
            return;


        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, SubstitutionTimeTableActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        createNotificationChannel(context);
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_assignment_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.gymlogo))
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setWhen(when)
                .setPriority(alert ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setOnlyAlertOnce(!alert)
                .setContentIntent(pendingIntent);


        if (com.asdoi.gymwen.util.PreferenceUtil.isAlwaysNotification()) {
            //Dismiss button intent
            Intent buttonIntent = new Intent(context, NotificationDismissButtonReceiver.class);
            buttonIntent.setAction("com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver");
            buttonIntent.putExtra(NotificationDismissButtonReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID);
            PendingIntent btPendingIntent = PendingIntent.getBroadcast(context, UUID.randomUUID().hashCode(), buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mNotifyBuilder.setOngoing(true);
            mNotifyBuilder.addAction(R.drawable.ic_close_black_24dp, context.getString(R.string.notif_dismiss), btPendingIntent);
        }

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
        }
    }

    private static void createNotificationChannel(@NonNull Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.timetable_channel), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.timetable_channel_desc));
            channel.enableLights(false);
            channel.setSound(null, null);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    private static String getLessons(ArrayList<Week> weeks, Context context) {
        StringBuilder lessons = new StringBuilder("");
        for (Week week : weeks) {
            if (week != null) {
                lessons.append(week.getSubject())
                        .append(" ")
                        .append(context.getString(R.string.time_from))
                        .append(" ")
                        .append(week.getFromTime())
                        .append(" - ")
                        .append(week.getToTime())
                        .append(" ")
                        .append(context.getString(R.string.share_msg_in_room))
                        .append(" ")
                        .append(week.getRoom())
                        .append("\n");
            }
        }

        return !lessons.toString().equals("") ? lessons.toString() : null;
    }

    @Nullable
    public static String getCurrentDay(int day) {
        String currentDay = null;
        switch (day) {
            case 1:
                currentDay = "Sunday";
                break;
            case 2:
                currentDay = "Monday";
                break;
            case 3:
                currentDay = "Tuesday";
                break;
            case 4:
                currentDay = "Wednesday";
                break;
            case 5:
                currentDay = "Thursday";
                break;
            case 6:
                currentDay = "Friday";
                break;
            case 7:
                currentDay = "Saturday";
                break;
        }
        return currentDay;
    }
}
