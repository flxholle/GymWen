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

package com.asdoi.gymwen.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver;
import com.asdoi.gymwen.ui.activities.MainActivity;
import com.asdoi.gymwen.ui.activities.WebsiteActivity;
import com.prof.rssparser.Article;
import com.prof.rssparser.Channel;
import com.prof.rssparser.OnTaskCompleted;
import com.prof.rssparser.Parser;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RSS_Feed {
    @NonNull
    public static final String CHANNEL_ID = "RSS_notification";

    public static void checkRSS(@NonNull Context context) {
        Parser parser = new Parser();
        parser.onFinish(new OnTaskCompleted() {

            //what to do when the parsing is done
            @Override
            public void onTaskCompleted(@NonNull Channel channel) {
                // Use the channel info
                List<Article> articles = channel.getArticles();
                if (articles.size() > 0) {
                    if (!Objects.requireNonNull(articles.get(0).getTitle()).equalsIgnoreCase(PreferenceUtil.getLastLoadedRSSTitle())) {
                        //Send Notification
                        Intent notificationIntent = new Intent(context, WebsiteActivity.class);
                        notificationIntent.putExtra(WebsiteActivity.LOADURL, articles.get(0).getLink());
                        sendRSSNotification(articles.get(0), context, R.drawable.ic_compass, notificationIntent);
                        PreferenceUtil.setLastLoadedRSSTitle(articles.get(0).getTitle());
                    }
                }
            }

            //what to do in case of error
            @Override
            public void onError(@NonNull Exception e) {
                // Handle the exception
            }
        });
        parser.execute(External_Const.rss_feed_Link);


        Parser parser2 = new Parser();
        parser2.onFinish(new OnTaskCompleted() {

            //what to do when the parsing is done
            @Override
            public void onTaskCompleted(@NonNull Channel channel) {
                // Use the channel info
                List<Article> articles = channel.getArticles();
                if (articles.size() > 0) {
                    if (!Objects.requireNonNull(articles.get(0).getTitle()).equalsIgnoreCase(PreferenceUtil.getLastLoadedRSSTitle2())) {
                        //Send Notification
                        Intent notificationIntent = new Intent(context, MainActivity.class);
                        notificationIntent.setAction(MainActivity.LOADURL);
                        notificationIntent.putExtra(MainActivity.LOADURL, articles.get(0).getLink());
                        sendRSSNotification(articles.get(0), context, R.drawable.ic_gitlab, notificationIntent);
                        PreferenceUtil.setLastLoadedRSSTitle2(articles.get(0).getTitle());
                    }
                }
            }

            //what to do in case of error
            @Override
            public void onError(@NonNull Exception e) {
                // Handle the exception
            }
        });
        parser2.execute(External_Const.rss_feed_Link_2);
    }

    private static void sendRSSNotification(@Nullable Article article, @NonNull Context context, int smallIcon, @NonNull Intent notificationIntent) {
        if (article == null || !PreferenceUtil.isRSSNotification() || Build.VERSION.SDK_INT < 21)
            return;

        int id = Objects.requireNonNull(article.getTitle()).hashCode();

        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        createNotificationChannel(context);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(smallIcon)
                .setContentTitle(article.getTitle())
                .setContentText(article.getDescription())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setWhen(when)
                .setContentIntent(pendingIntent);

        if (com.asdoi.gymwen.util.PreferenceUtil.isAlwaysNotification()) {
            //Dismiss button intent
            Intent buttonIntent = new Intent(context, NotificationDismissButtonReceiver.class);
            buttonIntent.setAction("com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver");
            buttonIntent.putExtra(NotificationDismissButtonReceiver.EXTRA_NOTIFICATION_ID, id);
            PendingIntent btPendingIntent = PendingIntent.getBroadcast(context, UUID.randomUUID().hashCode(), buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder.setOngoing(true);
            notificationBuilder.addAction(R.drawable.ic_close_black_24dp, context.getString(R.string.notif_dismiss), btPendingIntent);
        }

        if (notificationManager != null) {
            notificationManager.notify(id, notificationBuilder.build());
        }
    }

    private static void createNotificationChannel(@NonNull Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.rss_notification_channel), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.rss_notification_channel_description));
            channel.enableLights(true);
            channel.setLightColor(ContextCompat.getColor(context, R.color.colorAccent));
            channel.enableVibration(true);
            channel.enableVibration(true);
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }
    }

}
