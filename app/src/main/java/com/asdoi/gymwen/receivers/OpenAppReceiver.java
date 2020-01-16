package com.asdoi.gymwen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.asdoi.gymwen.ui.main.activities.MainActivity;

public class OpenAppReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && context != null) {
            switch (intent.getAction()) {
                case Intent.ACTION_MY_PACKAGE_REPLACED:
                case Intent.ACTION_PACKAGE_REPLACED:
                    Intent i = new Intent(context, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
            }
        }
    }
}
