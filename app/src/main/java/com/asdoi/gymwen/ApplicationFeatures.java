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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;

import com.ahmedjazzar.rosetta.LanguageSwitcher;
import com.asdoi.gymwen.profiles.Profile;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.receivers.CheckSubstitutionPlanReceiver;
import com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlan;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures;
import com.asdoi.gymwen.teacherlist.Teacherlist;
import com.asdoi.gymwen.ui.activities.AppIntroActivity;
import com.asdoi.gymwen.ui.activities.ChoiceActivity;
import com.asdoi.gymwen.ui.activities.MainActivity;
import com.asdoi.gymwen.ui.activities.SignInActivity;
import com.asdoi.gymwen.util.PreferenceUtil;
import com.asdoi.gymwen.widgets.SubstitutionWidgetProvider;
import com.asdoi.gymwen.widgets.SummaryWidget;
import com.kabouzeid.appthemehelper.ThemeStore;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;
import org.acra.data.StringFormat;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import saschpe.android.customtabs.CustomTabsActivityLifecycleCallbacks;

@AcraCore(buildConfigClass = BuildConfig.class,
        reportFormat = StringFormat.JSON)
@AcraMailSender(mailTo = "GymWenApp@t-online.de")
@AcraDialog(resText = R.string.acra_dialog_text,
        resCommentPrompt = R.string.acra_dialog_content,
        resTheme = R.style.AppTheme_Dark,
        resTitle = R.string.acra_dialog_title)
//@AcraToast(resText = R.string.acra_toast)


public class ApplicationFeatures extends MultiDexApplication {
    public static final int substitution_teacher_view_id = View.generateViewId();
    public static final int old_design_substitution_view_id = View.generateViewId();
    private static Context mContext;
    @Nullable
    public static ArrayList<String> websiteHistorySaveInstance;

    public static int getCurrentTimeInSeconds() {
        Calendar calendar = Calendar.getInstance();
        int time = calendar.get(Calendar.SECOND);
        time += calendar.get(Calendar.MINUTE) * 60;
        time += calendar.get(Calendar.HOUR_OF_DAY) * 3600;
        return time;
    }

    @NonNull
    private static Date removeTime(@NonNull Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Support vector drawable support for pre-Lollipop devices
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

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

        //Setup CheckSubstitutionPlanReceiver
        List<Integer> time = CheckSubstitutionPlanReceiver.Companion.getNextTime();
        ApplicationFeatures.setAlarm(getContext(), CheckSubstitutionPlanReceiver.class, time.get(0), time.get(1), time.get(2));

        // Preload custom tabs service for improved performance
        // This is optional but recommended
        registerActivityLifecycleCallbacks(new CustomTabsActivityLifecycleCallbacks());
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        LocaleChanger.onConfigurationChanged();
        mContext = this;
    }


    public static Context getContext() {
        return mContext;
    }

