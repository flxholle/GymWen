package com.asdoi.gymwen;

import android.content.Context;
import android.content.Intent;

public class NetworkReceiver extends BootReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, NotificationService.class);

        //Check if Network is available
        if (DummyApplication.checkedAtNetworkChange) {
            return;
        } else if (isNetworkAvailable(context)) {
            DummyApplication.checkedAtNetworkChange = true;
            context.stopService(service);
            context.startService(service);
        }
    }
}
