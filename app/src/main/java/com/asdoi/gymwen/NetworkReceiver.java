package com.asdoi.gymwen;

import android.content.Context;
import android.content.Intent;

import com.asdoi.gymwen.VertretungsplanInternal.VertretungsPlan;

public class NetworkReceiver extends BootReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, NotificationService.class);

        //Check if Network is available
        if (VertretungsPlan.checkedAtNetworkChange) {
            return;
        } else if (isNetworkAvailable(context)) {
            VertretungsPlan.checkedAtNetworkChange = true;
            context.stopService(service);
            context.startService(service);
        }
    }
}
