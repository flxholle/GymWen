package com.asdoi.gymwen;

import android.app.AlarmManager;
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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;

import com.ahmedjazzar.rosetta.LanguageSwitcher;
import com.asdoi.gymwen.profiles.Profile;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.receivers.CheckSubstitutionPlanReceiver;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlan;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures;
import com.asdoi.gymwen.teacherlist.TeacherlistFeatures;
import com.asdoi.gymwen.ui.activities.AppIntroActivity;
import com.asdoi.gymwen.ui.activities.ChoiceActivity;
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
        initSubstitutionPlanReceiver();

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

    public static void initSubstitutionPlanReceiver() {
        //Setup CheckSubstitutionPlanReceiver
        List<Integer> time = CheckSubstitutionPlanReceiver.Companion.getNextTime();
        ApplicationFeatures.setAlarm(getContext(), CheckSubstitutionPlanReceiver.class, time.get(0), time.get(1), time.get(2), CheckSubstitutionPlanReceiver.CheckSubstitutionPlanReceiverID);
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
        TeacherlistFeatures.setDoc(null);
    }

    public static void downloadTeacherlistDoc() {
        if (!TeacherlistFeatures.isDownloaded()) {
            if (ApplicationFeatures.isNetworkAvailable()) {
                TeacherlistFeatures.setDoc(downloadDoc(PreferenceUtil.getTeacherlistURL(getContext())));
            } else if (PreferenceUtil.isOfflineMode()) {
                TeacherlistFeatures.reloadDoc();
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

    /**
     * @param alert show Notification with sound or without sound
     *              Check if sth has changed -> Send Notification
     *              WatchOut: Network on Main Thread -> Always run this method inside a non-main thread
     */
    public static void checkSubstitutionPlan(boolean alert) {
        if (SubstitutionPlanFeatures.isUninit())
            SubstitutionPlanFeatures.reloadDocs();
        Document[] oldDocs = SubstitutionPlanFeatures.getDocs();

        downloadSubstitutionplanDocs(false, false);
        if (ProfileManagement.isUninit())
            ProfileManagement.reload();
        if (!coursesCheck(false))
            return;
        if (SubstitutionPlanFeatures.getTodayTitleString().equals(ApplicationFeatures.getContext().getString(R.string.noInternetConnection))) {
            //No Internet
            return;
        }

        Document[] newDocs = SubstitutionPlanFeatures.getDocs();

        boolean alertForAllProfiles = PreferenceUtil.isMainNotifForAllProfiles();

        if (!alertForAllProfiles) {
            Profile preferredProfile = ProfileManagement.getPreferredProfile();
            if (preferredProfile != null) {
                int whichDocIsToday = -1;

                if (SubstitutionPlanFeatures.getTodayTitle().isTitleCodeToday())
                    whichDocIsToday = 0;
                else if (SubstitutionPlanFeatures.getTomorrowTitle().isTitleCodeToday())
                    whichDocIsToday = 1;

                if (whichDocIsToday >= 0) {
                    SubstitutionPlan temp = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), preferredProfile.getCoursesArray());
                    if (temp.hasSthChanged(oldDocs[whichDocIsToday], newDocs[whichDocIsToday])) {
                        //Send Main Notif only if day sth has changed today for the preferred profile, else -> summaryNotif
                        temp.setTodayDoc(newDocs[whichDocIsToday]);
                        sendNotifications(alert);
                    }
                }
            }
        }

        for (int i = 0; i < ProfileManagement.getSize(); i++) {
            Profile p = ProfileManagement.getProfile(i);
            SubstitutionPlan temp = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), p.getCoursesArray());

            if (temp.hasSthChanged(oldDocs, newDocs)) {
                //Sth has changed since last download of substitutionplan
                sendNotifications(alertForAllProfiles);
                break;
            }
        }
    }

    //Send notifications
    public static void sendNotifications() {
        sendNotifications(false);
    }

    public static void sendNotifications(boolean alert) {
        if (PreferenceUtil.isNotification())
            new NotificationUtils.Companion.CreateNotification(alert).execute();
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
    public static void setAlarm(@NonNull Context context, @NonNull Class<?> cls, int hour, int min, int second, int id) {
        // cancel already scheduled reminders
        cancelAlarm(context, cls, id);

        Calendar currentCalendar = Calendar.getInstance();

        Calendar customCalendar = Calendar.getInstance();
        customCalendar.setTimeInMillis(System.currentTimeMillis());
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


        Intent intent = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        long startTime = customCalendar.getTimeInMillis();
        if (Build.VERSION.SDK_INT < 23) {
            if (Build.VERSION.SDK_INT >= 19) {
                if (System.currentTimeMillis() < startTime)
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
            } else {
                if (System.currentTimeMillis() < startTime)
                    alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
            }
        } else {
            if (System.currentTimeMillis() < startTime)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
        }
    }

    public static void cancelAlarm(@NonNull Context context, @NonNull Class<?> cls, int id) {
        Intent intent = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
        if (addCourseToProfile(getSelectedProfilePosition(), course)) {
            PreferenceUtil.updateCourses();
            return true;
        }
        return false;
    }

    private static boolean addCourseToProfile(int position, @NonNull String course) {
        return ProfileManagement.addCourseToProfile(position, course);
    }

    public static boolean removeFromSelectedProfile(@NonNull String course) {
        if (removeFromProfile(getSelectedProfilePosition(), course)) {
            PreferenceUtil.updateCourses();
            return true;
        }
        return false;
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
