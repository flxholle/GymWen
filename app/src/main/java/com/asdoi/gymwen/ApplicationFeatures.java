package com.asdoi.gymwen;

import android.app.AlarmManager;
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
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.ahmedjazzar.rosetta.LanguageSwitcher;
import com.asdoi.gymwen.lehrerliste.Lehrerliste;
import com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver;
import com.asdoi.gymwen.ui.main.activities.ChoiceActivity;
import com.asdoi.gymwen.ui.main.activities.MainActivity;
import com.asdoi.gymwen.ui.main.activities.SignInActivity;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;

@AcraCore(buildConfigClass = BuildConfig.class,
        reportFormat = StringFormat.JSON)
@AcraMailSender(mailTo = "GymWenApp@t-online.de")
@AcraDialog(resText = R.string.acra_dialog_text,
        resCommentPrompt = R.string.acra_dialog_content,
        resTheme = R.style.AppTheme,
        resTitle = R.string.acra_dialog_title)
@AcraToast(resText = R.string.acra_toast)


public class ApplicationFeatures extends Application {
    public static final int vertretung_teacher_view_id = View.generateViewId();
    public static final int old_design_vertretung_view_id = View.generateViewId();
    private static Context mContext;
    public static ArrayList<String> websiteHistorySaveInstance;

    public static int getCurrentTimeInSeconds() {
        Calendar calendar = Calendar.getInstance();
        int time = calendar.get(Calendar.SECOND);
        time += calendar.get(Calendar.MINUTE) * 60;
        time += calendar.get(Calendar.HOUR_OF_DAY) * 3600;
        return time;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        ACRA.init(this);
        initRosetta();
    }

//    @Override
//    public void onTerminate() {
//        VertretungsPlanFeatures.saveDoc();
//        super.onTerminate();
//    }


    public static Context getContext() {
        return mContext;
    }

