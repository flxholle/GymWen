package com.asdoi.gymwen.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;

public class PreferenceUtil {

    //Booleans
    public static boolean getBooleanSettings(String key, boolean defaultValue, Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(key, defaultValue);
    }

    public static boolean getBooleanSettings(String key, boolean defaultValue) {
        return ApplicationFeatures.getBooleanSettings(key, defaultValue);
    }

    public static boolean isBetaEnabled() {
        return ApplicationFeatures.getBooleanSettings("beta_features", false);
    }

    public static boolean isHour() {
        return ApplicationFeatures.getBooleanSettings("hours", false);
    }

    public static boolean isAlarmOn(Context context) {
        return getBooleanSettings("alarm", false, context);
    }

    public static boolean isFullTeacherNames() {
        return ApplicationFeatures.getBooleanSettings("show_full_names", false);
    }

    public static boolean isFullTeacherNamesSpecific() {
        return ApplicationFeatures.getBooleanSettings("show_full_names_specific", true);
    }

    public static void setAlarm(Context context, boolean value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("alarm", value);
        editor.commit();
    }

    public static boolean showWeekDate() {
        return ApplicationFeatures.getBooleanSettings("week_dates", false);
    }

    public static boolean isParents() {
        return ApplicationFeatures.getBooleanSettings("parents", false);
    }

    public static boolean isTwoNotifications() {
        return ApplicationFeatures.getBooleanSettings("two_notifs", false);
    }

    public static boolean isIntroShown() {
        return ApplicationFeatures.getBooleanSettings("intro", false);
    }

    public static boolean isPhoneRegistered() {
        return ApplicationFeatures.getBooleanSettings("registered", false);
    }

    public static boolean isFilteredUnfiltered() {
        return ApplicationFeatures.getBooleanSettings("show_sections", false);
    }

    public static boolean isMenuDays() {
        return ApplicationFeatures.getBooleanSettings("show_days", true);
    }

    public static boolean isBothMenu() {
        return getBooleanSettings("menu_both", true);
    }

    public static boolean isFilteredMenu() {
        return getBooleanSettings("menu_filtered", true);
    }

    public static boolean isUnfilteredMenu() {
        return getBooleanSettings("menu_unfiltered", true);
    }

    public static boolean isOfficeMenu() {
        return getBooleanSettings("menu_call_office", true);
    }

    public static boolean isTransportMenu() {
        return getBooleanSettings("menu_public_transport", true);
    }

    public static boolean isNotesMenu() {
        return getBooleanSettings("menu_notes", true);
    }

    public static boolean isTimetableMenu() {
        return getBooleanSettings("menu_timetable", true);
    }

    public static boolean isNews() {
        return getBooleanSettings("menu_news", true);
    }

    public static boolean isShop() {
        return getBooleanSettings("menu_shop", false);
    }

    public static boolean isNavigation() {
        return getBooleanSettings("menu_navigation", true);
    }

    public static boolean isDark() {
        int theme = getGeneralTheme();
        switch (theme) {
            case R.style.AppTheme_Dark:
            case R.style.AppTheme_Black:
                return true;
            case R.style.AppTheme_Light:
            default:
                return false;
        }
    }

    public static boolean isOldDesign() {
        return ApplicationFeatures.getBooleanSettings("old_vertretung", false);
    }

    public static boolean isOldTitle() {
        return ApplicationFeatures.getBooleanSettings("old_vertretung_title", true);
    }

    public static boolean isSummarizeUp() {
        return getBooleanSettings("summarize", true);
    }

    public static boolean isSummarizeOld() {
        return getBooleanSettings("summarize_old", true);
    }


    //SubstitutionPlan
    public static String getUsername(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString("username", "");
    }

    public static String getPassword(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString("password", "");
    }

    public static String getTodayURL(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString("today_url", External_Const.todayURL);
    }

    public static String getTomorrowURL(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString("tomorrow_url", External_Const.tomorrowURL);
    }

    public static boolean isOfflineMode() {
        return getBooleanSettings("offline_mode", true);
    }

    //Teacherlist
    public static String getTeacherlistURL(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString("teacherlist_url", External_Const.teacherlistUrl);
    }


    //Other
    public static int[] getAlarmTime() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
        return new int[]{sharedPref.getInt("Alarm_hour", -1), sharedPref.getInt("Alarm_minute", -1), sharedPref.getInt("Alarm_second", -1)};
    }

    public static void setAlarmTime(int... times) {
        if (times.length != 3) {
            if (times.length > 0 && times[0] == 0) {
                setAlarm(ApplicationFeatures.getContext(), false);
            } else {
                System.out.println("wrong parameters");
            }
            return;
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        setAlarm(ApplicationFeatures.getContext(), true);
        editor.putInt("Alarm_hour", times[0]);
        editor.putInt("Alarm_minute", times[1]);
        editor.putInt("Alarm_second", times[2]);
        editor.commit();

    }

    public static int getIntSettings(String key, int defaultValue) {
        Context context = ApplicationFeatures.getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getInt(key, defaultValue);
    }

    @StyleRes
    public static int getGeneralTheme() {
        Context context = ApplicationFeatures.getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return getThemeResFromPrefValue(sharedPref.getString("theme", "switch"));
    }

    @StyleRes
    private static int getThemeResFromPrefValue(String themePrefValue) {
        switch (themePrefValue) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return R.style.AppTheme_Dark;
            case "black":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return R.style.AppTheme_Black;
            case "switch":
                int nightModeFlags = ApplicationFeatures.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
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

    public static void changeDesign(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean newdesign = !sharedPref.getBoolean("old_vertretung", false);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("old_vertretung", newdesign);
        editor.commit();
    }
}
