package com.asdoi.gymwen;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;

import com.asdoi.gymwen.main.MainActivity;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import de.cketti.library.changelog.ChangeLog;
import info.isuru.sheriff.enums.SheriffPermission;
import info.isuru.sheriff.helper.Sheriff;
import info.isuru.sheriff.interfaces.PermissionListener;
import io.github.tonnyl.whatsnew.WhatsNew;
import io.github.tonnyl.whatsnew.item.WhatsNewItem;
import io.github.tonnyl.whatsnew.util.PresentationOption;
import saschpe.android.customtabs.CustomTabsHelper;
import saschpe.android.customtabs.WebViewFallback;


public class ActivityFeatures extends AppCompatActivity implements PermissionListener {
    public Context getContext() {
        return ApplicationFeatures.getContext();
    }

    //Changelog
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


    //Permissions
    private Sheriff sheriffPermission;
    private AlertDialog dialog;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        sheriffPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsGranted(int requestCode, ArrayList<String> acceptedPermissionList) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, ArrayList<String> deniedPermissionList) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getContext().getString(R.string.permission_required));
        builder.setMessage(getContext().getString(R.string.permission_required_description));

        // add the buttons
        builder.setPositiveButton(getContext().getString(R.string.permission_ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openAppPermissionSettings();
            }
        });
        builder.setNegativeButton(getContext().getString(R.string.permission_cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.cancel();
            }
        });

        // create and show the alert dialog
        dialog = builder.create();
        dialog.show();
    }

    private static final int REQUEST_MULTIPLE_PERMISSION = 101;

    private void requestPermission(SheriffPermission... permissions) {
        sheriffPermission = Sheriff.Builder()
                .with(this)
                .requestCode(REQUEST_MULTIPLE_PERMISSION)
                .setPermissionResultCallback(this)
                .askFor(permissions)
                .rationalMessage(getContext().getString(R.string.sheriff_permission_rational))
                .build();

        sheriffPermission.requestPermissions();
    }

    private void openAppPermissionSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }


    //DownloadManager
    private String subPath;

    public void startDownload(String url, String title, String description, String subPath, BroadcastReceiver onComplete) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(SheriffPermission.STORAGE);

            /*if (baseView != null) {
                Snackbar.make(baseView, R.string.down_permission_denied, Snackbar.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.down_permission_denied, Toast.LENGTH_LONG).show();
            }*/
            return;
        }

        registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        registerReceiver(onNotificationClick,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

        this.subPath = subPath;

        Uri uri = Uri.parse(url);

        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();

        DownloadManager mgr = (DownloadManager) getContext().getSystemService(DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                        DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(title)
                .setDescription(description)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, subPath)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.allowScanningByMediaScanner();

        mgr.enqueue(request);

    }

    public BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            installApk(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + subPath);
        }
    };

    BroadcastReceiver onNotificationClick = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Intent i = new Intent(getContext(), MainActivity.class);
            startActivity(i);
        }
    };


    //Apk Installer
    public void installApk(String path) {
        File apkFile = new File(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", apkFile);
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}
