package com.asdoi.gymwen.main;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.asdoi.gymwen.R;

import java.util.concurrent.ExecutionException;

import androidx.browser.customtabs.CustomTabsIntent;
import saschpe.android.customtabs.CustomTabsHelper;
import saschpe.android.customtabs.WebViewFallback;

public abstract class ViewActions {

    public static void tabIntent(String url, Context context) {
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .addDefaultShareMenuItem()
                    .setToolbarColor(context.getResources()
                            .getColor(R.color.colorPrimary))
                    .setShowTitle(true)
                    .setCloseButtonIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_arrow_back_white_24dp))
                    .build();

            // This is optional but recommended
            CustomTabsHelper.addKeepAliveExtra(context, customTabsIntent.intent);

            // This is where the magic happens...
            CustomTabsHelper.openCustomTab(context, customTabsIntent,
                    Uri.parse(url),
                    new WebViewFallback());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
