package com.asdoi.gymwen;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;

import de.cketti.library.changelog.ChangeLog;
import io.github.tonnyl.whatsnew.WhatsNew;
import io.github.tonnyl.whatsnew.item.WhatsNewItem;
import io.github.tonnyl.whatsnew.util.PresentationOption;
import saschpe.android.customtabs.CustomTabsHelper;
import saschpe.android.customtabs.WebViewFallback;

public class ActivityFeatures extends AppCompatActivity {
    public void tabIntent(String url) {
        Context context = this;
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .addDefaultShareMenuItem()
                    .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setShowTitle(true)
                    .setCloseButtonIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_arrow_back_white_24dp))
                    .build();

//            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // This is optional but recommended
            CustomTabsHelper.addKeepAliveExtra(context, customTabsIntent.intent);

            // This is where the magic happens...
            CustomTabsHelper.openCustomTab(context, customTabsIntent,
                    Uri.parse(url),
                    new WebViewFallback());
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    public Context getContext() {
        return ApplicationFeatures.getContext();
    }

    public void showChangelogCK(boolean checkFirstRun) {
        ChangeLog cl = new ChangeLog(this);
        if (checkFirstRun) {
            if (cl.isFirstRun())
                cl.getLogDialog().show();
        } else {
            cl.getFullLogDialog().show();
        }
    }

    public void showChanglogTonny() {
      /*  WhatsNew.newInstance(
//                        new WhatsNewItem("Nice Icons", "Completely customize colors, texts and icons.", R.drawable.ic_heart),
//                        new WhatsNewItem("Such Easy", "Setting this up only takes 2 lines of code, impressive you say?", R.drawable.ic_thumb_up),
                new WhatsNewItem("Very Sleep", "It helps you get more sleep by writing less code.", R.drawable.ic_refresh_black_24dp),
                new WhatsNewItem("Text Only", "No icons? Just go with plain text.", WhatsNewItem.NO_IMAGE_RES_ID)
        ).presentAutomatically(this);*/

        WhatsNew whatsNew = WhatsNew.newInstance(
                new WhatsNewItem("Nice Icons", "Completely customize colors, texts and icons.", R.drawable.ic_close_black_24dp),
                new WhatsNewItem("Such Easy", "Setting this up only takes 2 lines of code, impressive you say?", R.drawable.ic_menu_share_white_24dp),
                new WhatsNewItem("Very Sleep", "It helps you get more sleep by writing less code.", R.drawable.ic_refresh_black_24dp),
                new WhatsNewItem("Text Only", "No icons? Just go with plain text.", WhatsNewItem.NO_IMAGE_RES_ID));

        whatsNew.setPresentationOption(PresentationOption.ALWAYS);

        whatsNew.setTitleColor(ContextCompat.getColor(this, R.color.colorAccent));
        whatsNew.setTitleText("What's Up");

        whatsNew.setButtonText("Got it!");
        whatsNew.setButtonBackground(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        whatsNew.setButtonTextColor(ContextCompat.getColor(this, R.color.colorAccent));

        whatsNew.setItemTitleColor(ContextCompat.getColor(this, R.color.colorAccent));
        whatsNew.setItemContentColor(Color.parseColor("#808080"));

        whatsNew.presentAutomatically(this);
    }
}
