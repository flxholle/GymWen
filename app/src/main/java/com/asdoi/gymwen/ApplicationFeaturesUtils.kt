package com.asdoi.gymwen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.asdoi.gymwen.profiles.ProfileManagement
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures


class ApplicationFeaturesUtils {

    companion object {
        class CreateMainNotification(val title: String, val content: Array<Array<String>>) : ApplicationFeatures.DownloadSubstitutionplanDocsTask() {

            override fun onPostExecute(v: Void?) {
                super.onPostExecute(v)
                try {
                    if (ProfileManagement.isUninit())
                        ProfileManagement.reload()
                    if (!ApplicationFeatures.coursesCheck(false))
                        return
                    if (SubstitutionPlanFeatures.getTodayTitle() == ApplicationFeatures.getContext().getString(R.string.noInternetConnection)) {
                        return
                    }
                    sendNotification()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            fun sendNotification() {
                val context = ApplicationFeatures.getContext()

                val person = Person.Builder().setName("1.Stunde").setIcon(IconCompat.createWithResource(context, R.drawable.ic_looks_one_black_24dp)).build()
                var message1 = NotificationCompat.MessagingStyle.Message("entfältt", 0, person)
                val person2 = Person.Builder().setName("2. Stunde").setIcon(IconCompat.createWithResource(context, R.drawable.ic_looks_two_black_24dp)).build()
                var message2 = NotificationCompat.MessagingStyle.Message("HEM, 109", 0, person2)

                var builder = NotificationCompat.Builder(context, ApplicationFeatures.NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_assignment_late)
//                        .setStyle(NotificationCompat.MessagingStyle(person)
//                                .setConversationTitle("Vertretung heute:")
//                                .addMessage(message1)
//                                .addMessage(message2))
                        .setStyle(NotificationCompat.InboxStyle()
                                .addLine("1. Stunde entfällt")
                                .addLine("2. stunde bei HEM")
                                .setBigContentTitle("Heute, 2020")
                                .setSummaryText("+2 more"))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                createNotificationChannel(context)
                with(NotificationManagerCompat.from(context)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(0, builder.build())
                }

            }

            private fun createNotificationChannel(context: Context) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val notificationChannel = NotificationChannel(ApplicationFeatures.NOTIFICATION_CHANNEL_ID, context.getString(R.string.notification_channel_title), NotificationManager.IMPORTANCE_LOW)

                    // Configure the notification channel.
                    notificationChannel.description = context.getString(R.string.notification_channel_description)
                    notificationChannel.enableLights(false)
                    //                    notificationChannel.setLightColor(ContextCompat.getColor(context, R.color.colorAccent));
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