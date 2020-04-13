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

package com.ulan.timetable.utils

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.asdoi.gymwen.ApplicationFeatures
import com.asdoi.gymwen.profiles.ProfileManagement
import com.asdoi.gymwen.substitutionplan.SubstitutionList
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures
import java.util.*

class TurnOnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null) {
            if (intent.action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED, ignoreCase = true)) {
                // Set the alarm here.
                setDoNotDisturbReceivers(context)
                return
            }
        }

        setDoNotDisturbReceivers(context)
        setDoNotDisturb(context, true)
    }

    companion object {
        const val TurnOn_ID = 30000
    }
}

class TurnOffReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        setDoNotDisturbReceivers(context)
        if (PreferenceUtil.isDoNotDisturbTurnOff())
            setDoNotDisturb(context, false)
    }

    companion object {
        const val TurnOff_ID = 60000
    }
}


fun setDoNotDisturb(context: Context, on: Boolean) {
    NotificationUtil.sendNotificationCurrentLesson(context, true)
    if (!PreferenceUtil.isAutomaticDoNotDisturb())
        return

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Check if the notification policy access has been granted for the app.
        if (notificationManager.isNotificationPolicyAccessGranted) {
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.setInterruptionFilter(if (on) NotificationManager.INTERRUPTION_FILTER_PRIORITY else NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }
}

fun setDoNotDisturbReceivers(context: Context) {
    Thread(Runnable {
        val dbHelper = DbHelper(context)

        ProfileManagement.initProfiles()
        ApplicationFeatures.downloadSubstitutionplanDocs(false, true)
        val substitutionPlan = SubstitutionPlanFeatures.createTempSubstitutionplan(false, ProfileManagement.getProfile(ProfileManagement.loadPreferredProfilePosition()).coursesArray)

        val calendar = Calendar.getInstance()
        val currentDay = NotificationUtil.getCurrentDay(calendar.get(Calendar.DAY_OF_WEEK))
        val substitutionList: SubstitutionList =
                if (!substitutionPlan.today.getNoInternet()) {
                    when {
                        substitutionPlan.todayTitle.isTitleCodeToday() -> substitutionPlan.today
                        substitutionPlan.tomorrowTitle.isTitleCodeToday() -> substitutionPlan.tomorrow
                        else -> SubstitutionList(true)
                    }
                } else
                    SubstitutionList(true)

        val weeks = WeekUtils.compareSubstitutionAndWeeks(context, dbHelper.getWeek(currentDay), substitutionList, ProfileManagement.getProfile(ProfileManagement.loadPreferredProfilePosition()).isSenior)

        var lastCalendar = Calendar.getInstance()
        lastCalendar.set(Calendar.HOUR_OF_DAY, 23)
        lastCalendar.set(Calendar.MINUTE, 59)
        var on: Boolean? = null

        for (week in weeks) {
            val weekCalendarStart = Calendar.getInstance()
            val startHour = Integer.parseInt(week.fromTime.substring(0, week.fromTime.indexOf(":")))
            weekCalendarStart.set(Calendar.HOUR_OF_DAY, startHour)
            val startMinute = Integer.parseInt(week.fromTime.substring(week.fromTime.indexOf(":") + 1))
            weekCalendarStart.set(Calendar.MINUTE, startMinute)

            if (((startHour == calendar.get(Calendar.HOUR_OF_DAY) && startMinute >= calendar.get(Calendar.MINUTE)) || startHour > calendar.get(Calendar.HOUR_OF_DAY)) && ((startHour == lastCalendar.get(Calendar.HOUR_OF_DAY) && startMinute <= lastCalendar.get(Calendar.MINUTE)) || startHour < lastCalendar.get(Calendar.HOUR_OF_DAY))) {
                lastCalendar = weekCalendarStart
                on = true
            }

            val weekCalendarEnd = Calendar.getInstance()
            val endHour = Integer.parseInt(week.toTime.substring(0, week.toTime.indexOf(":")))
            weekCalendarEnd.set(Calendar.HOUR_OF_DAY, endHour)
            val endMinute = Integer.parseInt(week.toTime.substring(week.toTime.indexOf(":") + 1))
            weekCalendarEnd.set(Calendar.MINUTE, endMinute)

            if (((endHour == calendar.get(Calendar.HOUR_OF_DAY) && endMinute > calendar.get(Calendar.MINUTE)) || endHour > calendar.get(Calendar.HOUR_OF_DAY)) && ((endHour == lastCalendar.get(Calendar.HOUR_OF_DAY) && endMinute < lastCalendar.get(Calendar.MINUTE)) || endHour < lastCalendar.get(Calendar.HOUR_OF_DAY))) {
                lastCalendar = weekCalendarEnd
                on = false
            }
        }

        if (on != null) {
            if (on) {
                ApplicationFeatures.setOneTimeAlarm(context, TurnOnReceiver::class.java, lastCalendar.get(Calendar.HOUR_OF_DAY), lastCalendar.get(Calendar.MINUTE), 0, TurnOnReceiver.TurnOn_ID)
            } else {
                ApplicationFeatures.setOneTimeAlarm(context, TurnOffReceiver::class.java, lastCalendar.get(Calendar.HOUR_OF_DAY), lastCalendar.get(Calendar.MINUTE), 0, TurnOffReceiver.TurnOff_ID)
            }
        }
    }).start()
}