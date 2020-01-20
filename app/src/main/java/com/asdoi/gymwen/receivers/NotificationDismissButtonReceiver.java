package com.asdoi.gymwen.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.asdoi.gymwen.ApplicationFeatures;

public class NotificationDismissButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // if you want cancel notification
        try {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(ApplicationFeatures.NOTIFICATION_ID);
            manager.cancel(ApplicationFeatures.NOTIFICATION_ID_2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
