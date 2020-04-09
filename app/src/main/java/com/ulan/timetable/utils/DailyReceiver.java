package com.ulan.timetable.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver;
import com.asdoi.gymwen.util.PreferenceUtil;
import com.ulan.timetable.activities.MainActivity;

import java.util.Calendar;
import java.util.UUID;

/**
 * Created by Ulan on 28.01.2019.
 */
public class DailyReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "timetable_notification";

    Context context;
    DbHelper db;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(@NonNull Context context, Intent intent) {
        this.context = context;

        String message;

        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        db = new DbHelper(context);
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        message = getLessons(day);
        if (message == null || !PreferenceUtil.isTimeTableNotification())
            return;

        createNotificationChannel(context);
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_assignment_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.gymlogo))
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(message)
                .setAutoCancel(true)
                .setWhen(when)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent);


        if (PreferenceUtil.isAlwaysNotification()) {
            //Dismiss button intent
            Intent buttonIntent = new Intent(context, NotificationDismissButtonReceiver.class);
            buttonIntent.setAction("com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver");
            buttonIntent.putExtra(NotificationDismissButtonReceiver.EXTRA_NOTIFICATION_ID, 5);
            PendingIntent btPendingIntent = PendingIntent.getBroadcast(context, UUID.randomUUID().hashCode(), buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mNotifyBuilder.setOngoing(true);
            mNotifyBuilder.addAction(R.drawable.ic_close_black_24dp, context.getString(R.string.notif_dismiss), btPendingIntent);
        }

        if (notificationManager != null) {
            notificationManager.notify(5, mNotifyBuilder.build());
        }
    }

    private void createNotificationChannel(@NonNull Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "name";
            String description = "Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(ContextCompat.getColor(context, R.color.colorAccent));
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getLessons(int day) {
        StringBuilder lessons = new StringBuilder("");
        String currentDay = getCurrentDay(day);

        db.getWeek(currentDay).forEach(week -> {
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
        });

        return !lessons.toString().equals("") ? lessons.toString() : null;
    }

    @Nullable
    private String getCurrentDay(int day) {
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