    //Download Doc
    @Nullable
    private static Document downloadDoc(String url) {
        try {
            return Jsoup.connect(url).get();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteOfflineTeacherlistDoc() {
        Teacherlist.setDoc(null);
    }

    public static void downloadTeacherlistDoc() {
        if (!Teacherlist.isDownloaded()) {
            if (ApplicationFeatures.isNetworkAvailable()) {
                Teacherlist.setDoc(downloadDoc(PreferenceUtil.getTeacherlistURL(getContext())));
            } else if (PreferenceUtil.isOfflineMode()) {
                Teacherlist.reloadDoc();
            }
        }
    }

    public static void deleteOfflineSubstitutionDocs() {
        SubstitutionPlanFeatures.setDocs(null, null);
    }

    //Substitutionplan docs
    public static void downloadSubstitutionplanDocs(boolean isWidget, boolean signIn) {

        //DownloadDocs
        if (!SubstitutionPlanFeatures.areDocsDownloaded()) {
            if (ApplicationFeatures.isNetworkAvailable()) {
                if (!ApplicationFeatures.initSettings(true, signIn)) {
                    return;
                }
                downloadSubstitutionDoc();

                if (!isWidget) {
                    refreshWidgets();
                }
            } else if (PreferenceUtil.isOfflineMode()) {
                SubstitutionPlanFeatures.reloadDocs();
            }
//            sendNotification();
        }
    }

    public static void downloadSubstitutionplanDocsAlways(boolean isWidget, boolean signIn) {
        //DownloadDocs
        if (ApplicationFeatures.isNetworkAvailable()) {
            if (!ApplicationFeatures.initSettings(true, signIn)) {
                return;
            }

            downloadSubstitutionDoc();

            if (!isWidget) {
                refreshWidgets();
            }
        } else if (PreferenceUtil.isOfflineMode()) {
            SubstitutionPlanFeatures.reloadDocs();
        }
//            sendNotification();
    }

    private static void downloadSubstitutionDoc() {
        String[] strURL = new String[]{PreferenceUtil.getTodayURL(getContext()), PreferenceUtil.getTomorrowURL(getContext())};
        Document[] doc = new Document[strURL.length];
        for (int i = 0; i < 2; i++) {

            String authString = SubstitutionPlanFeatures.strUserId + ":" + SubstitutionPlanFeatures.strPasword;

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
        SubstitutionPlanFeatures.setDocs(doc[0], doc[1]);
    }

    public static class DownloadSubstitutionplanDocsTask extends AsyncTask<Boolean, Void, Void> {
        @Nullable
        @Override
        protected Void doInBackground(@Nullable Boolean... params) {
            try {
                if (params == null)
                    params = new Boolean[]{false, false};
                else if (params.length < 2) {
                    if (params.length == 1)
                        params = new Boolean[]{params[0], true};
                    else
                        params = new Boolean[]{false, false};
                }
                downloadSubstitutionplanDocs(params[0], params[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        final ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Nullable
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            if (!urldisplay.trim().isEmpty()) {
                Bitmap mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
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
            //Profile reload has been called in onCreate Application
            if (!coursesCheck(true))
                return false;

            String courses = getSelectedProfile().getCourses();

            boolean hours = PreferenceUtil.isHour();

            SubstitutionPlanFeatures.setup(hours, courses.split(Profile.coursesSeparator));

            String username = PreferenceUtil.getUsername(context);
            String password = PreferenceUtil.getPassword(context);

            SubstitutionPlanFeatures.signin(username, password);


            if (!isWidget) {
                refreshWidgets();
            }
        } else if (signIn) {
            if (PreferenceUtil.isIntroShown()) {
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

    public static boolean coursesCheck(boolean openAddActivity) {
        if (ProfileManagement.getSize() <= 0) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            //Backwards compatibility for versions older 1.1
            String courses = sharedPref.getString("courses", "");
            if (courses.trim().isEmpty() && openAddActivity) {
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

    public static boolean coursesCheck() {
        if (ProfileManagement.getSize() <= 0) {
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


    private static void refreshWidgets() {
        Context context = getContext();
        AppWidgetManager man = AppWidgetManager.getInstance(context);

        //SubstitutionWidget
        int[] ids = man.getAppWidgetIds(new ComponentName(context, SubstitutionWidgetProvider.class));
        Intent updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(SubstitutionWidgetProvider.WIDGET_ID_KEY, ids);
        context.sendBroadcast(updateIntent);

        //SummaryWidget
        ids = man.getAppWidgetIds(new ComponentName(context, SummaryWidget.class));
        updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(SummaryWidget.SUMMARY_WIDGET_ID_KEY, ids);
        context.sendBroadcast(updateIntent);
    }

    public static Bitmap vectorToBitmap(@DrawableRes int resVector) {
        Context context = getContext();
        Drawable drawable = AppCompatResources.getDrawable(context, resVector);
        return vectorToBitmap(drawable);
    }

    private static Bitmap vectorToBitmap(@NonNull Drawable drawable) {
        Bitmap b = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        drawable.setBounds(0, 0, c.getWidth(), c.getHeight());
        drawable.draw(c);
        return b;
    }

    public static boolean getBooleanSettings(String key, boolean defaultValue) {
        return PreferenceUtil.getBooleanSettings(key, defaultValue, getContext());
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

    @NonNull
    public static String urlToRightFormat(@NonNull String url) {
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
    final public static int NOTIFICATION_INFO_ID = 1;
    final public static int NOTIFICATION_INFO_ID_2 = 2;
    final public static int NOTIFICATION_MAIN_ID = 3;
    final public static String NOTIFICATION_CHANNEL_ID = "Substitutionplan_01";

    public static void sendNotification() {
        sendSummaryNotif();
    }

    //Check if sth has changed -> Send Notification
    public static void checkSubstitutionPlan() {
        if (SubstitutionPlanFeatures.isUninit())
            SubstitutionPlanFeatures.reloadDocs();
        Document[] oldDocs = SubstitutionPlanFeatures.getDocs();

        downloadSubstitutionplanDocs(false, false);
        if (ProfileManagement.isUninit())
            ProfileManagement.reload();
        if (!coursesCheck(false))
            return;
        if (SubstitutionPlanFeatures.getTodayTitle().equals(ApplicationFeatures.getContext().getString(R.string.noInternetConnection))) {
            //No Internet
            return;
        }

        Document[] newDocs = SubstitutionPlanFeatures.getDocs();

        Profile preferredProfile = ProfileManagement.getPreferredProfile();
        if (preferredProfile != null) {
            int whichDocIsToday = -1;

            int titleCodeToday = SubstitutionPlanFeatures.getTodayTitleCode();
            int titleCodeTomorrow = SubstitutionPlanFeatures.getTomorrowTitleCode();

            if (SubstitutionPlanFeatures.isTitleCodeToday(titleCodeToday))
                whichDocIsToday = 0;
            else if (SubstitutionPlanFeatures.isTitleCodeToday(titleCodeTomorrow))
                whichDocIsToday = 1;

            if (whichDocIsToday >= 0) {
                SubstitutionPlan temp = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), preferredProfile.getCoursesArray());
                if (temp.hasSthChanged(oldDocs[whichDocIsToday], newDocs[whichDocIsToday])) {
                    //Send Main Notif only if day sth has changed today for the preferred profile, else -> summaryNotif
                    temp.setTodayDoc(newDocs[whichDocIsToday]);
                    sendMainNotif(temp.getTitleString(true), temp.getDay(true));
                }
            }
        }

        for (int i = 0; i < ProfileManagement.getSize(); i++) {
            Profile p = ProfileManagement.getProfile(i);
            SubstitutionPlan temp = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), p.getCoursesArray());

            if (temp.hasSthChanged(oldDocs, newDocs)) {
                //Sth has changed since last download of substitutionplan
                sendSummaryNotif();
                break;
            }
        }
    }

    private static void sendMainNotif() {
        if (ProfileManagement.isUninit())
            ProfileManagement.reload();
        Profile preferredProfile = ProfileManagement.getPreferredProfile();
        if (preferredProfile != null) {
            int whichDayIsToday = -1;

            int titleCodeToday = SubstitutionPlanFeatures.getTodayTitleCode();
            int titleCodeTomorrow = SubstitutionPlanFeatures.getTomorrowTitleCode();

            if (SubstitutionPlanFeatures.isTitleCodeToday(titleCodeToday))
                whichDayIsToday = 0;
            else if (SubstitutionPlanFeatures.isTitleCodeToday(titleCodeTomorrow))
                whichDayIsToday = 1;

            SubstitutionPlan temp = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), preferredProfile.getCoursesArray());

            switch (whichDayIsToday) {
                case 0:
                    sendMainNotif(SubstitutionPlanFeatures.getTodayTitle(), temp.getDay(true));
                    break;
                case 1:
                    sendMainNotif(SubstitutionPlanFeatures.getTomorrowTitle(), temp.getDay(false));
                    break;
            }
        }
    }

    private static void sendMainNotif(String title, String[][] content) {
        new ApplicationFeaturesUtils.Companion.CreateMainNotification(title, content).execute(true, false);
    }

    public static void sendSummaryNotif() {
//        if (PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("showNotification", false)) {
//            new CreateInfoNotification().execute(true, false);
//        }
        sendMainNotif("title", new String[][]{});
    }

    private static class CreateInfoNotification extends DownloadSubstitutionplanDocsTask {

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            try {
                if (ProfileManagement.isUninit())
                    ProfileManagement.reload();
                if (!coursesCheck(false))
                    return;
                if (SubstitutionPlanFeatures.getTodayTitle().equals(ApplicationFeatures.getContext().getString(R.string.noInternetConnection))) {
                    return;
                }
                sendNotification();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendNotification() {
            if (PreferenceUtil.isTwoNotifications())
                notificationMessageTwoNotifs();
            else
                notificationMessageOneNotif();
        }

        private void notificationMessageOneNotif() {
            StringBuilder messageToday = new StringBuilder();
            StringBuilder messageTomorrow = new StringBuilder();
            String[] titleTodayArray = SubstitutionPlanFeatures.getTodayTitleArray();
            String[] titleTomorrowArray = SubstitutionPlanFeatures.getTomorrowTitleArray();
            String titleToday = titleTodayArray[0] + ", " + titleTodayArray[1] + ":";
            String titleTomorrow = titleTomorrowArray[0] + ", " + titleTomorrowArray[1] + ":";
            boolean isMoreThanOneProfile = ProfileManagement.isMoreThanOneProfile();

            boolean[] isNo = new boolean[]{true, true};

            StringBuilder count = new StringBuilder();

            for (int i = 0; i < ProfileManagement.getSize(); i++) {
                Profile p = ProfileManagement.getProfile(i);
                SubstitutionPlan temp = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), p.getCoursesArray());
                String[][] content = temp.getDay(true);
                try {
                    count.append(content.length);
                    count.append("|");
                    if (content.length != 0) {
                        if (isMoreThanOneProfile) {
                            messageToday.append(ProfileManagement.getProfile(i).getName());
                            messageToday.append(":\n");
                        }
                        messageToday.append(notifMessageContent(content, temp));
                        isNo[0] = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                content = temp.getDay(false);
                try {
                    count.append(content.length);
                    count.append(", ");
                    if (content.length != 0) {
                        if (isMoreThanOneProfile) {
                            messageTomorrow.append(ProfileManagement.getProfile(i).getName());
                            messageTomorrow.append(":\n");
                        }
                        messageTomorrow.append(notifMessageContent(content, temp));
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

            createNotification(message.toString(), getContext().getString(R.string.notif_content_title) + " " + count, NOTIFICATION_INFO_ID);
        }

        private void notificationMessageTwoNotifs() {
            StringBuilder messageToday = new StringBuilder();
            StringBuilder messageTomorrow = new StringBuilder();
            String[] titleTodayArray = SubstitutionPlanFeatures.getTodayTitleArray();
            String[] titleTomorrowArray = SubstitutionPlanFeatures.getTomorrowTitleArray();
            String titleToday = titleTodayArray[0] + ", " + titleTodayArray[1] + ":";
            String titleTomorrow = titleTomorrowArray[0] + ", " + titleTomorrowArray[1] + ":";

            boolean isMoreThanOneProfile = ProfileManagement.isMoreThanOneProfile();
            boolean[] isNo = new boolean[]{true, true};

            StringBuilder count1 = new StringBuilder();
            StringBuilder count2 = new StringBuilder();

            for (int i = 0; i < ProfileManagement.getSize(); i++) {
                Profile p = ProfileManagement.getProfile(i);
                SubstitutionPlan temp = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), p.getCoursesArray());
                String[][] content = temp.getDay(true);
                try {
                    count1.append(content.length);
                    count1.append(", ");
                    if (content.length != 0) {
                        if (isMoreThanOneProfile) {
                            messageToday.append(ProfileManagement.getProfile(i).getName());
                            messageToday.append(":\n");
                        }
                        messageToday.append(notifMessageContent(content, temp));
                        isNo[0] = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                content = temp.getDay(false);
                try {
                    count2.append(content.length);
                    count2.append(", ");
                    if (content.length != 0) {
                        if (isMoreThanOneProfile) {
                            messageTomorrow.append(ProfileManagement.getProfile(i).getName());
                            messageTomorrow.append(":\n");
                        }
                        messageTomorrow.append(notifMessageContent(content, temp));
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
            createNotification(messageTom, titleTomorrow + " " + count2.toString(), NOTIFICATION_INFO_ID_2);
            createNotification(messageTo, titleToday + " " + count1.toString(), NOTIFICATION_INFO_ID);
        }

        @NonNull
        String notifMessageContent(@Nullable String[][] content, @NonNull SubstitutionPlan vp) {
            StringBuilder message = new StringBuilder();
            if (content == null) {
                return "";
            }
            if (content.length == 0) {
                message.append(getContext().getString(R.string.notif_nothing)).append("\n");
            } else {
                if (vp.getSenior()) {
                    for (String[] line : content) {
                        if (SubstitutionPlanFeatures.isNothing(line[3])) {
                            message.append(line[1]).append(". Stunde entfällt\n");
                        } else {
                            message.append(line[1]).append(". Stunde, ").append(line[0]).append(", ").append(line[4]).append(", ").append(line[3]).append(" ").append(line[5]).append("\n");
                        }
                    }
                } else {
                    for (String[] line : content) {
                        if (SubstitutionPlanFeatures.isNothing(line[3])) {
                            message.append(line[1]).append(". Stunde entfällt\n");
                        } else {
                            message.append(line[1]).append(". Stunde ").append(line[2]).append(" bei ").append(line[3]).append(", ").append(line[4]).append(" ").append(line[5]).append("\n");
                        }
                    }
                }
            }
            return message.toString();
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


    //Others
    public static int frequencyOfSubString(@NonNull String str, @NonNull String findStr) {
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
    public static void setAlarm(@NonNull Context context, @NonNull Class<?> cls, int hour, int min, int second) {
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
    }

    public static void cancelAlarm(@NonNull Context context, @NonNull Class<?> cls) {
        // Disable a receiver
        ComponentName receiver = new ComponentName(context, cls);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        Intent intent1 = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }


    //Profiles
    public static void initProfile(int position, boolean global) {
        String courses = ProfileManagement.getProfile(position).getCourses();
        SubstitutionPlanFeatures.setup(PreferenceUtil.isHour(), courses.split(Profile.coursesSeparator));
        if (global)
            initProfileGlobal(position, courses);
    }

    private static void initProfileGlobal(int position, String courses) {
        Context context = getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("courses", courses);
        editor.putInt("selected", position);
        editor.apply();
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

    public static boolean addCourseToSelectedProfile(@NonNull String course) {
        return addCourseToProfile(getSelectedProfilePosition(), course);
    }

    private static boolean addCourseToProfile(int position, @NonNull String course) {
        return ProfileManagement.addCourseToProfile(position, course);
    }

    public static boolean removeFromSelectedProfile(@NonNull String course) {
        return removeFromProfile(getSelectedProfilePosition(), course);
    }

    private static boolean removeFromProfile(int position, String course) {
        return ProfileManagement.removeFromProfile(position, course);
    }


    //Colors
    public static int getTextColorPrimary(@NonNull Context context) {
        return getThemeColor(android.R.attr.textColorPrimary, context);
    }

    public static int getTextColorSecondary(@NonNull Context context) {
        return getThemeColor(android.R.attr.textColorSecondary, context);
    }

    public static int getBackgroundColor(@NonNull Context context) {
        return getThemeColor(android.R.attr.windowBackground, context);
    }

    public static int getWebPageBackgroundColor(@NonNull Context context) {
        return getThemeColor(R.attr.webPageBackground, context);
    }

    public static int getLinkColor(@NonNull Context context) {
        return getThemeColor(android.R.attr.textColorLink, context);
    }

    public static int getPrimaryColor(@NonNull Context context) {
        return PreferenceUtil.getIntSettings("colorPrimary", 0) == 0 ? getThemeColor(R.attr.colorPrimary, context) : PreferenceUtil.getIntSettings("colorPrimary", 0);
    }

    public static int getAccentColor(@NonNull Context context) {
        return PreferenceUtil.getIntSettings("colorAccent", 0) == 0 ? getThemeColor(R.attr.colorAccent, context) : PreferenceUtil.getIntSettings("colorAccent", 0);
    }

    private static int getThemeColor(int themeAttributeId, @NonNull Context context) {
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


    //Backup, Export, Import
    public static void exportBackup(@NonNull Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write("test");
            outputStreamWriter.close();
        } catch (IOException e) {
            //"Exception", "File write failed: " + e.toString());
            //Error
        }
    }
}
