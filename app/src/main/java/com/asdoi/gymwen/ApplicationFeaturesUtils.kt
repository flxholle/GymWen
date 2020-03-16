package com.asdoi.gymwen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.asdoi.gymwen.profiles.ProfileManagement
import com.asdoi.gymwen.substitutionplan.SubstitutionList
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures
import com.github.stephenvinouze.shapetextdrawable.ShapeForm
import com.github.stephenvinouze.shapetextdrawable.ShapeTextDrawable


class ApplicationFeaturesUtils {

    companion object {
        class CreateMainNotification(val title: String, val content: SubstitutionList) : ApplicationFeatures.DownloadSubstitutionplanDocsTask() {

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

            fun sendNotification() {
                val context = ApplicationFeatures.getContext()

                val style = NotificationCompat.MessagingStyle(Person.Builder().setName("me").build())
                style.conversationTitle = title

                for (con in content.entries) {
                    val drawable = ShapeTextDrawable(ShapeForm.ROUND, radius = 10f, text = con.hour, textSize = 32, textBold = true, color = Color.RED)
//                    val person = Person.Builder().setName("$s. Stunde entfällt").setIcon(IconCompat.createWithBitmap(drawable.toBitmap(48, 48))).build()
//                    val message1 = NotificationCompat.MessagingStyle.Message("e Sportkurs", s.toLong(), person)
//                    style.addMessage(message1)
                }


                val builder = NotificationCompat.Builder(context, ApplicationFeatures.NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_assignment_late)
                        .setStyle(style)
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