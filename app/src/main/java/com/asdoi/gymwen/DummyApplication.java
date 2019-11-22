package com.asdoi.gymwen;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.asdoi.gymwen.VertretungsplanInternal.VertretungsPlan;
import com.asdoi.gymwen.main.ChoiceActivity;
import com.asdoi.gymwen.main.SignInActivity;

public class DummyApplication extends Application {
    private static Context mContext;
    public static boolean checkedAtNetworkChange = false;

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

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }


    public static Context getContext() {
        return mContext;
    }
}
