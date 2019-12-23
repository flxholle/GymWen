package com.asdoi.gymwen;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.asdoi.gymwen.ui.main.activities.MainActivity;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;
import com.google.android.material.snackbar.Snackbar;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import de.cketti.library.changelog.ChangeLog;
import info.isuru.sheriff.enums.SheriffPermission;
import info.isuru.sheriff.helper.Sheriff;
import info.isuru.sheriff.interfaces.PermissionListener;
import ru.github.igla.ferriswheel.FerrisWheelView;
import saschpe.android.customtabs.CustomTabsHelper;
import saschpe.android.customtabs.WebViewFallback;


public class ActivityFeatures extends AppCompatActivity implements PermissionListener, TimePickerDialog.OnTimeSetListener {
    public Context getContext() {
        return this;
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
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

    //TabIntent and UpdateCheck
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
        String url = "https://gitlab.com/asdoi/gymwenreleases/raw/master/UpdaterFile.json";

        try {
            AppUpdater appUpdater = new AppUpdater(context)
                    .setDisplay(display)
                    .setUpdateFrom(UpdateFrom.JSON)
                    .setUpdateJSON(url)
                    .setTitleOnUpdateAvailable(R.string.update_available_title)
                    .setContentOnUpdateAvailable(R.string.update_available_content)
                    .setTitleOnUpdateNotAvailable(R.string.update_not_available_title)
                    .setContentOnUpdateNotAvailable(R.string.update_not_available_content)
                    .setButtonUpdate(R.string.update_now)
                    .setButtonUpdateClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                String apkUrl = "https://gitlab.com/asdoi/gymwenreleases/raw/master/GymWenApp.apk";
                                startDownload(apkUrl, "GymWen Version " + (BuildConfig.VERSION_CODE + 1), getContext().getString(R.string.update_down_title), "GymWenAppv" + (BuildConfig.VERSION_CODE + 1) + ".apk", installApk);
                            } catch (Exception e) {
                                tabIntent("https://gitlab.com/asdoi/gymwenreleases/blob/master/GymWenApp.apk");
                            }
                        }
                    })
                    .setButtonDismiss(R.string.update_later)
                    .setButtonDoNotShowAgain(/*R.string.update_website*/ null)
                    /*.setButtonDismissClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            tabIntent("https://gitlab.com/asdoi/gymwenreleases/blob/master/GymWenApp.apk");
                        }
                    })*/
                    .setIcon(R.drawable.ic_system_update_black_24dp)
                    .showAppUpdated(showUpdated);
            appUpdater.start();
        } catch (Exception e) {
            AppUpdaterUtils appUpdaterUtils = new AppUpdaterUtils(this)
                    .setUpdateFrom(UpdateFrom.JSON)
                    .setUpdateJSON(url)
                    .withListener(new AppUpdaterUtils.UpdateListener() {
                        @Override
                        public void onSuccess(Update update, Boolean isUpdateAvailable) {
                            if (isUpdateAvailable) {
                                Toast.makeText(getContext(), getContext().getString(R.string.update_available_title), Toast.LENGTH_LONG).show();
                                tabIntent("https://gitlab.com/asdoi/gymwenreleases/blob/master/GymWenApp.apk");
                            }
                        }

                        @Override
                        public void onFailed(AppUpdaterError error) {

                        }
                    });
            appUpdaterUtils.start();
        }
    }

    public static void createLoadingPanel(ViewGroup view) {
        Context context = ApplicationFeatures.getContext();
        FrameLayout base = new FrameLayout(context);
        base.setBackgroundColor(ContextCompat.getColor(ApplicationFeatures.getContext(), R.color.colorBackground));
        base.setTag("vertretung_loading");
        base.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout panel = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 0, 0, 30);
        panel.setLayoutParams(params);
        panel.setGravity(Gravity.BOTTOM);
        panel.setOrientation(LinearLayout.VERTICAL);

        FerrisWheelView ferrisWheelView = new FerrisWheelView(context);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ferrisWheelView.setLayoutParams(params2);
        ferrisWheelView.setNumberOfCabins(8);
        ferrisWheelView.setRotateDegreeSpeedInSec(35);
