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
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures
import com.asdoi.gymwen.ui.activities.MainActivity
import com.github.stephenvinouze.shapetextdrawable.ShapeForm
import com.github.stephenvinouze.shapetextdrawable.ShapeTextDrawable
import java.util.*

const val NOTIFICATION_MAIN_CHANNEL_ID = "substitutionchannel_02"
const val NOTIFICATION_SUMMARY_CHANNEL_ID = "substitutionchannel_01"
const val NOTIFICATION_MAIN_ID = 3
const val NOTIFICATION_SUMMARY_ID = 4

class ApplicationFeaturesUtils {

    companion object {
        class CreateMainNotification(val alert: Boolean, val id: String = NOTIFICATION_MAIN_ID.toString()) : ApplicationFeatures.DownloadSubstitutionplanDocsTask() {

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
                    sendNotification()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            fun sendNotification(title: String, content: SubstitutionList) {
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


                val builder = NotificationCompat.Builder(context, id)
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
                    notify(NOTIFICATION_MAIN_ID, builder.build())
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

        class CreateSummaryNotification(val id: String = NOTIFICATION_SUMMARY_ID.toString()) : ApplicationFeatures.DownloadSubstitutionplanDocsTask() {
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
                    sendNotification()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            private fun sendNotification(title: String, content: Array<String>) {
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

                val builder = NotificationCompat.Builder(context, id)
                        .setSmallIcon(R.drawable.ic_assignment_black_24dp)
                        .setStyle(style)
                        .setContentText(title)
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
                    notify(NOTIFICATION_MAIN_ID, builder.build())
                }
            }

            private fun createNotificationChannel(context: Context) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val notificationChannel = NotificationChannel(NOTIFICATION_SUMMARY_CHANNEL_ID, context.getString(R.string.notification_channel_title), NotificationManager.IMPORTANCE_MIN)

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