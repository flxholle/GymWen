package com.asdoi.gymwen

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
import androidx.preference.PreferenceManager
import com.asdoi.gymwen.profiles.Profile
import com.asdoi.gymwen.profiles.ProfileManagement
import com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver
import com.asdoi.gymwen.substitutionplan.SubstitutionEntry
import com.asdoi.gymwen.substitutionplan.SubstitutionList
import com.asdoi.gymwen.substitutionplan.SubstitutionPlan
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures
import com.asdoi.gymwen.ui.activities.MainActivity
import com.asdoi.gymwen.util.PreferenceUtil
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
        const val none = -1

        class CreateNotification(val alert: Boolean) : ApplicationFeatures.DownloadSubstitutionplanDocsTask() {
            private val summarize = PreferenceUtil.isSummarizeUp()
            private val alertForAllProfiles = PreferenceUtil.isMainNotifForAllProfiles()
            private val dontChangeSummary = PreferenceUtil.isDontChangeSummary()

            override fun onPostExecute(v: Void?) {
                super.onPostExecute(v)
                try {
                    if (ProfileManagement.isUninit())
                        ProfileManagement.reload()
                    if (!ApplicationFeatures.coursesCheck(false))
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
                if (ProfileManagement.isUninit())
                    ProfileManagement.reload()

                val profileList = ProfileManagement.getProfileList()

                val titleTodayArray = SubstitutionPlanFeatures.getTodayTitle()
                val titleTomorrowArray = SubstitutionPlanFeatures.getTomorrowTitle()
                var titleToday = "${titleTodayArray.dayOfWeek}, ${titleTodayArray.date}:"
                var titleTomorrow = "${titleTomorrowArray.dayOfWeek}, ${titleTomorrowArray.date}:"

                val isMoreThanOneProfile = ProfileManagement.isMoreThanOneProfile()

                //Send main notif for preferred Profile
                var daySendInSummaryNotif = none //1 = today; 2 = tomorrow

                val preferredProfile = ProfileManagement.getPreferredProfile()
                val preferredProfilePos = if (alertForAllProfiles) -5 else ProfileManagement.getPreferredProfilePosition()

                if (preferredProfile != null || alertForAllProfiles) {
                    var whichDayIsToday = none
                    val titleCodeToday = SubstitutionPlanFeatures.getTodayTitleCode()
                    val titleCodeTomorrow = SubstitutionPlanFeatures.getTomorrowTitleCode()
                    if (SubstitutionPlanFeatures.isTitleCodeToday(titleCodeToday))
                        whichDayIsToday = today
                    else if (SubstitutionPlanFeatures.isTitleCodeToday(titleCodeTomorrow))
                        whichDayIsToday = tomorrow

                    var checkProfileList = mutableListOf<Profile>()
                    if (!alertForAllProfiles && preferredProfile != null)
                        checkProfileList.add(preferredProfile)
                    else
                        checkProfileList = ProfileManagement.getProfileList()

                    for (p in checkProfileList.indices) {
                        val temp = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), checkProfileList.get(p).coursesArray)
                        daySendInSummaryNotif = when (whichDayIsToday) {
                            today -> {
                                MainNotification(SubstitutionPlanFeatures.getTodayTitleString(), temp.getDay(true), if (isMoreThanOneProfile && alertForAllProfiles) checkProfileList.get(p).name; else "", alert, p)
                                tomorrow
                            }
                            tomorrow -> {
                                MainNotification(SubstitutionPlanFeatures.getTomorrowTitleString(), temp.getDay(false), if (isMoreThanOneProfile && alertForAllProfiles) checkProfileList.get(p).name; else "", alert, p)
                                today
                            }
                            else -> none
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

                for (i in profileList.indices) {
                    val temp = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), profileList.get(i).coursesArray)

                    if (i == preferredProfilePos && daySendInSummaryNotif != none && !dontChangeSummary) {
                        if (daySendInSummaryNotif == tomorrow) {
                            //Tomorrow
                            var content = temp.getDay(false)
                            try {
                                countTomorrow.append(content.entries.size)
                                countTomorrow.append(", ")
                                countTotal.append(content.entries.size)
                                countTotal.append(", ")
                                content = if (summarize) temp.getDay(false).summarizeUp("-") else content
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
                    }

                    //Today
                    var content = temp.getDay(true)
                    try {
                        countToday.append(content.entries.size)
                        countToday.append(", ")
                        countTotal.append(content.entries.size)
                        countTotal.append("|")
                        content = if (summarize) temp.getDay(true).summarizeUp("-") else content
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


                    if (i == preferredProfilePos && daySendInSummaryNotif != none && !dontChangeSummary) {
                        if (!dontChangeSummary) {
                            countTotal.deleteCharAt(countTotal.lastIndexOf("|"))
                            countTotal.append(", ")
                        }
                        continue
                    }

                    //Tomorrow
                    content = temp.getDay(false)
                    try {
                        countTomorrow.append(content.entries.size)
                        countTomorrow.append(", ")
                        countTotal.append(content.entries.size)
                        countTotal.append(", ")
                        content = if (summarize) temp.getDay(false).summarizeUp("-") else content
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
                    e.printStackTrace()
                }
                try {
                    countTotal.deleteCharAt(countTotal.lastIndexOf(", "))
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

                if (isNoToday) messageToday = StringBuilder("${ApplicationFeatures.getContext().getString(R.string.notif_nothing)}\n")
                if (isNoTomorrow) messageTomorrow = StringBuilder("${ApplicationFeatures.getContext().getString(R.string.notif_nothing)}\n")

                val twoNotifs = PreferenceUtil.isTwoNotifications()

                if (!isMoreThanOneProfile || alertForAllProfiles) {
                    if (daySendInSummaryNotif == today) {
                        titleToday = "$titleToday $countToday"
                        SummaryNotification(titleToday, messageToday.split("\n").toTypedArray())
                        return
                    } else if (daySendInSummaryNotif == tomorrow) {
                        titleTomorrow = "$titleTomorrow $countTomorrow"
                        SummaryNotification(titleTomorrow, messageTomorrow.split("\n").toTypedArray())
                        return
                    }
                }

                if (twoNotifs) {
                    titleToday = "$titleToday $countToday"
                    titleTomorrow = "$titleTomorrow $countTomorrow"
                    SummaryNotification(titleToday, messageToday.split("\n").toTypedArray())
                    SummaryNotification(titleTomorrow, messageTomorrow.split("\n").toTypedArray(), NOTIFICATION_SUMMARY_ID_2)
                } else {
                    //Sort notification
                    val title = "${ApplicationFeatures.getContext().getString(R.string.notif_content_title)} $countTotal"
                    val content = titleToday + "\n" + messageToday + titleTomorrow + "\n" + messageTomorrow
                    SummaryNotification(title, content.split("\n").toTypedArray())
                }

            }

            private fun notifMessageContent(content: SubstitutionList?, vp: SubstitutionPlan): String {
                val message = java.lang.StringBuilder()
                if (content == null || content.getNoInternet()) {
                    return ""
                }
                if (content.size() == 0) {
                    message.append(ApplicationFeatures.getContext().getString(R.string.notif_nothing)).append("\n")
                } else {
                    if (vp.senior) {
                        for (line in content.entries) {
                            if (SubstitutionPlanFeatures.isNothing(line.teacher)) {
                                message.append(line.hour).append(". Stunde entfällt\n")
                            } else {
                                message.append(line.hour).append(". Stunde, ").append(line.course).append(", ").append(line.room).append(", ").append(line.teacher).append(" ").append(line.moreInformation).append("\n")
                            }
                        }
                    } else {
                        for (line in content.entries) {
                            if (SubstitutionPlanFeatures.isNothing(line.teacher)) {
                                message.append(line.hour).append(". Stunde entfällt\n")
                            } else {
                                message.append(line.hour).append(". Stunde ").append(line.subject).append(" bei ").append(line.teacher).append(", ").append(line.room).append(" ").append(line.moreInformation).append("\n")
                            }
                        }
                    }
                }
                return message.toString()
            }
        }

        private class MainNotification(var title: String, val content: SubstitutionList, val profileName: String, var alert: Boolean, val id: Int = NOTIFICATION_MAIN_ID) {
            var nothing: Boolean = false

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

                for (con in content.entries) {
                    val color = if (SubstitutionPlanFeatures.isNothing(con.teacher))
                        ContextCompat.getColor(context, R.color.notification_icon_background_omitted)
                    else
                        ContextCompat.getColor(context, R.color.notification_icon_background_substitution)

                    val textColor = if (SubstitutionPlanFeatures.isNothing(con.teacher))
                        ContextCompat.getColor(context, R.color.notification_icon_text_omitted)
                    else
                        ContextCompat.getColor(context, R.color.notification_icon_text_substitution)

                    val textSize = if (con.hour.length > 1) 32 else 36

                    val drawable = ShapeTextDrawable(ShapeForm.ROUND, radius = 10f, text = con.hour, textSize = textSize, textBold = true, color = color, textColor = textColor)
                    val list = createMessage(con)
                    val person = Person.Builder().setName(list[0]).setIcon(IconCompat.createWithBitmap(drawable.toBitmap(48, 48))).build()
                    val message1 = NotificationCompat.MessagingStyle.Message("${list[1]} ${if (!profileName.trim().isEmpty()) " ($profileName)"; else ""}", 0.toLong(), person)
                    style.addMessage(message1)
                }
                if (nothing) {
                    val person = Person.Builder().setName(context.getString(R.string.notif_nothing)).setIcon(IconCompat.createWithBitmap(ApplicationFeatures.vectorToBitmap(R.drawable.ic_check))).build()
                    val message1 = NotificationCompat.MessagingStyle.Message(if (!profileName.trim().isEmpty()) " ($profileName)"; else "", 0.toLong(), person)
                    style.addMessage(message1)
                } /*else {
                    title = "$title ($profileName)"
                }*/


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
                        .setWhen(System.currentTimeMillis())
                        .setOnlyAlertOnce(!alert)

                if (PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("alwaysNotification", true)) {
                    builder.setOngoing(true)
                    builder.addAction(R.drawable.ic_close_black_24dp, ApplicationFeatures.getContext().getString(R.string.notif_dismiss), btPendingIntent)
                }

                if (alert)
                    createNotificationChannel(context)
                else
                    SummaryNotification.createNotificationChannel(context)
                with(NotificationManagerCompat.from(context)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(id, builder.build())
                }

            }

            fun createMessage(entry: SubstitutionEntry): List<String> {
                val context = ApplicationFeatures.getContext()
                return if (SubstitutionPlanFeatures.isNothing(entry.teacher)) {
                    listOf("${entry.hour}. ${context.getString(R.string.share_msg_nothing_hour)}", entry.moreInformation)
                } else {
                    listOf("${entry.hour}. ${context.getString(R.string.share_msg_hour)} ${context.getString(R.string.share_msg_in_room)} ${entry.room} ${context.getString(R.string.with_teacher)} ${entry.teacher}", "${entry.moreInformation} (${entry.course})")
                }

            }

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
                    notificationManager.createNotificationChannel(notificationChannel)
                    notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    notificationManager.createNotificationChannel(notificationChannel)
                }
            }
        }

        class SummaryNotification(val title: String, var content: Array<String>, val id: Int = NOTIFICATION_SUMMARY_ID_1) {
            init {
                val contentList = mutableListOf<String>()
                for (s in content) {
                    if (!s.trim().isEmpty())
                        contentList.add(s)
                }
                if (contentList.size == 0)
                    contentList.add(ApplicationFeatures.getContext().getString(R.string.notif_nothing))

                content = contentList.toTypedArray()
                sendNotification()
            }

            private fun sendNotification() {
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
                        .setSmallIcon(R.drawable.ic_assignment_black_24dp)
                        .setStyle(style)
                        .setContentTitle(title)
                        .setContentIntent(resultPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setPriority(Notification.PRIORITY_LOW)
                        .setOnlyAlertOnce(true)

                if (PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("alwaysNotification", true)) {
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