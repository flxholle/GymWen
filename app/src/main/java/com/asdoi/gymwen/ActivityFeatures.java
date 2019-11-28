package com.asdoi.gymwen;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;

public class ActivityFeatures extends AppCompatActivity {
    public void tabIntent(String url) {
        throw new NullPointerException();
//        Context context = this;
//        try {
//            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
//                    .addDefaultShareMenuItem()
//                    .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
//                    .setShowTitle(true)
//                    .setCloseButtonIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_arrow_back_white_24dp))
//                    .build();
//
////            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            // This is optional but recommended
//            CustomTabsHelper.addKeepAliveExtra(context, customTabsIntent.intent);
//
//            // This is where the magic happens...
//            CustomTabsHelper.openCustomTab(context, customTabsIntent,
//                    Uri.parse(url),
//                    new WebViewFallback());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void checkUpdates(Display display, boolean showUpdated) {
        Context context = this;
        try {
            String url = "https://gitlab.com/asdoi/gymwenreleases/raw/master/UpdaterFile.json";
            AppUpdater appUpdater = new AppUpdater(context)
                    .setDisplay(display)
                    .setUpdateFrom(UpdateFrom.JSON)
                    .setUpdateJSON(url)
                    .setTitleOnUpdateAvailable(R.string.update_available_title)
                    .setContentOnUpdateAvailable(R.string.update_available_content)
                    .setTitleOnUpdateNotAvailable(R.string.update_not_available_title)
                    .setContentOnUpdateNotAvailable(R.string.update_not_available_content)
                    .setButtonUpdate(R.string.update_now)
                    .setButtonDismiss(R.string.update_later)
                    .setButtonDoNotShowAgain(null)
                    .setIcon(R.drawable.ic_system_update_black_24dp)
                    .showAppUpdated(showUpdated);
            appUpdater.start();
        } catch (Exception e) {
            //Create new Updater
        }
    }
}
