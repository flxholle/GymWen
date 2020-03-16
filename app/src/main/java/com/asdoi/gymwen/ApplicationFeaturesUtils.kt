package com.asdoi.gymwen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.PreferenceManager
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
const val NOTIFICATION_MAIN_ID = 3
const val NOTIFICATION_SUMMARY_ID_1 = 4
const val NOTIFICATION_SUMMARY_ID_2 = 5

class ApplicationFeaturesUtils {

    companion object {
        class CreateNotification(val alert: Boolean) : ApplicationFeatures.DownloadSubstitutionplanDocsTask() {
            private val summarize = PreferenceUtil.isSummarizeUp()

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

            fun createNotification() {
                if (ProfileManagement.isUninit())
                    ProfileManagement.reload()

                val profileList = ProfileManagement.getProfileList()

                val titleTodayArray = SubstitutionPlanFeatures.getTodayTitle()
                val titleTomorrowArray = SubstitutionPlanFeatures.getTomorrowTitle()
                var titleToday = titleTodayArray.date + ", " + titleTodayArray.dayOfWeek + ":"
                var titleTomorrow = titleTomorrowArray.date + ", " + titleTomorrowArray.dayOfWeek + ":"

                //Send main notif for preferred Profile
                var daySendInSummaryNotif = -1 //0 = today; 1 = tomorrow

                val preferredProfile = ProfileManagement.getPreferredProfile()
                val preferredProfilePos = ProfileManagement.getPreferredProfilePosition()

                if (preferredProfile != null) {
                    var whichDayIsToday = -1
                    val titleCodeToday = SubstitutionPlanFeatures.getTodayTitleCode()
                    val titleCodeTomorrow = SubstitutionPlanFeatures.getTomorrowTitleCode()
                    if (SubstitutionPlanFeatures.isTitleCodeToday(titleCodeToday))
                        whichDayIsToday = 0
                    else if (SubstitutionPlanFeatures.isTitleCodeToday(titleCodeTomorrow))
                        whichDayIsToday = 1

                    val temp = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), preferredProfile.coursesArray)
                    daySendInSummaryNotif = when (whichDayIsToday) {
                        0 -> {
                            MainNotification(SubstitutionPlanFeatures.getTodayTitleString(), temp.getDay(true), alert)
                            1
                        }
                        1 -> {
                            MainNotification(SubstitutionPlanFeatures.getTomorrowTitleString(), temp.getDay(false), alert)
                            0
                        }
                        else -> -1
                    }
                }

                val isMoreThanOneProfile = ProfileManagement.isMoreThanOneProfile()

                //Both
                val countTotal = StringBuilder()

                //Today
                val countToday = StringBuilder()
                var messageToday = StringBuilder()
                var isNoToday = false

                //Tomorrow
                val countTomorrow = StringBuilder()
                var messageTomorrow = StringBuilder()
                var isNoTomorrow = false

                for (i in profileList.indices) {
                    val temp = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), profileList.get(i).coursesArray)

