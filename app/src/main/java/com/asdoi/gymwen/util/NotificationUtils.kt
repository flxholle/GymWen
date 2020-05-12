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

package com.asdoi.gymwen.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import com.asdoi.gymwen.ApplicationFeatures
import com.asdoi.gymwen.R
import com.asdoi.gymwen.profiles.Profile
import com.asdoi.gymwen.profiles.ProfileManagement
import com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver
import com.asdoi.gymwen.substitutionplan.SubstitutionEntry
import com.asdoi.gymwen.substitutionplan.SubstitutionList
import com.asdoi.gymwen.substitutionplan.SubstitutionPlan
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures
import com.asdoi.gymwen.ui.activities.MainActivity
import com.github.stephenvinouze.shapetextdrawable.ShapeForm
import com.github.stephenvinouze.shapetextdrawable.ShapeTextDrawable
import java.util.*

const val NOTIFICATION_MAIN_CHANNEL_ID = "substitutionchannel_02"
const val NOTIFICATION_SUMMARY_CHANNEL_ID = "substitutionchannel_01"
const val NOTIFICATION_MAIN_ID = -30
const val NOTIFICATION_SUMMARY_ID_1 = -40
const val NOTIFICATION_SUMMARY_ID_2 = -50

class NotificationUtils {

