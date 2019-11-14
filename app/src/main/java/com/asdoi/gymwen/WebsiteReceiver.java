package com.asdoi.gymwen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.asdoi.gymwen.main.WebsiteActivity;

public class WebsiteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // !!Useless!!

        Uri data = intent.getData();
//        String scheme = data.getScheme(); // "http"
        Log.i("Web", "startedApp");
        String host = data.getHost(); // "twitter.com"
        Intent websiteIntent = new Intent(context, WebsiteActivity.class);
        websiteIntent.putExtra("url",host);
        context.startActivity(websiteIntent);
    }
}