//        ferrisWheelView.setWheelColor(R.color.wheel_wheel);
//        ferrisWheelView.setClockwise(false);
        ferrisWheelView.setAutoRotate(true);
        ferrisWheelView.startAnimation();


        ProgressBar bar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        bar.setIndeterminate(true);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(20, 5, 20, 0);
        bar.setLayoutParams(params);
        bar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(ApplicationFeatures.getContext(), R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);


        TextView textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setText(ApplicationFeatures.getContext().getString(R.string.downloading));

        base.addView(ferrisWheelView);

        panel.addView(bar);
        panel.addView(textView);

        base.addView(panel);


        view.addView(base);
    }

    public View getTeacherView(View view, String[] entry) {
        TextView kürzel = view.findViewById(R.id.teacher_kürzel);
        kürzel.setText(entry[0]);

        TextView nname = view.findViewById(R.id.teacher_nname);
        nname.setText(entry[1]);

        TextView vname = view.findViewById(R.id.teacher_vname);
        vname.setText(" " + entry[2]);

        TextView hour = view.findViewById(R.id.teacher_hour);
        hour.setText(entry[3]);
        hour.setVisibility(View.GONE);

        LinearLayout hideLayout = view.findViewById(R.id.teacher_list_linear);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        hideLayout.setLayoutParams(params);


        Button mailButton = view.findViewById(R.id.teacher_mail);
        mailButton.setOnClickListener((View v) -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + entry[0] + "@gym-wendelstein.de"));
            try {
                startActivity(emailIntent);
            } catch (Exception e) {
                try {
                    emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(emailIntent);
                } catch (ActivityNotFoundException e2) {
                    Snackbar.make(v, ApplicationFeatures.getContext().getString(R.string.no_email_app), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        FrameLayout root = view.findViewById(R.id.teacher_rootLayout);
        root.setOnClickListener((View v) -> {
            hour.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            hideLayout.setLayoutParams(params2);
        });

        return view;
    }


    //Permissions
    private Sheriff sheriffPermission;
    private static final int REQUEST_MULTIPLE_PERMISSION = 101;
    private Thread permissionRunAfter;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        sheriffPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsGranted(int requestCode, ArrayList<String> acceptedPermissionList) {
        if (permissionRunAfter == null)
            return;
        try {
            permissionRunAfter.run();
        } catch (Exception e) {
        }
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
                dialogInterface.cancel();
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void requestPermission(Thread runAfter, SheriffPermission... permissions) {
        sheriffPermission = Sheriff.Builder()
                .with(this)
                .requestCode(REQUEST_MULTIPLE_PERMISSION)
                .setPermissionResultCallback(this)
                .askFor(permissions)
                .rationalMessage(getContext().getString(R.string.sheriff_permission_rational))
                .build();

        permissionRunAfter = runAfter;

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

            requestPermission(new Thread(() -> {
                        startDownload(url, title, description, subPath, onComplete);
                    }),
                    SheriffPermission.STORAGE);
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

    public BroadcastReceiver installApk = new BroadcastReceiver() {
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


    //Time picker
    public class NotifyWork extends Worker {

        public NotifyWork(@NonNull Context context, @NonNull WorkerParameters params) {
            super(context, params);
        }


        @Override
        public Result doWork() {
            ApplicationFeatures.proofeNotification();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            int time = sharedPref.getInt("notif_time", 0);
            Calendar calendar = Calendar.getInstance();
            System.out.println("Seconds in current minute = " + calendar.get(Calendar.SECOND));

            int currentTime = time - getCurrentTimeInSeconds();
            scheduleNotification(currentTime);
            return Result.success();
        }
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        int time = hourOfDay * 3600 + minute * 60 + second;
        System.out.println("You picked the following time: " + hourOfDay + "h" + minute + "m" + second);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("notif_time", time);
        editor.apply();

        int currentTimeDelay = time - getCurrentTimeInSeconds();
        scheduleNotification(currentTimeDelay);
    }

    public void createTimePicker() {
        Calendar now = Calendar.getInstance();

        TimePickerDialog tpd = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );
        tpd.setTitle("Pick a time");
        tpd.show(getSupportFragmentManager(), "Timepickerdialog");
    }

    private static final String NOTIFICATION_WORK = "notif_work";

    public void scheduleNotification(long timeInSeconds) {
        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotifyWork.class)/*.setInitialDelay(timeInSeconds, TimeUnit.SECONDS)*/.build();
        WorkManager.getInstance().beginUniqueWork(NOTIFICATION_WORK, ExistingWorkPolicy.REPLACE, notificationWork).enqueue();
//        WorkManager.getInstance().enqueue(notificationWork);

//        PeriodicWorkRequest notificationWork = new PeriodicWorkRequest.Builder(NotifyWork.class, timeInSeconds, TimeUnit.SECONDS).build();
//        WorkManager.getInstance(this).enqueue(notificationWork);
    }

    public static int getCurrentTimeInSeconds() {
        Calendar calendar = Calendar.getInstance();
        int time = calendar.get(Calendar.SECOND);
        time += calendar.get(Calendar.MINUTE) * 60;
        time += calendar.get(Calendar.HOUR_OF_DAY) * 3600;
        return time;
    }

}
