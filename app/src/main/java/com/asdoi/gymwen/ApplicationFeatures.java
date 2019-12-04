package com.asdoi.gymwen;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.asdoi.gymwen.main.ChoiceActivity;
import com.asdoi.gymwen.main.MainActivity;
import com.asdoi.gymwen.main.SignInActivity;
import com.asdoi.gymwen.receivers.ButtonReceiver;
import com.asdoi.gymwen.vertretungsplanInternal.VertretungsPlan;
import com.asdoi.gymwen.widgets.VertretungsplanWidget;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;
import org.acra.annotation.AcraToast;
import org.acra.data.StringFormat;
import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;

@AcraCore(buildConfigClass = BuildConfig.class,
        reportFormat = StringFormat.JSON)
@AcraMailSender(mailTo = "GymWenApp@t-online.de")
@AcraDialog(resText = R.string.acra_dialog_text,
        resCommentPrompt = R.string.acra_dialog_content,
        resTheme = R.style.AppTheme,
        resTitle = R.string.acra_dialog_title)
@AcraToast(resText = R.string.acra_toast)

public class ApplicationFeatures extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        ACRA.init(this);
    }

    public static Context getContext() {
        return mContext;
    }

    //Download Doc
    public static Runnable downloadRunnable(final boolean isWidget, final boolean signIn) {
        return new Runnable() {
            @Override
            public void run() {
                downloadDocs(isWidget, signIn);
            }
        };
    }

    public static void downloadDocs(boolean isWidget, boolean signIn) {

        //DownloadDocs
        if (!VertretungsPlan.areDocsDownloaded() && ApplicationFeatures.isNetworkAvailable()) {
            if (!ApplicationFeatures.initSettings(true, signIn)) {
                return;
            }
            String[] strURL = new String[]{VertretungsPlan.todayURL, VertretungsPlan.tomorrowURL};
            Document[] doc = new Document[strURL.length];
            for (int i = 0; i < 2; i++) {

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

                    VertretungsPlan.lastAuthString = authString + "t";


                } catch (IOException e) {
                    e.printStackTrace();
                    VertretungsPlan.lastAuthString = authString + "f";
                    return;
                }
            }
            VertretungsPlan.setDocs(doc[0], doc[1]);
            if (!isWidget) {
                proofeNotification();
                updateMyWidgets();
            }
        }
    }

    public static class downloadDocsTask extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... params) {
            if (params == null || params.length < 2) {
                if (params.length == 1)
                    params = new Boolean[]{params[0], true};
            }
            downloadDocs(params[0], params[1]);
            return null;
        }
    }

    public static class downloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public downloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            if (!urldisplay.trim().isEmpty()) {
                Bitmap mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
//                Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
                return mIcon11;
            }
            return null;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    //Settings
    public static boolean initSettings(boolean isWidget, boolean signIn) {
        Context context = getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean signedIn = sharedPref.getBoolean("signed", false);

        if (signedIn) {
            boolean oberstufe = sharedPref.getBoolean("oberstufe", true);
            String courses = sharedPref.getString("courses", "");
            if (courses.trim().isEmpty()) {
                Intent i = new Intent(context, ChoiceActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                return signedIn;
            }

            boolean hours = sharedPref.getBoolean("hours", false);

            VertretungsPlan.setup(oberstufe, courses.split("#"), courses, hours);

//            System.out.println("settings: " + oberstufe + courses);

            String username = sharedPref.getString("username", "");
            String password = sharedPref.getString("password", "");

            VertretungsPlan.signin(username, password);


            if (!isWidget) {
                proofeNotification();
                updateMyWidgets();
            }
        } else if (signIn) {
            Intent i = new Intent(context, SignInActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        return signedIn;
    }

    public static void updateMyWidgets() {
        Context context = getContext();
        AppWidgetManager man = AppWidgetManager.getInstance(context);
        int[] ids = man.getAppWidgetIds(new ComponentName(context, VertretungsplanWidget.class));
        Intent updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(VertretungsplanWidget.WIDGET_ID_KEY, ids);
        context.sendBroadcast(updateIntent);
    }

    public static Bitmap vectorToBitmap(@DrawableRes int resVector) {
        Context context = getContext();
        Drawable drawable = AppCompatResources.getDrawable(context, resVector);
        Bitmap b = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        drawable.setBounds(0, 0, c.getWidth(), c.getHeight());
        drawable.draw(c);
        return b;
    }

    //Notification
    public static void proofeNotification() {
        if (PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("showNotification", false)) {
            new ApplicationFeatures.createNotification().execute(true, false);
        }
    }

    public static class createNotification extends downloadDocsTask {

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
            Context context = getContext();

            try {
                // Create an Intent for the activity you want to start
                Intent resultIntent = new Intent(getContext(), MainActivity.class);
                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntentWithParentStack(resultIntent);
                // Get the PendingIntent containing the entire back stack
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


                int NOTIFICATION_ID = 1;

                //Create an Intent for the BroadcastReceiver
                Intent buttonIntent = new Intent(context, ButtonReceiver.class);
                buttonIntent.putExtra("notificationId", NOTIFICATION_ID);
//            PendingIntent btPendingIntent = PendingIntent.getActivity(context,  0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent btPendingIntent = PendingIntent.getBroadcast(context, 0, buttonIntent, 0);


                NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

                //Build notification
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

                notificationBuilder.setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setContentTitle(context.getString(R.string.notif_content_title));


                notificationBuilder.setContentIntent(resultPendingIntent);

                notificationBuilder.setAutoCancel(true);

                if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("alwaysNotification", true)) {
                    notificationBuilder.setOngoing(true);
                    notificationBuilder.addAction(R.drawable.ic_close_black_24dp, getContext().getString(R.string.notif_dismiss), btPendingIntent);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, context.getString(R.string.notification_channel_title), NotificationManager.IMPORTANCE_LOW);

                    // Configure the notification channel.
                    notificationChannel.setDescription(context.getString(R.string.notification_channel_description));
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(ContextCompat.getColor(context, R.color.colorAccent));
//                notificationChannel.enableVibration(true);
                    notificationManager.createNotificationChannel(notificationChannel);
                    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

                    notificationManager.createNotificationChannel(notificationChannel);
                } else {
                    notificationBuilder.setPriority(Notification.PRIORITY_LOW);
                }

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    notificationBuilder.setSmallIcon(R.drawable.ic_stat_assignment_late);
                }

                try {
                    notificationManager.notify(/*notification id*/1, notificationBuilder.build());
                } catch (Exception e) {
                }
            } catch (Exception e) {
                //Known Icon Error
            }
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
                return "";
            }
            if (inhalt.length == 0) {
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
