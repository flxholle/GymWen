package com.asdoi.gymwen;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.asdoi.gymwen.VertretungsplanInternal.VertretungsPlan;
import com.asdoi.gymwen.main.Fragments.VertretungFragment;
import com.asdoi.gymwen.main.MainActivity;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class NotificationService extends Service {
    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new createNotification(this).execute(VertretungsPlan.todayURL, VertretungsPlan.tomorrowURL);
        return Service.START_NOT_STICKY;
    }

    public static class createNotificationOld extends VertretungFragment.downloadDoc {
        private Context mA;

        public createNotificationOld(Context a) {
            mA = a;
        }

        @Override
        protected void onPostExecute(Document[] result) {
            setDocs(result);
            if (VertretungsPlan.getTodayTitle().equals("Keine Internetverbindung!")) {
                return;
            }
            proofeNotification();
        }

        public void proofeNotification() {
            // Create an Intent for the activity you want to start
            Intent resultIntent = new Intent(mA, MainActivity.class);
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mA);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            // Get the PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


            NotificationManager notificationManager = (NotificationManager) mA.getSystemService(NOTIFICATION_SERVICE);
            String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

//        Spanned spanBody = Html.fromHtml(notificationMessage());
            String body = notificationMessage();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "GymWen Vertretung", NotificationManager.IMPORTANCE_LOW);

                // Configure the notification channel.
                notificationChannel.setDescription("Channel description");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.setVibrationPattern(new long[]{400});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }


            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mA, NOTIFICATION_CHANNEL_ID);

            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentTitle("Vertretung:")
                    .setOngoing(true);
            notificationBuilder.setContentIntent(resultPendingIntent);

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

    public static class createNotification extends AsyncTask<String, Void, Document[]> {
        private Context mA;

        @Override
        protected Document[] doInBackground(String... strURL) {
            Document[] doc = new Document[strURL.length];
            if (!VertretungsPlan.areDocsDownloaded()) {
                for (int i = 0; i < strURL.length; i++) {

                    String authString = VertretungsPlan.strUserId + ":" + VertretungsPlan.strPasword;

                    String lastAuthString = VertretungsPlan.lastAuthString;
                    //Check if already tried logging in with this authentication and if it failed before, return null
                    if (lastAuthString.length() > 1 && lastAuthString.substring(0, lastAuthString.length() - 1).equals(authString) && lastAuthString.charAt(lastAuthString.length() - 1) == 'f') {
                        System.out.println("failed before with same authString");
                        //return doc;
                    }

                    String encodedString =
                            new String(Base64.encodeBase64(authString.getBytes()));

                    try {
                        doc[i] = Jsoup.connect(strURL[i])
                                .header("Authorization", "Basic " + encodedString)
                                .get();

                        System.out.println("Logged in using basic authentication");
                        VertretungsPlan.lastAuthString = authString + "t";


                    } catch (IOException e) {
                        e.printStackTrace();
                        VertretungsPlan.lastAuthString = authString + "f";
                        return null;
                    }
                }
            }
            return doc;
        }

        public createNotification(Context a) {
            mA = a;
        }

        @Override
        protected void onPostExecute(Document[] result) {
            setDocs(result);
            if (VertretungsPlan.getTodayTitle().equals("Keine Internetverbindung!")) {
                return;
            }
            proofeNotification();
        }

        public void setDocs(Document[] values) {
            if (!VertretungsPlan.areDocsDownloaded()) {
                if (values == null) {
                    VertretungsPlan.setDocs(null, null);
                    return;
                }
                if (values.length == 2) {
                    VertretungsPlan.setDocs(values[0], values[1]);
                } else if (values.length == 1) {
                    VertretungsPlan.setTodayDoc(values[0]);
                }
            }
        }

        public void proofeNotification() {
            // Create an Intent for the activity you want to start
            Intent resultIntent = new Intent(mA, MainActivity.class);
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mA);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            // Get the PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


            NotificationManager notificationManager = (NotificationManager) mA.getSystemService(NOTIFICATION_SERVICE);
            String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

//        Spanned spanBody = Html.fromHtml(notificationMessage());
            String body = notificationMessage();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "GymWen Vertretung", NotificationManager.IMPORTANCE_LOW);

                // Configure the notification channel.
                notificationChannel.setDescription("Channel description");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.setVibrationPattern(new long[]{400});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }


            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mA, NOTIFICATION_CHANNEL_ID);

            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentTitle("Vertretung:")
                    .setOngoing(true);
            notificationBuilder.setContentIntent(resultPendingIntent);

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
