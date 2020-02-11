package com.asdoi.gymwen;

import android.app.AlarmManager;
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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;

import com.ahmedjazzar.rosetta.LanguageSwitcher;
import com.asdoi.gymwen.lehrerliste.Lehrerliste;
import com.asdoi.gymwen.profiles.Profile;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver;
import com.asdoi.gymwen.ui.activities.AppIntroActivity;
import com.asdoi.gymwen.ui.activities.ChoiceActivity;
import com.asdoi.gymwen.ui.activities.MainActivity;
import com.asdoi.gymwen.ui.activities.SignInActivity;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;
import com.asdoi.gymwen.vertretungsplan.Vertretungsplan;
import com.asdoi.gymwen.widgets.VertretungsplanWidget;
import com.kabouzeid.appthemehelper.ThemeStore;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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


public class ApplicationFeatures extends MultiDexApplication {
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

        // default theme
        if (!ThemeStore.isConfigured(this, 1)) {
            ThemeStore.editTheme(this)
                    .primaryColorRes(R.color.colorPrimary)
                    .accentColorRes(R.color.colorAccent)
                    .commit();
        }

        mContext = this;
        ACRA.init(this);
        initRosetta();
        ProfileManagement.reload();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        LocaleChanger.onConfigurationChanged();
        mContext = this;
    }


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
        if (!Lehrerliste.isDownloaded()) {
            if (ApplicationFeatures.isNetworkAvailable()) {
                Lehrerliste.setDoc(downloadDoc(Lehrerliste.listUrl));
            } else {
                Lehrerliste.reloadDoc();
            }
        }
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

                if (!isWidget) {
                    updateMyWidgets();
                }
            } else {
                ActivityFeatures.reloadVertretungDocs(ApplicationFeatures.getContext());
            }
            sendNotification();
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
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(CONNECTIVITY_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network nw = connectivityManager.getActiveNetwork();
                NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
                return actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);

            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }


    //Settings
    public static boolean initSettings(boolean isWidget, boolean signIn) {
        Context context = getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean signedIn = sharedPref.getBoolean("signed", false);

        if (signedIn) {
            if (!coursesCheck())
                return false;

            String courses = getSelectedProfile().getCourses();

            boolean hours = isHour();

            VertretungsPlanFeatures.setup(hours, courses.split("#"));

            String username = sharedPref.getString("username", "");
            String password = sharedPref.getString("password", "");

            VertretungsPlanFeatures.signin(username, password);


            if (!isWidget) {
//                sendNotification();
                updateMyWidgets();
            }
        } else if (signIn) {
            if (isIntroShown()) {
                Intent i = new Intent(context, SignInActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            } else {
                Intent i = new Intent(context, AppIntroActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
        return signedIn;
    }

    public static boolean coursesCheck() {
        if (ProfileManagement.sizeProfiles() <= 0) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            //Backwards compatibility for versions older 1.1
            String courses = sharedPref.getString("courses", "");
            if (courses.trim().isEmpty()) {
                Intent i = new Intent(getContext(), ChoiceActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(i);
                return false;
            } else {
                String name = getContext().getString(R.string.profile_default_name);
                ProfileManagement.addProfile(new Profile(courses, name));
                ApplicationFeatures.initProfile(0, true);
            }
        }
        return true;
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
        return vectorToBitmap(drawable);
    }

    public static Bitmap vectorToBitmap(Drawable drawable) {
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

    public static boolean getBooleanSettings(String key, boolean defaultValue, Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(key, defaultValue);
    }

    public static boolean getBooleanSettings(String key, boolean defaultValue) {
        return getBooleanSettings(key, defaultValue, getContext());
    }

    public static boolean isHour() {
        return getBooleanSettings("hours", false);
    }

    public static boolean isSections() {
        return getBooleanSettings("show_sections", true);
    }

    public static boolean isAlarmOn(Context context) {
        return getBooleanSettings("alarm", false, context);
    }

    public static void setAlarm(Context context, boolean value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("alarm", value);
        editor.commit();
    }

    public static boolean showWeekDate() {
        return getBooleanSettings("week_dates", false);
    }

    public static boolean isParents() {
        return getBooleanSettings("parents", false);
    }

    public static boolean isTwoNotifications() {
        return getBooleanSettings("two_notifs", false);
    }

    public static boolean isIntroShown() {
        return getBooleanSettings("intro", false);
    }

    public static int[] getAlarmTime() {
        SharedPreferences sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getContext());
        return new int[]{sharedPref.getInt("Alarm_hour", -1), sharedPref.getInt("Alarm_minute", -1), sharedPref.getInt("Alarm_second", -1)};
    }

    public static void setAlarmTime(int... times) {
        if (times.length != 3) {
            if (times.length > 0 && times[0] == 0) {
                setAlarm(getContext(), false);
            } else {
                System.out.println("wrong parameters");
            }
            return;
        }

        SharedPreferences sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        setAlarm(getContext(), true);
        editor.putInt("Alarm_hour", times[0]);
        editor.putInt("Alarm_minute", times[1]);
        editor.putInt("Alarm_second", times[2]);
        editor.commit();

    }

    public static int getIntSettings(String key, int defaultValue) {
        Context context = getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getInt(key, defaultValue);
    }

    @StyleRes
    public static int getGeneralTheme() {
        Context context = getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return getThemeResFromPrefValue(sharedPref.getString("theme", "switch"));
    }

    @StyleRes
    public static int getThemeResFromPrefValue(String themePrefValue) {
        switch (themePrefValue) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return R.style.AppTheme_Dark;
            case "black":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return R.style.AppTheme_Black;
            case "switch":
                int nightModeFlags = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        return R.style.AppTheme_Dark;
                    default:
                    case Configuration.UI_MODE_NIGHT_NO:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        return R.style.AppTheme_Light;
                }
            case "light":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                return R.style.AppTheme_Light;
        }
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
    final public static int NOTIFICATION_ID = 1;
    final public static int NOTIFICATION_ID_2 = 2;
    final private static String NOTIFICATION_CHANNEL_ID = "vertretungsplan_01";

    public static void sendNotification() {
        if (PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("showNotification", false)) {
            new createInfoNotification().execute(true, false);
        }
    }

    public static class createInfoNotification extends downloadVertretungsplanDocsTask {

        @Override
        protected void onPostExecute(Void v) {
            try {
                ProfileManagement.reload();
                if (VertretungsPlanFeatures.getTodayTitle().equals(ApplicationFeatures.getContext().getString(R.string.noInternetConnection))) {
                    return;
                }
                sendNotification();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendNotification() {
            if (isTwoNotifications())
                notificationMessageTwoNotifs();
            else
                notificationMessageOneNotif();
        }

        private void notificationMessageOneNotif() {
            StringBuilder messageToday = new StringBuilder();
            StringBuilder messageTomorrow = new StringBuilder();
            String[] titleTodayArray = VertretungsPlanFeatures.getTodayTitleArray();
            String[] titleTomorrowArray = VertretungsPlanFeatures.getTomorrowTitleArray();
            String titleToday = titleTodayArray[0] + ", " + titleTodayArray[1] + ":";
            String titleTomorrow = titleTomorrowArray[0] + ", " + titleTomorrowArray[1] + ":";
            boolean isMoreThanOneProfile = ProfileManagement.isMoreThanOneProfile();

            boolean[] isNo = new boolean[]{true, true};

            StringBuilder count = new StringBuilder();

            for (int i = 0; i < ProfileManagement.sizeProfiles(); i++) {
                Profile p = ProfileManagement.getProfile(i);
                Vertretungsplan temp = VertretungsPlanFeatures.createTempVertretungsplan(ApplicationFeatures.isHour(), p.getCourses().split("#"));
                String[][] inhalt = temp.getDay(true);
                try {
                    count.append(inhalt.length);
                    count.append("|");
                    if (inhalt.length != 0) {
                        if (isMoreThanOneProfile) {
                            messageToday.append(ProfileManagement.getProfile(i).getName());
                            messageToday.append(":\n");
                        }
                        messageToday.append(notifMessageContent(inhalt, temp));
                        isNo[0] = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                inhalt = temp.getDay(false);
                try {
                    count.append(inhalt.length);
                    count.append(", ");
                    if (inhalt.length != 0) {
                        if (isMoreThanOneProfile) {
                            messageTomorrow.append(ProfileManagement.getProfile(i).getName());
                            messageTomorrow.append(":\n");
                        }
                        messageTomorrow.append(notifMessageContent(inhalt, temp));
                        isNo[1] = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            count.deleteCharAt(count.lastIndexOf(", "));

            messageToday = new StringBuilder(isNo[0] ? getContext().getString(R.string.notif_nothing) + "\n" : messageToday);
            messageTomorrow = new StringBuilder(isNo[1] ? getContext().getString(R.string.notif_nothing) + "\n" : messageTomorrow);

            StringBuilder message = new StringBuilder(titleToday + "\n" + messageToday + titleTomorrow + "\n" + messageTomorrow);
            message.delete(message.length() - 1, message.length());

            createNotification(message.toString(), getContext().getString(R.string.notif_content_title) + " " + count, NOTIFICATION_ID);
        }

        private void notificationMessageTwoNotifs() {
            StringBuilder messageToday = new StringBuilder();
            StringBuilder messageTomorrow = new StringBuilder();
            String[] titleTodayArray = VertretungsPlanFeatures.getTodayTitleArray();
            String[] titleTomorrowArray = VertretungsPlanFeatures.getTomorrowTitleArray();
            String titleToday = titleTodayArray[0] + ", " + titleTodayArray[1] + ":";
            String titleTomorrow = titleTomorrowArray[0] + ", " + titleTomorrowArray[1] + ":";

            boolean isMoreThanOneProfile = ProfileManagement.isMoreThanOneProfile();
            boolean[] isNo = new boolean[]{true, true};

            StringBuilder count1 = new StringBuilder();
            StringBuilder count2 = new StringBuilder();

            for (int i = 0; i < ProfileManagement.sizeProfiles(); i++) {
                Profile p = ProfileManagement.getProfile(i);
                Vertretungsplan temp = VertretungsPlanFeatures.createTempVertretungsplan(ApplicationFeatures.isHour(), p.getCourses().split("#"));
                String[][] inhalt = temp.getDay(true);
                try {
                    count1.append(inhalt.length);
                    count1.append(", ");
                    if (inhalt.length != 0) {
                        if (isMoreThanOneProfile) {
                            messageToday.append(ProfileManagement.getProfile(i).getName());
                            messageToday.append(":\n");
                        }
                        messageToday.append(notifMessageContent(inhalt, temp));
                        isNo[0] = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                inhalt = temp.getDay(false);
                try {
                    count2.append(inhalt.length);
                    count2.append(", ");
                    if (inhalt.length != 0) {
                        if (isMoreThanOneProfile) {
                            messageTomorrow.append(ProfileManagement.getProfile(i).getName());
                            messageTomorrow.append(":\n");
                        }
                        messageTomorrow.append(notifMessageContent(inhalt, temp));
                        isNo[1] = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            count1.deleteCharAt(count1.lastIndexOf(", "));
            count2.deleteCharAt(count2.lastIndexOf(", "));

            String messageTo = isNo[0] ? getContext().getString(R.string.notif_nothing) + "\n" : messageToday.toString();
            String messageTom = isNo[1] ? getContext().getString(R.string.notif_nothing) + "\n" : messageTomorrow.toString();

            messageTo = messageTo.substring(0, messageTo.length() - 1);
            messageTom = messageTom.substring(0, messageTom.length() - 1);
            createNotification(messageTom, titleTomorrow + " " + count2.toString(), NOTIFICATION_ID_2);
            createNotification(messageTo, titleToday + " " + count1.toString(), NOTIFICATION_ID);
        }

        public String notifMessageContent(String[][] inhalt, Vertretungsplan vp) {
            String message = "";
            if (inhalt == null) {
                return "";
            }
            if (inhalt.length == 0) {
                message += getContext().getString(R.string.notif_nothing) + "\n";
            } else {
                if (vp.getOberstufe()) {
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

        private void createNotification(String body, String title, int notification_id) {
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
                buttonIntent.setAction("com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver");
                buttonIntent.putExtra("EXTRA_NOTIFICATION_ID", notification_id);
                PendingIntent btPendingIntent = PendingIntent.getBroadcast(context, 0, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

                //Build notification
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

                notificationBuilder
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setContentTitle(title)
                        .setContentIntent(resultPendingIntent)
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
                    notificationChannel.setSound(null, null);
                    notificationManager.createNotificationChannel(notificationChannel);
                    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

                    notificationManager.createNotificationChannel(notificationChannel);
                } else {
                    notificationBuilder.setPriority(Notification.PRIORITY_LOW);
                }

                notificationManager.notify(notification_id, notificationBuilder.build());

            } catch (Exception e) {
                e.printStackTrace();
                //Known Icon Error
            }
        }
    }

    public static class createTodayWarningNotification extends createInfoNotification {

        @Override
        protected void onPostExecute(Void v) {
            try {
                ProfileManagement.reload();
                if (VertretungsPlanFeatures.getTodayTitle().equals(ApplicationFeatures.getContext().getString(R.string.noInternetConnection))) {
                    return;
                }
                sendNotification();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void sendNotification() {
            notificationMessage();
        }

        private void notificationMessage() {
            StringBuilder messageToday = new StringBuilder();

            String[] titleTodayArray = VertretungsPlanFeatures.getTodayTitleArray();
            String[] titleTomorrowArray = VertretungsPlanFeatures.getTomorrowTitleArray();

            String title = "Vertretung Heute:";

            boolean isMoreThanOneProfile = ProfileManagement.isMoreThanOneProfile();

            boolean nothing = true;
            boolean today = true;

            /*if (isToday(titleTodayArray[0]))
                today = true;
            else if (isToday(titleTomorrowArray[0]))
                today = false;
            else {
                //Today is not online
                return;
            }*/

            StringBuilder count1 = new StringBuilder();

            for (int i = 0; i < ProfileManagement.sizeProfiles(); i++) {
                Profile p = ProfileManagement.getProfile(i);
                Vertretungsplan temp = VertretungsPlanFeatures.createTempVertretungsplan(ApplicationFeatures.isHour(), p.getCourses().split("#"));
                String[][] inhalt = temp.getDay(today);
                try {
                    count1.append(inhalt.length);
                    count1.append(", ");
                    if (inhalt.length != 0) {
                        if (isMoreThanOneProfile) {
                            messageToday.append(ProfileManagement.getProfile(i).getName());
                            messageToday.append(":\n");
                        }
                        messageToday.append(notifMessageContent(inhalt, temp));
                        nothing = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            count1.deleteCharAt(count1.lastIndexOf(", "));

            if (nothing) {
                //Nothing TODO
            } else {
                String messageTo = messageToday.toString();
                messageTo = messageTo.substring(0, messageTo.length() - 1);
                createNotification(messageTo, title + " " + count1.toString(), NOTIFICATION_ID, ApplicationFeatures.getPrimaryColor(getContext()));
            }
        }

        private boolean isToday(String source) {
            try {
                DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

                Date startDate = Vertretungsplan.removeTime(df.parse(source));

                Date currentDate = Vertretungsplan.removeTime(new Date());

                if (currentDate.equals(startDate)) {
                    //If date is today
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }


        private void createNotification(String body, String title, int notification_id, int color) {
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
                buttonIntent.setAction("com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver");
                buttonIntent.putExtra("EXTRA_NOTIFICATION_ID", notification_id);
                PendingIntent btPendingIntent = PendingIntent.getBroadcast(context, 0, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

                //Build notification
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

                notificationBuilder
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setContentTitle(title)
                        .setContentIntent(resultPendingIntent)
                        .setSmallIcon(R.drawable.ic_error_black_24dp)
                        .setLargeIcon(ApplicationFeatures.vectorToBitmap(R.drawable.ic_error_black_24dp))
                        .setColorized(true)
                        .setColor(color);


                if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("alwaysNotification", true)) {
                    notificationBuilder.setOngoing(true);
                    notificationBuilder.addAction(R.drawable.ic_close_black_24dp, getContext().getString(R.string.notif_dismiss), btPendingIntent);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, context.getString(R.string.notification_channel_title), NotificationManager.IMPORTANCE_HIGH);

                    // Configure the notification channel.
                    notificationChannel.setDescription(context.getString(R.string.notification_channel_description));
                    notificationChannel.enableLights(false);
//                    notificationChannel.setLightColor(ContextCompat.getColor(context, R.color.colorAccent));
                    notificationChannel.enableVibration(false);
                    notificationChannel.setSound(null, null);
                    notificationManager.createNotificationChannel(notificationChannel);
                    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

                    notificationManager.createNotificationChannel(notificationChannel);
                } else {
                    notificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
                }

                notificationManager.notify(notification_id, notificationBuilder.build());

            } catch (Exception e) {
                e.printStackTrace();
                //Known Icon Error
            }
        }
    }

    public static int frequencyOfSubString(String str, String findStr) {
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = str.indexOf(findStr, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += findStr.length();
            }
        }
        return count;
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

//        List<Locale> SUPPORTED_LOCALES = new ArrayList<Locale>(supportedLocales);
//
//        LocaleChanger.initialize(getApplicationContext(), SUPPORTED_LOCALES);
    }


    //Schedule and TimePicker
    public static final int DAILY_REMINDER_REQUEST_CODE = 100;

    public static void setAlarm(Context context, Class<?> cls, int hour, int min, int second) {
        // cancel already scheduled reminders
        cancelAlarm(context, cls);

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


    //Profiles
    private static void initProfileGlobal(int position, String courses) {
        Context context = getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("courses", courses);
        editor.putInt("selected", position);
        editor.apply();
    }

    public static void initProfile(int position, boolean global) {
        String courses = ProfileManagement.getProfile(position).getCourses();
        VertretungsPlanFeatures.setup(isHour(), courses.split("#"));
        if (global) initProfileGlobal(position, courses);
    }

    public static Profile getSelectedProfile() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        int n = sharedPref.getInt("selected", 0);
        return ProfileManagement.getProfile(sharedPref.getInt("selected", 0));

    }

    public static void resetSelectedProfile() {
        Context context = getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("selected", 0);
        editor.apply();
    }

    public static int getSelectedProfilePosition() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPref.getInt("selected", 0);
    }


    //Colors
    public static int getTextColorPrimary(Context context) {
        return getThemeColor(android.R.attr.textColorPrimary, context);
    }

    public static int getTextColorSecondary(Context context) {
        return getThemeColor(android.R.attr.textColorSecondary, context);
    }

    public static int getBackgroundColor(Context context) {
        return getThemeColor(android.R.attr.windowBackground, context);
    }

    public static int getWebPageBackgroundColor(Context context) {
        return getThemeColor(R.attr.webPageBackground, context);
    }

    public static int getLinkColor(Context context) {
        return getThemeColor(android.R.attr.textColorLink, context);
    }

    public static int getPrimaryColor(Context context) {
        return getIntSettings("colorPrimary", 0) == 0 ? getThemeColor(R.attr.colorPrimary, context) : getIntSettings("colorPrimary", 0);
    }

    public static int getAccentColor(Context context) {
        return getIntSettings("colorAccent", 0) == 0 ? getThemeColor(R.attr.colorAccent, context) : getIntSettings("colorAccent", 0);
    }

    public static int getThemeColor(int themeAttributeId, Context context) {
        try {
            TypedValue outValue = new TypedValue();
            Resources.Theme theme = context.getTheme();
            boolean wasResolved = theme.resolveAttribute(themeAttributeId, outValue, true);
            if (wasResolved) {
                return ContextCompat.getColor(context, outValue.resourceId);
            } else {
                // fallback colour handling
                return Color.BLACK;
            }
        } catch (Exception e) {
            return Color.BLACK;
        }
    }
}
