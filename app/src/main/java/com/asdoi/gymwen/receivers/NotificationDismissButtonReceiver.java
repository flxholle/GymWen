package com.asdoi.gymwen.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.asdoi.gymwen.ApplicationFeatures;

import java.util.Objects;

public class NotificationDismissButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(@NonNull Context context, Intent intent) {
        // if you want cancel notification
        try {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Objects.requireNonNull(manager).cancel(ApplicationFeatures.NOTIFICATION_ID);
            manager.cancel(ApplicationFeatures.NOTIFICATION_ID_2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
