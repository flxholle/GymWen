package com.asdoi.gymwen;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;


public class NotifyScheduler extends Worker {
    private static final String NOTIFICATION_WORK = "notif_work";

    public NotifyScheduler(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        System.out.println("Do work");
        ApplicationFeatures.sendNotification();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
        int time = sharedPref.getInt("notif_time", 0);

        int currentTime = time - ApplicationFeatures.getCurrentTimeInSeconds();
        scheduleNotification(currentTime, TimeUnit.SECONDS);
        return Result.success();
    }


    public static void scheduleNotification(long time, TimeUnit timeUnit) {
        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotifyScheduler.class).setInitialDelay(time, timeUnit).build();
        WorkManager workManager = WorkManager.getInstance(ApplicationFeatures.getContext());
        workManager.beginUniqueWork(NOTIFICATION_WORK, ExistingWorkPolicy.REPLACE, notificationWork).enqueue();
//        WorkManager.getInstance().enqueue(notificationWork);

//        PeriodicWorkRequest notificationWork = new PeriodicWorkRequest.Builder(NotifyScheduler.class, timeInSeconds, TimeUnit.SECONDS).build();
//        WorkManager.getInstance(this).enqueue(notificationWork);
    }

    public static final int DAILY_REMINDER_REQUEST_CODE = 100;
    public static final String TAG = "NotificationScheduler";

}