    //Download Doc
    public static Document downloadDoc(String url) {
        try {
            return Jsoup.connect(url).get();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void downloadLehrerDoc() {
        if (!Lehrerliste.isDownloaded() && ApplicationFeatures.isNetworkAvailable() && Lehrerliste.reloadDoc())
            Lehrerliste.setDoc(downloadDoc(Lehrerliste.listUrl));
    }

    public static void downloadVertretungsplanDocs(boolean isWidget, boolean signIn) {

        //DownloadDocs
        if (!VertretungsPlanFeatures.areDocsDownloaded()) {
            if (ApplicationFeatures.isNetworkAvailable()) {
                if (!ApplicationFeatures.initSettings(true, signIn)) {
                    return;
                }
                String[] strURL = new String[]{VertretungsPlanFeatures.todayURL, VertretungsPlanFeatures.tomorrowURL};
                Document[] doc = new Document[strURL.length];
                for (int i = 0; i < 2; i++) {

                    String authString = VertretungsPlanFeatures.strUserId + ":" + VertretungsPlanFeatures.strPasword;

                    String encodedString =
                            new String(Base64.encodeBase64(authString.getBytes()));

                    try {
                        doc[i] = Jsoup.connect(strURL[i])
                                .header("Authorization", "Basic " + encodedString)
                                .get();

                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                VertretungsPlanFeatures.setDocs(doc[0], doc[1]);
                sendNotification();
                if (!isWidget) {
                    updateMyWidgets();
                }
            } else {
                ActivityFeatures.reloadDocs();
            }
        }
    }


    public static class downloadVertretungsplanDocsTask extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... params) {
            if (params == null || params.length < 2) {
                if (params.length == 1)
                    params = new Boolean[]{params[0], true};
            }
            downloadVertretungsplanDocs(params[0], params[1]);
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
                return false;
            }

            boolean hours = isHour();

            VertretungsPlanFeatures.setup(hours, courses.split("#"));

//            System.out.println("settings: " + oberstufe + courses);

            String username = sharedPref.getString("username", "");
            String password = sharedPref.getString("password", "");

            VertretungsPlanFeatures.signin(username, password);

            sendNotification();

            if (!isWidget) {
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

    public static boolean isBetaEnabled() {
        return getBooleanSettings("beta_features", false);
    }

    public static boolean isOld() {
        return getBooleanSettings("old_vertretung", false);
    }

    public static boolean getBooleanSettings(String key, boolean defaultValue) {
        Context context = getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(key, defaultValue);
    }

    public static boolean isHour() {
        return getBooleanSettings("hours", false);
    }

    public static boolean isSections() {
        return getBooleanSettings("show_sections", true);
    }

    public static boolean isAlarmOn() {
        return getBooleanSettings("alarm", false);
    }

    public static boolean showWeekDate() {
        return getBooleanSettings("week_dates", false);
    }

    public static int[] getAlarmTime() {
        SharedPreferences sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getContext());
        return new int[]{sharedPref.getInt("Alarm_hour", -1), sharedPref.getInt("Alarm_minute", -1), sharedPref.getInt("Alarm_second", -1)};
    }

    public static void setAlarmTime(int... times) {
        if (times.length != 3) {
            System.out.println("wrong parameters");
            return;
        }

        SharedPreferences sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("Alarm_hour", times[0]);
        editor.putInt("Alarm_minute", times[1]);
        editor.putInt("Alarm_second", times[2]);
        editor.apply();

    }


    //Website
    public static boolean isURLValid(String url) {
        boolean isValid = true;
        try {
            URL u = new URL(url); // this would check for the protocol
            u.toURI(); // does the extra checking required for validation of URI
        } catch (Exception e) {
            isValid = false;
        }
        return isValid;
    }

    public static String urlToRightFormat(String url) {
        //Set URL to right format
        if (!url.substring(0, 3).equals("www") && !url.substring(0, 4).equals("http")) {
            url = "http://www." + url;
        }
        if (url.substring(0, 3).equals("www")) {
            url = "http://" + url;
        }
        if (!url.contains("http://www.")) {
            url = "http://www." + url.substring("http://".length());
        }
        if (url.charAt(url.length() - 1) != '/')
            url += "/";
        return url;
    }


    //Notification
    final private static int NOTIFICATION_ID = 1;
    final private static String NOTIFICATION_CHANNEL_ID = "vertretungsplan_01";

    public static void sendNotification() {
        if (PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("showNotification", false)) {
            new ApplicationFeatures.createNotification().execute(true, false);
        }
    }

    public static class createNotification extends downloadVertretungsplanDocsTask {
        int count1 = 0;
        int count2 = 0;

        @Override
        protected void onPostExecute(Void v) {
            if (VertretungsPlanFeatures.getTodayArray() == null) {
                return;
            }
            sendNotification();
        }

        public void sendNotification() {
            //Spanned spanBody = Html.fromHtml(notificationMessage());
            String body = notificationMessage();
            createNotification(body);

        }

        private void createNotification(String body) {
            Context context = getContext();
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
                return;

            try {
                // Create an Intent for the activity you want to start
                Intent resultIntent = new Intent(getContext(), MainActivity.class);
                // Create the TaskStackBuilder and add the intent, which inflates the backgroundShape stack
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntentWithParentStack(resultIntent);
                // Get the PendingIntent containing the entire backgroundShape stack
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


                //Create an Intent for the BroadcastReceiver
                Intent buttonIntent = new Intent(context, NotificationDismissButtonReceiver.class);
                buttonIntent.putExtra("notificationId", NOTIFICATION_ID);
//              PendingIntent btPendingIntent = PendingIntent.getActivity(context,  0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent btPendingIntent = PendingIntent.getBroadcast(context, 0, buttonIntent, 0);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

                //Build notification
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

                notificationBuilder
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setContentTitle(context.getString(R.string.notif_content_title) + " " + count1 + "x" + ", " + count2 + "x")
                        .setContentIntent(resultPendingIntent)
//                        .setLargeIcon(getBitmapFromVectorDrawable(R.drawable.ic_stat_assignment_late))
                        .setSmallIcon(R.drawable.ic_stat_assignment_late);

                if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("alwaysNotification", true)) {
                    notificationBuilder.setOngoing(true);
                    notificationBuilder.addAction(R.drawable.ic_close_black_24dp, getContext().getString(R.string.notif_dismiss), btPendingIntent);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, context.getString(R.string.notification_channel_title), NotificationManager.IMPORTANCE_LOW);

                    // Configure the notification channel.
                    notificationChannel.setDescription(context.getString(R.string.notification_channel_description));
                    notificationChannel.enableLights(false);
//                    notificationChannel.setLightColor(ContextCompat.getColor(context, R.color.colorAccent));
                    notificationChannel.enableVibration(false);
                    notificationManager.createNotificationChannel(notificationChannel);
                    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

                    notificationManager.createNotificationChannel(notificationChannel);
                } else {
                    notificationBuilder.setPriority(Notification.PRIORITY_LOW);
                }

                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

            } catch (Exception e) {
                e.printStackTrace();
                //Known Icon Error
            }
        }

        private String notificationMessage() {
            String message = "";
            String day = VertretungsPlanFeatures.getTodayTitle();
            String[][] inhalt = VertretungsPlanFeatures.getTodayArray();
            try {
                count1 = inhalt.length;
            } catch (Exception e) {
                e.printStackTrace();
            }

            message += notifMessageContent(inhalt, day);

            day = VertretungsPlanFeatures.getTomorrowTitle();
            inhalt = VertretungsPlanFeatures.getTomorrowArray();
            try {
                count2 = inhalt.length;
            } catch (Exception e) {
                e.printStackTrace();
            }

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
                if (VertretungsPlanFeatures.getOberstufe()) {
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

    public static Bitmap getBitmapFromVectorDrawable(int drawableId) {
        Context context = ApplicationFeatures.getContext();
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    //Localization
    private static LanguageSwitcher languageSwitcher;

    public static LanguageSwitcher getLanguageSwitcher() {
        return languageSwitcher;
    }

    private void initRosetta() {
        // This is the locale that you wanna your app to launch with.
        Locale displayLang = Locale.getDefault();

        // You can use a HashSet<String> instead and call 'setSupportedStringLocales()' :)
        HashSet<Locale> supportedLocales = new HashSet<>();
        supportedLocales.add(Locale.GERMAN);
        supportedLocales.add(Locale.ENGLISH);

        boolean match = false;
        for (Locale l : supportedLocales) {
            if (displayLang.getDisplayLanguage().contains(l.getDisplayLanguage())) {
                match = true;
                break;
            }
        }

        if (!match)
            displayLang = Locale.ENGLISH;

        languageSwitcher = new LanguageSwitcher(this, displayLang);
        languageSwitcher.setSupportedLocales(supportedLocales);
    }


    //Schedule and TimePicker
    public static final int DAILY_REMINDER_REQUEST_CODE = 100;

    public static void setAlarm(Context context, Class<?> cls, int hour, int min, int second) {
        // cancel already scheduled reminders
        cancelAlarm(context, cls);

        if (!isAlarmOn()) {
            return;
        }

        Calendar currentCalendar = Calendar.getInstance();

        Calendar customCalendar = Calendar.getInstance();
        customCalendar.set(Calendar.HOUR_OF_DAY, hour);
        customCalendar.set(Calendar.MINUTE, min);
        customCalendar.set(Calendar.SECOND, second);

        if (customCalendar.before(currentCalendar))
            customCalendar.add(Calendar.DATE, 1);

        // Enable a receiver
        ComponentName receiver = new ComponentName(context, cls);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);


        Intent intent1 = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ApplicationFeatures.getContext(), 0, intent1, 0);


        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, customCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

//        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis() + 1000, pendingIntent);

        /*if (Build.VERSION.SDK_INT >= 24) {
            AlarmManager.OnAlarmListener s = () -> {
                ApplicationFeatures.sendNotification();
            };
            am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis() + 1000, "sd", s);
        }*/


    }

    public static void cancelAlarm(Context context, Class<?> cls) {
        // Disable a receiver
        ComponentName receiver = new ComponentName(context, cls);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        Intent intent1 = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, DAILY_REMINDER_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }
}
