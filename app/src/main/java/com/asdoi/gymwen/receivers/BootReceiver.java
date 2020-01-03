package com.asdoi.gymwen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.asdoi.gymwen.ApplicationFeatures;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && context != null) {
            switch (intent.getAction()) {
                case Intent.ACTION_DATE_CHANGED:
                case Intent.ACTION_BOOT_COMPLETED:
                case Intent.ACTION_LOCKED_BOOT_COMPLETED:
                    ApplicationFeatures.sendNotification();
                    break;
                default:
                    break;
            }
        }
    }
}
