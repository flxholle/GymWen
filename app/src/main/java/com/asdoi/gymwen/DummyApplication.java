package com.asdoi.gymwen;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.asdoi.gymwen.VertretungsplanInternal.VertretungsPlan;
import com.asdoi.gymwen.main.ChoiceActivity;
import com.asdoi.gymwen.main.SignInActivity;

public class DummyApplication extends Application {
    private static Context mContext;
    public static boolean checkedAtNetworkChange = false;

    static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }

    public static boolean setSettings() {
        Context context = getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean signedIn = sharedPref.getBoolean("signed", false);

        if (signedIn) {

            boolean oberstufe = sharedPref.getBoolean("oberstufe", true);
            String courses = sharedPref.getString("courses", "");
            if (courses.trim().isEmpty()) {
                Intent i = new Intent(context, ChoiceActivity.class);
                context.startActivity(i);
                return signedIn;
            }
            VertretungsPlan.setup(oberstufe, courses.split("#"), courses);

            System.out.println("settings: " + oberstufe + courses);

            String username = sharedPref.getString("username", "");
            String password = sharedPref.getString("password", "");

            VertretungsPlan.signin(username, password);
            proofeNotification();
            updateMyWidgets(context);
        } else {
            Intent i = new Intent(context, SignInActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        return signedIn;
    }

    public static void proofeNotification() {
        Context context = getContext();
        Intent intent = new Intent(context, NotificationService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.stopService(intent);
        context.startService(intent);
    }

    public static void updateMyWidgets(Context context) {
        AppWidgetManager man = AppWidgetManager.getInstance(context);
        int[] ids = man.getAppWidgetIds(new ComponentName(context, VertretungsplanWidget.class));
        Intent updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(VertretungsplanWidget.WIDGET_ID_KEY, ids);
        context.sendBroadcast(updateIntent);
    }
}