    companion object {
        const val today = 1
        const val tomorrow = 2
        const val both = -1

        class CreateNotification(private val alert: Boolean) : ApplicationFeatures.DownloadSubstitutionplanDocsTask() {
            private val summarize = PreferenceUtil.isSummarizeUp()
            private val alertForAllProfiles = PreferenceUtil.isMainNotifForAllProfiles() && ProfileManagement.isMoreThanOneProfile()
            private val unchangedSummary = PreferenceUtil.isDontChangeSummary()

            override fun onPostExecute(v: Void?) {
                super.onPostExecute(v)
                try {
                    ProfileManagement.initProfiles()
                    if (!ApplicationFeatures.initSettings(false, false))
                        return
                    if (SubstitutionPlanFeatures.getTodayTitleString() == ApplicationFeatures.getContext().getString(R.string.noInternetConnection)) {
                        return
                    }
                    createNotification()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            private fun createNotification() {
                ProfileManagement.initProfiles()

                val titleTodayArray = SubstitutionPlanFeatures.getTodayTitle()
                val titleTomorrowArray = SubstitutionPlanFeatures.getTomorrowTitle()
                var titleToday = "${titleTodayArray.dayOfWeek}, ${titleTodayArray.date}:"
                var titleTomorrow = "${titleTomorrowArray.dayOfWeek}, ${titleTomorrowArray.date}:"

                val isMoreThanOneProfile = ProfileManagement.isMoreThanOneProfile()

                //Send main notif for preferred Profile
                var daySendInSummaryNotif = both //1 = today; 2 = tomorrow

                val preferredProfile = ProfileManagement.getPreferredProfile()
                val preferredProfilePos = if (alertForAllProfiles) -5 else ProfileManagement.getPreferredProfilePosition()

                if (preferredProfile != null || alertForAllProfiles) {
                    var whichDayIsToday = both
                    if (titleTodayArray.isTitleCodeToday())
                        whichDayIsToday = today
                    else if (titleTomorrowArray.isTitleCodeToday())
                        whichDayIsToday = tomorrow

                    var checkProfileList = mutableListOf<Profile>()
                    if (!alertForAllProfiles && preferredProfile != null)
                        checkProfileList.add(preferredProfile)
                    else
                        checkProfileList = ProfileManagement.getProfileList()

                    for (p in checkProfileList.indices) {
                        val temp = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), checkProfileList[p].coursesArray)
                        daySendInSummaryNotif = when (whichDayIsToday) {
                            today -> {
                                MainNotification(SubstitutionPlanFeatures.getTodayTitleString(), if (summarize) temp.todaySummarized else temp.getDay(true), temp.senior, if (isMoreThanOneProfile && alertForAllProfiles) checkProfileList[p].name; else "", alert, p)
                                tomorrow
                            }
                            tomorrow -> {
                                MainNotification(SubstitutionPlanFeatures.getTomorrowTitleString(), if (summarize) temp.tomorrowSummarized else temp.getDay(false), temp.senior, if (isMoreThanOneProfile && alertForAllProfiles) checkProfileList[p].name; else "", alert, p)
                                today
                            }
                            else -> both
                        }
                    }
                }

                //Both
                val countTotal = StringBuilder()

                //Today
                val countToday = StringBuilder()
                var messageToday = StringBuilder()
                var isNoToday = true

                //Tomorrow
                val countTomorrow = StringBuilder()
                var messageTomorrow = StringBuilder()
                var isNoTomorrow = true


                for (i in ProfileManagement.getProfileList().indices) {
                    val temp = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), ProfileManagement.getProfileList().get(i).coursesArray)

                    if (i == preferredProfilePos && !unchangedSummary) {
                        if (daySendInSummaryNotif == today) {
                            //Today
                            var content = temp.getDay(true)
                            try {
                                countToday.append(content.entries.size)
                                countToday.append(", ")
                                countTotal.append(content.entries.size)
                                countTotal.append(", ")
                                content = if (summarize) temp.todaySummarized else content
                                if (content.size() != 0) {
                                    if (isMoreThanOneProfile) {
                                        messageToday.append(ProfileManagement.getProfile(i).name)
                                        messageToday.append(":\n")
                                    }
                                    messageToday.append(notifMessageContent(content, temp))
                                    isNoToday = false
                                }
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }
                        if (daySendInSummaryNotif == tomorrow) {
                            //Tomorrow
                            var content = temp.getDay(false)
                            try {
                                countTomorrow.append(content.entries.size)
                                countTomorrow.append(", ")
                                countTotal.append(content.entries.size)
                                countTotal.append(", ")
                                content = if (summarize) temp.tomorrowSummarized else content
                                if (content.size() != 0) {
                                    if (isMoreThanOneProfile) {
                                        messageTomorrow.append(ProfileManagement.getProfile(i).name)
                                        messageTomorrow.append(":\n")
                                    }
                                    messageTomorrow.append(notifMessageContent(content, temp))
                                    isNoTomorrow = false
                                }
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }
                        if (daySendInSummaryNotif != both) {
                            continue
                        }
                    }


                    //Today
                    var content = temp.getDay(true)
                    try {
                        countToday.append(content.entries.size)
                        countToday.append(", ")
                        countTotal.append(content.entries.size)
                        countTotal.append("|")
                        content = if (summarize) temp.todaySummarized else content
                        if (content.size() != 0) {
                            if (isMoreThanOneProfile) {
                                messageToday.append(ProfileManagement.getProfile(i).name)
                                messageToday.append(":\n")
                            }
                            messageToday.append(notifMessageContent(content, temp))
                            isNoToday = false
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }

                    //Tomorrow
                    content = temp.getDay(false)
                    try {
                        countTomorrow.append(content.entries.size)
                        countTomorrow.append(", ")
                        countTotal.append(content.entries.size)
                        countTotal.append(", ")
                        content = if (summarize) temp.tomorrowSummarized else content
                        if (content.size() != 0) {
                            if (isMoreThanOneProfile) {
                                messageTomorrow.append(ProfileManagement.getProfile(i).name)
                                messageTomorrow.append(":\n")
                            }
                            messageTomorrow.append(notifMessageContent(content, temp))
                            isNoTomorrow = false
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }



                try {
                    countToday.deleteCharAt(countToday.lastIndexOf(", "))
                    countTomorrow.deleteCharAt(countTomorrow.lastIndexOf(", "))
                } catch (e: java.lang.Exception) {
                }
                try {
                    countTotal.deleteCharAt(countTotal.lastIndexOf(", "))
                } catch (e: java.lang.Exception) {
                }

                if (isNoToday) messageToday = StringBuilder("${ApplicationFeatures.getContext().getString(R.string.notif_nothing)}\n")
                if (isNoTomorrow) messageTomorrow = StringBuilder("${ApplicationFeatures.getContext().getString(R.string.notif_nothing)}\n")


                val twoNotifs = PreferenceUtil.isTwoNotifications()

                //Hide days in the past and today after 18 o'clock
                var sendToday = (!PreferenceUtil.isIntelligentHide() || !titleTodayArray.isTitleCodeInPast())
                var sendTomorrow = (!PreferenceUtil.isIntelligentHide() || !titleTomorrowArray.isTitleCodeInPast())

                if (!unchangedSummary && (alertForAllProfiles || !isMoreThanOneProfile)) {
                    sendToday = sendToday && (daySendInSummaryNotif == both || daySendInSummaryNotif == today)
                    sendTomorrow = sendTomorrow && (daySendInSummaryNotif == both || daySendInSummaryNotif == tomorrow)
                }


                //Send Notifs
                if (twoNotifs) {
                    titleToday = "$titleToday $countToday"
                    titleTomorrow = "$titleTomorrow $countTomorrow"
                    if (sendToday) SummaryNotification(titleToday, messageToday.split("\n").toTypedArray())
                    if (sendTomorrow) SummaryNotification(titleTomorrow, messageTomorrow.split("\n").toTypedArray(), NOTIFICATION_SUMMARY_ID_2)
                } else {
                    //Sort notification
                    var title = ""
                    var content = ""

                    if (sendToday && sendTomorrow) {
                        title = "${ApplicationFeatures.getContext().getString(R.string.notif_content_title)} $countTotal"
                        content = titleToday + "\n" + messageToday + titleTomorrow + "\n" + messageTomorrow
                        SummaryNotification(title, content.split("\n").toTypedArray())
                    } else if (sendToday) {
                        titleToday = "$titleToday $countToday"
                        SummaryNotification(titleToday, messageToday.split("\n").toTypedArray())
                    } else if (sendTomorrow) {
                        titleTomorrow = "$titleTomorrow $countTomorrow"
                        SummaryNotification(titleTomorrow, messageTomorrow.split("\n").toTypedArray())
                    }
                }

            }

            private fun notifMessageContent(content: SubstitutionList, vp: SubstitutionPlan): String {
                val message = java.lang.StringBuilder()
                val context = ApplicationFeatures.getContext()
                if (content.getNoInternet()) {
                    return ""
                }
                if (content.size() == 0) {
                    message.append(ApplicationFeatures.getContext().getString(R.string.notif_nothing)).append("\n")
                } else {
                    if (vp.senior) {
                        for (line in content.entries) {
                            if (line.isNothing()) {
                                message.append(line.hour).append(". ").append(context.getString(R.string.share_msg_nothing_hour_senior)).append(" ").append(line.course).append("\n")
                            } else {
                                message.append(line.hour).append(". ").append(context.getString(R.string.share_msg_hour_senior)).append(" ").append(line.course).append(" ").append(context.getString(R.string.share_msg_in_room)).append(" ").append(line.room).append(" ").append(context.getString(R.string.with_teacher)).append(" ").append(line.teacher).append(", ").append(line.moreInformation).append("\n")
                            }
                        }
                    } else {
                        for (line in content.entries) {
                            if (line.isNothing()) {
                                message.append(line.hour).append(". ").append(context.getString(R.string.share_msg_nothing_hour)).append("\n")
                            } else {
                                message.append(line.hour).append(". ").append(context.getString(R.string.share_msg_hour)).append(" ").append(line.course).append(" ").append(context.getString(R.string.share_msg_in_room)).append(" ").append(line.room).append(" ").append(context.getString(R.string.with_teacher)).append(" ").append(line.teacher).append(", ").append(line.moreInformation).append("\n")
                            }
                        }
                    }
                }
                return message.toString()
            }
        }

        private class MainNotification(var title: String, val content: SubstitutionList, val senior: Boolean, val profileName: String, var alert: Boolean, val id: Int = NOTIFICATION_MAIN_ID) {
            var nothing: Boolean = false
            var isOmitted: Boolean = false

            init {
                if (content.entries.size == 0) {
                    alert = false
                    nothing = true
                }
                sendNotification()
            }

            fun sendNotification() {
                val context = ApplicationFeatures.getContext()

                val style = NotificationCompat.MessagingStyle(Person.Builder().setName("me").build())
                style.conversationTitle = title

                for ((j, con) in content.entries.withIndex()) {
                    var color: Int
                    if (con.isNothing()) {
                        color = ContextCompat.getColor(context, R.color.notification_icon_background_omitted)
                        isOmitted = true
                    } else {
                        color = ContextCompat.getColor(context, R.color.notification_icon_background_substitution)
                    }

                    val textColor = if (con.isNothing())
                        ContextCompat.getColor(context, R.color.notification_icon_text_omitted)
                    else
                        ContextCompat.getColor(context, R.color.notification_icon_text_substitution)

                    var textSize = context.resources.getInteger(R.integer.notification_max_text_size) - context.resources.getInteger(R.integer.notification_text_size_substitution_factor) * con.hour.length
                    if (textSize < context.resources.getInteger(R.integer.notification_min_text_size))
                        textSize = context.resources.getInteger(R.integer.notification_min_text_size)

                    val drawable = ShapeTextDrawable(ShapeForm.ROUND, radius = 10f, text = con.hour, textSize = textSize, textBold = true, color = color, textColor = textColor)
                    val list = createMessage(con)
                    val person = Person.Builder().setName(list[0]).setIcon(IconCompat.createWithBitmap(drawable.toBitmap(context.resources.getInteger(R.integer.notification_bitmap_size), context.resources.getInteger(R.integer.notification_bitmap_size)))).build()
                    val message = "${list[1]} ${if (profileName.trim().isNotEmpty() && j == 0) " ($profileName)"; else ""}"
                    val message1 = NotificationCompat.MessagingStyle.Message(message, 0.toLong(), person)
                    style.addMessage(message1)
                }
                if (nothing) {
                    val person = Person.Builder().setName(context.getString(R.string.notif_nothing)).setIcon(IconCompat.createWithBitmap(ApplicationFeatures.vectorToBitmap(R.drawable.ic_check))).build()
                    val message1 = NotificationCompat.MessagingStyle.Message(if (profileName.trim().isNotEmpty()) " ($profileName)"; else "", 0.toLong(), person)
                    style.addMessage(message1)
                }


                //Intent
                // Create an Intent for the activity you want to start
                val resultIntent = Intent(ApplicationFeatures.getContext(), MainActivity::class.java)
                val stackBuilder = TaskStackBuilder.create(context)
                stackBuilder.addNextIntentWithParentStack(resultIntent)
                val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

                //Dismiss button intent
                val buttonIntent = Intent(context, NotificationDismissButtonReceiver::class.java)
                buttonIntent.action = "com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver"
                buttonIntent.putExtra(NotificationDismissButtonReceiver.EXTRA_NOTIFICATION_ID, id)
                val btPendingIntent = PendingIntent.getBroadcast(context, UUID.randomUUID().hashCode(), buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                val builder = NotificationCompat.Builder(context, if (alert) NOTIFICATION_MAIN_CHANNEL_ID else NOTIFICATION_SUMMARY_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_assignment_late)
                        .setStyle(style)
                        .setContentText(title)
                        .setContentIntent(resultPendingIntent)
                        .setPriority(if (alert) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setShowWhen(false)
                        .setOnlyAlertOnce(!alert)

                when {
                    nothing -> builder.color = ContextCompat.getColor(context, R.color.notification_icon_nothing_background)
                    isOmitted -> builder.color = ContextCompat.getColor(context, R.color.notification_icon_background_omitted)
                    else -> builder.color = ContextCompat.getColor(context, R.color.notification_icon_background_substitution)
                }

                if (PreferenceUtil.isAlwaysNotification()) {
                    builder.setOngoing(true)
                    builder.addAction(R.drawable.ic_close_black_24dp, ApplicationFeatures.getContext().getString(R.string.notif_dismiss), btPendingIntent)
                }

                if (alert)
                    createNotificationChannel(context)
                else {
                    SummaryNotification.createNotificationChannel(context)
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        builder.priority = NotificationCompat.PRIORITY_LOW
                    }
                }

                with(NotificationManagerCompat.from(context)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(id, builder.build())
                }

            }

            fun createMessage(entry: SubstitutionEntry): List<String> {
                val context = ApplicationFeatures.getContext()
                return if (entry.isNothing()) {
                    listOf("${entry.hour}. ${context.getString(R.string.share_msg_nothing_hour)}", "${entry.moreInformation} ${if (senior) "(${entry.course})"; else ""}")
                } else {
                    listOf("${if (!entry.subject.isBlank()) "${entry.subject} ${context.getString(R.string.with_teacher)} " else ""}${entry.teacher} ${context.getString(R.string.share_msg_in_room)} ${entry.room}", "${entry.hour}. ${context.getString(R.string.share_msg_hour)}${if (!entry.moreInformation.isBlank()) ", ${entry.moreInformation} " else " "} ${if (senior) "(${entry.course})"; else ""}")
                }

            }

            companion object {
                private fun createNotificationChannel(context: Context) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        val notificationChannel = NotificationChannel(NOTIFICATION_MAIN_CHANNEL_ID, context.getString(R.string.notification_main_channel_title), NotificationManager.IMPORTANCE_HIGH)

                        // Configure the notification channel.
                        notificationChannel.description = context.getString(R.string.notification_main_channel_description)
                        notificationChannel.enableLights(true)
                        notificationChannel.lightColor = ContextCompat.getColor(context, R.color.colorAccent)
                        notificationChannel.enableVibration(true)
                        notificationChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                        notificationManager.createNotificationChannel(notificationChannel)
                    }
                }
            }
        }

        private class SummaryNotification(val title: String, var content: Array<String>, val id: Int = NOTIFICATION_SUMMARY_ID_1) {
            init {
                val contentList = mutableListOf<String>()
                for (s in content) {
                    if (s.trim().isNotEmpty())
                        contentList.add(s)
                }
                if (contentList.size == 0)
                    contentList.add(ApplicationFeatures.getContext().getString(R.string.notif_nothing))

                content = contentList.toTypedArray()
                sendNotification()
            }

            private fun sendNotification() {
                if (!PreferenceUtil.isSummaryNotification())
                    return

                val context = ApplicationFeatures.getContext()

                //Intent
                // Create an Intent for the activity you want to start
                val resultIntent = Intent(ApplicationFeatures.getContext(), MainActivity::class.java)
                val stackBuilder = TaskStackBuilder.create(context)
                stackBuilder.addNextIntentWithParentStack(resultIntent)
                val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

                //Dismiss button intent
                val buttonIntent = Intent(context, NotificationDismissButtonReceiver::class.java)
                buttonIntent.action = "com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver"
                buttonIntent.putExtra(NotificationDismissButtonReceiver.EXTRA_NOTIFICATION_ID, id)
                val btPendingIntent = PendingIntent.getBroadcast(context, UUID.randomUUID().hashCode(), buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT)


                //Build notification
                val style = NotificationCompat.InboxStyle()
                for (s in content) {
                    style.addLine(s)
                }

                val maxLineSize = 6

                if (content.size > maxLineSize) {
                    style.setSummaryText("+" + (content.size - maxLineSize) + " " + context.getString(R.string.more))
                }

                val builder = NotificationCompat.Builder(context, NOTIFICATION_SUMMARY_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_assignment_white_24dp)
                        .setShowWhen(false)
                        .setStyle(style)
                        .setContentTitle(title)
                        .setContentIntent(resultPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .setOnlyAlertOnce(true)

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    builder.priority = NotificationCompat.PRIORITY_LOW
                } else
                    builder.priority = NotificationCompat.PRIORITY_DEFAULT

                if (PreferenceUtil.isAlwaysNotification()) {
                    builder.setOngoing(true)
                    builder.addAction(R.drawable.ic_close_black_24dp, ApplicationFeatures.getContext().getString(R.string.notif_dismiss), btPendingIntent)
                }

                createNotificationChannel(context)
                with(NotificationManagerCompat.from(context)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(id, builder.build())
                }
            }

            companion object {
                fun createNotificationChannel(context: Context) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        val notificationChannel = NotificationChannel(NOTIFICATION_SUMMARY_CHANNEL_ID, context.getString(R.string.notification_summary_channel_title), NotificationManager.IMPORTANCE_LOW)

                        // Configure the notification channel.
                        notificationChannel.description = context.getString(R.string.notification_summary_channel_description)
                        notificationChannel.enableLights(false)
                        notificationChannel.enableVibration(false)
                        notificationChannel.setSound(null, null)
                        notificationManager.createNotificationChannel(notificationChannel)
                        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                        notificationManager.createNotificationChannel(notificationChannel)
                    }
                }
            }
        }
    }
}