                    if (i == preferredProfilePos && daySendInSummaryNotif > 0) {
                        if (daySendInSummaryNotif == 1) {
                            //Tomorrow
                            val content = if (summarize) temp.getDay(false).summarizeUp("-") else temp.getDay(false)
                            try {
                                countTomorrow.append(content.entries.size)
                                countTomorrow.append(", ")
                                countTotal.append(content.entries.size)
                                countTotal.append("|")
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
                    var content = if (summarize) temp.getDay(true).summarizeUp("-") else temp.getDay(true)
                    try {
                        countToday.append(content.entries.size)
                        countToday.append(", ")
                        countTotal.append(content.entries.size)
                        countTotal.append(" ")
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

                    if (i == preferredProfilePos && daySendInSummaryNotif > 0) {
                        break
                    }

                    //Tomorrow
                    content = if (summarize) temp.getDay(false).summarizeUp("-") else temp.getDay(false)
                    try {
                        countTomorrow.append(content.entries.size)
                        countTomorrow.append(", ")
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

                countToday.deleteCharAt(countToday.lastIndexOf(", "))
                countTomorrow.deleteCharAt(countTomorrow.lastIndexOf(", "))
                countTotal.deleteCharAt(countTotal.lastIndexOf(" "))

                if (isNoToday) messageToday = StringBuilder("${ApplicationFeatures.getContext().getString(R.string.notif_nothing)}\n")
                if (isNoTomorrow) messageTomorrow = StringBuilder("${ApplicationFeatures.getContext().getString(R.string.notif_nothing)}\n")

                val twoNotifs = PreferenceUtil.isTwoNotifications()

                if (!isMoreThanOneProfile) {
                    if (daySendInSummaryNotif == 0) {
                        titleToday = "$titleToday: $countToday"
                        SummaryNotification(titleToday, messageToday.split("\n").toTypedArray())
                        return
                    } else if (daySendInSummaryNotif == 1) {
                        titleTomorrow = "$titleTomorrow: $countTomorrow"
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
                    val title = "${ApplicationFeatures.getContext().getString(R.string.notif_content_title)} $countTotal"
                    messageToday.append(messageTomorrow)
                    SummaryNotification(title, messageToday.split("\n").toTypedArray())
                }
            }

            fun notifMessageContent(content: SubstitutionList?, vp: SubstitutionPlan): String {
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

        class MainNotification(val title: String, val content: SubstitutionList, val alert: Boolean, val id: Int = NOTIFICATION_MAIN_ID) {
            init {
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

                    val drawable = ShapeTextDrawable(ShapeForm.ROUND, radius = 10f, text = con.hour, textSize = 32, textBold = true, color = color, textColor = textColor)
                    val list = createMessage(con)
                    val person = Person.Builder().setName(list[0]).setIcon(IconCompat.createWithBitmap(drawable.toBitmap(48, 48))).build()
                    val message1 = NotificationCompat.MessagingStyle.Message(list[1], 0.toLong(), person)
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
                buttonIntent.putExtra("EXTRA_NOTIFICATION_ID", id)
                val btPendingIntent = PendingIntent.getBroadcast(context, UUID.randomUUID().hashCode(), buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT)


                val builder = NotificationCompat.Builder(context, NOTIFICATION_MAIN_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_assignment_late)
                        .setStyle(style)
                        .setContentText(title)
                        .setContentIntent(resultPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(alert)

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

            fun createMessage(entry: SubstitutionEntry): List<String> {
                val context = ApplicationFeatures.getContext()
                if (SubstitutionPlanFeatures.isNothing(entry.teacher)) {
                    return listOf("${entry.hour}. ${context.getString(R.string.share_msg_nothing_hour)}", entry.moreInformation)
                } else {
                    return listOf("${entry.hour}. ${context.getString(R.string.share_msg_hour)} ${context.getString(R.string.share_msg_in_room)} ${entry.room} ${context.getString(R.string.with_teacher)} ${entry.teacher}", "${entry.moreInformation} (${entry.course})")
                }

            }

            private fun createNotificationChannel(context: Context) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val notificationChannel = NotificationChannel(NOTIFICATION_MAIN_CHANNEL_ID, context.getString(R.string.notification_channel_title), NotificationManager.IMPORTANCE_HIGH)

                    // Configure the notification channel.
                    notificationChannel.description = context.getString(R.string.notification_channel_description)
                    if (alert) {
                        notificationChannel.enableLights(true)
                        notificationChannel.lightColor = ContextCompat.getColor(context, R.color.colorAccent)
                        notificationChannel.enableVibration(true)
//                        notificationChannel.setSound(true, null)
                    }
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
                buttonIntent.putExtra("EXTRA_NOTIFICATION_ID", id)
                val btPendingIntent = PendingIntent.getBroadcast(context, UUID.randomUUID().hashCode(), buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT)


                //Build notification
                val style = NotificationCompat.InboxStyle()
                for (s in content) {
                    style.addLine(s)
                }
                if (content.size > 7) {
                    style.setSummaryText("+" + (content.size - 7) + " " + context.getString(R.string.more))
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

            private fun createNotificationChannel(context: Context) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val notificationChannel = NotificationChannel(NOTIFICATION_SUMMARY_CHANNEL_ID, context.getString(R.string.notification_channel_title), NotificationManager.IMPORTANCE_DEFAULT)

                    // Configure the notification channel.
                    notificationChannel.description = context.getString(R.string.notification_channel_description)
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