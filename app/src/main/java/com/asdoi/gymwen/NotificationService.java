package com.asdoi.gymwen;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.asdoi.gymwen.VertretungsplanInternal.VertretungsPlan;
import com.asdoi.gymwen.main.MainActivity;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

public class NotificationService extends Service {
    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new createNotification().execute();
        return Service.START_NOT_STICKY;
    }

    public static class createNotification extends DummyApplication.downloadDocsTask {

        @Override
        protected void onPostExecute(Void v) {
            if (VertretungsPlan.getTodayTitle().equals("Keine Internetverbindung!")) {
                return;
            }
            proofeNotification();
        }

        public void proofeNotification() {
            //Spanned spanBody = Html.fromHtml(notificationMessage());
            String body = notificationMessage();
            createNotification(body);

        }

        private void createNotification(String body) {
            Context context = DummyApplication.getContext();

            // Create an Intent for the activity you want to start
            Intent resultIntent = new Intent(DummyApplication.getContext(), MainActivity.class);
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            // Get the PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

            //Build notification
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setContentTitle(context.getString(R.string.notif_content_title))
                    .setOngoing(true);
            notificationBuilder.setContentIntent(resultPendingIntent);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, context.getString(R.string.notification_channel_title), NotificationManager.IMPORTANCE_HIGH);

                // Configure the notification channel.
                notificationChannel.setDescription(context.getString(R.string.notification_channel_description));
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(ContextCompat.getColor(context, R.color.colorAccent));
                notificationChannel.setVibrationPattern(new long[]{400});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

                notificationManager.createNotificationChannel(notificationChannel);
            } else {
                notificationBuilder.setPriority(Notification.PRIORITY_MAX);
            }

            notificationManager.notify(/*notification id*/1, notificationBuilder.build());
        }

        private String notificationMessage() {
            String message = "";
            String day = VertretungsPlan.getTodayTitle();
            String[][] inhalt = VertretungsPlan.getTodayArray();

            message += notifMessageContent(inhalt, day);

            day = VertretungsPlan.getTomorrowTitle();
            inhalt = VertretungsPlan.getTomorrowArray();

            message += notifMessageContent(inhalt, day);
            message = message.substring(0, message.length() - 1);
            return message;
        }

        private String notifMessageContent(String[][] inhalt, String day) {
            String message = "";
            if (inhalt == null) {
                message += day + ": keine Vertretung\n";
            } else {
                message = day + ":\n";
                if (VertretungsPlan.getOberstufe()) {
                    for (String[] line : inhalt) {
                        if (line[3].equals("entfällt")) {
                            message += line[1] + ". Stunde entfällt\n";
                        } else {
                            message += line[1] + ". Stunde, " + line[0] + ", " + line[4] + ", " + line[3] + " " + line[5] + "\n";
                        }
                    }
                } else {
                    for (String[] line : inhalt) {
                        if (line[3].equals("entfällt")) {
                            message += line[1] + ". Stunde entfällt\n";
                        } else {
                            message += line[1] + ". Stunde " + line[2] + " bei " + line[3] + ", " + line[4] + " " + line[5] + "\n";
                        }
                    }
                }
            }
            return message;
        }
    }
}
