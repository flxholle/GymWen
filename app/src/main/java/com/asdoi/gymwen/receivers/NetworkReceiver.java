package com.asdoi.gymwen.receivers;

import android.content.Context;
import android.content.Intent;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.vertretungsplanInternal.VertretungsPlan;

public class NetworkReceiver extends BootReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        //Check if Network is available
        if (VertretungsPlan.checkedAtNetworkChange) {
            return;
        } else if (isNetworkAvailable(context)) {
            VertretungsPlan.checkedAtNetworkChange = true;
            ApplicationFeatures.proofeNotification();
        }
    }
}
