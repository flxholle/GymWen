/*
 * Copyright (c) 2020 Felix Hollederer
 *     This file is part of GymWenApp.
 *
 *     GymWenApp is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GymWenApp is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GymWenApp.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.asdoi.gymwen;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.asdoi.gymwen.teacherlist.TeacherListEntry;
import com.asdoi.gymwen.ui.activities.MainActivity;
import com.asdoi.gymwen.util.External_Const;
import com.asdoi.gymwen.util.PreferenceUtil;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;
import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialDialogsUtil;
import com.pd.chocobar.ChocoBar;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import de.cketti.library.changelog.ChangeLog;
import info.isuru.sheriff.enums.SheriffPermission;
import info.isuru.sheriff.helper.Sheriff;
import info.isuru.sheriff.interfaces.PermissionListener;
import ru.github.igla.ferriswheel.FerrisWheelView;
import saschpe.android.customtabs.CustomTabsHelper;
import saschpe.android.customtabs.WebViewFallback;


public abstract class ActivityFeatures extends AppCompatActivity {

    @NonNull
    public Context getContext() {
        return this;
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        setupColors();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PreferenceUtil.getGeneralTheme());
        setStatusbarColor();
        super.onCreate(savedInstanceState);
        MaterialDialogsUtil.updateMaterialDialogsThemeSingleton(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    protected abstract void setupColors();

    protected void setToolbar(boolean backButton) {
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setBackgroundColor(ApplicationFeatures.getPrimaryColor(this));
                setSupportActionBar(toolbar);
            }
            if (backButton)
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Colors

    /**
     * @author Karim Abou Zeid (kabouzeid) from VinylMusicPlayer
     */
    private void setStatusbarColor() {
        // we don't want to use statusbar color because we are doing the color darkening on our own to support KitKat
        setStatusbarColor(ApplicationFeatures.getPrimaryColor(this));
    }

    private void setStatusbarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final View statusBar = /*getWindow().getDecorView().getRootView().findViewById(R.id.status_bar)*/ null;
            if (statusBar != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    statusBar.setBackgroundColor(ColorUtil.darkenColor(color));
                    setLightStatusbarAuto(color);
                } else {
                    statusBar.setBackgroundColor(color);
                }
            } else if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(ColorUtil.darkenColor(color));
                setLightStatusbarAuto(color);
            }
        }
    }

    private void setLightStatusbarAuto(int bgColor) {
        setLightStatusbar(ColorUtil.isColorLight(bgColor));
    }

    private void setLightStatusbar(boolean enabled) {
        ATH.setLightStatusbar(this, enabled);
    }


    //Changelog
    protected void showChangelogCK(boolean checkFirstRun) {
        ChangeLog cl = new ChangeLog(this);
        try {
            if (checkFirstRun) {
                if (cl.isFirstRun())
                    cl.getLogDialog().show();
            } else {
                cl.getFullLogDialog().show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //TabIntent and UpdateCheck
    public void tabIntent(String url) {
        Context context = this;
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .addDefaultShareMenuItem()
                    .setToolbarColor(ApplicationFeatures.getPrimaryColor(this))
                    .setShowTitle(true)
                    .build();

            // This is optional but recommended
            CustomTabsHelper.Companion.addKeepAliveExtra(context, customTabsIntent.intent);

            // This is where the magic happens...
            CustomTabsHelper.Companion.openCustomTab(context, customTabsIntent,
                    Uri.parse(url),
                    new WebViewFallback());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkUpdates(Display display, boolean showUpdated) {
        Context context = this;
        String url = External_Const.UPDATER_JSON;

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
                    .setButtonUpdateClickListener((dialogInterface, i) -> {
                        try {
                            String apkUrl = External_Const.APK_DOWNLOAD;
                            startDownload(apkUrl, "GymWen Version " + (BuildConfig.VERSION_CODE + 1), getContext().getString(R.string.update_down_title), Environment.DIRECTORY_DOWNLOADS, "GymWenAppv" + (BuildConfig.VERSION_CODE + 1) + ".apk", new installApk("GymWenAppv" + (BuildConfig.VERSION_CODE + 1) + ".apk"));
                            dialogInterface.dismiss();
                        } catch (Exception e) {
                            tabIntent(External_Const.APK_DOWNLOAD_PAGE);
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
                                tabIntent(External_Const.APK_DOWNLOAD_PAGE);
                            }
                        }

                        @Override
                        public void onFailed(AppUpdaterError error) {

                        }
                    });
            appUpdaterUtils.start();
        }
    }

    public void createLoadingPanel(@NonNull ViewGroup view) {
        Context context = ApplicationFeatures.getContext();
        FrameLayout base = new FrameLayout(context);
        base.setBackgroundColor(ApplicationFeatures.getBackgroundColor(this));
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                bar.getIndeterminateDrawable().setColorFilter(new BlendModeColorFilter(ApplicationFeatures.getAccentColor(this), BlendMode.SRC_ATOP));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //noinspection deprecation
            bar.getIndeterminateDrawable().setColorFilter(ApplicationFeatures.getAccentColor(this), PorterDuff.Mode.SRC_ATOP);
        }


        TextView textView = new TextView(context);
        textView.setTextColor(ApplicationFeatures.getTextColorPrimary(this));
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

    public static void removeLoadingPanel(@NonNull ViewGroup view) {
        view.removeView(view.findViewWithTag("vertretung_loading"));
    }

    @NonNull
    public ViewGroup createTeacherView(@NonNull TeacherListEntry teacher) {
        LinearLayout base = new LinearLayout(this);
        base.setOrientation(LinearLayout.VERTICAL);
        base.setGravity(Gravity.CENTER);
        base.setBackgroundColor(ApplicationFeatures.getTextColorSecondary(this));
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        base.setLayoutParams(params);
        base.setId(ApplicationFeatures.substitution_teacher_view_id);
        base.setOnClickListener((View v) -> {
            try {
                ((ViewGroup) v.getParent()).removeView(v);
            } catch (NullPointerException e) {
                v.setVisibility(View.GONE);
            }
        });

        ViewGroup teacherEntry = new LinearLayout(this);
        ViewStub viewStub = new ViewStub(this);
        viewStub.setLayoutResource(R.layout.list_teacherlist_entry);
        teacherEntry.addView(viewStub);
        viewStub.inflate();
        teacherEntry = (ViewGroup) getTeacherView(teacherEntry, teacher);

        LinearLayout background = new LinearLayout(this);
        params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        background.setLayoutParams(params);
        background.setBackgroundColor(ApplicationFeatures.getBackgroundColor(this));

        background.addView(teacherEntry);
        base.addView(background);
        return base;
    }

    @NonNull
    public View getTeacherView(@NonNull View view, @NonNull TeacherListEntry entry) {
        TextView kuerzel = view.findViewById(R.id.teacher_kürzel);
        kuerzel.setText(entry.getShort());

        TextView nname = view.findViewById(R.id.teacher_nname);
        nname.setText(entry.getName());

        TextView vname = view.findViewById(R.id.teacher_vname);
        vname.setText(" " + entry.getFirst_name());

        vname.setTextIsSelectable(false);
        nname.setTextIsSelectable(false);

        TextView hour = view.findViewById(R.id.teacher_hour);
        hour.setText(entry.getMeeting());
        hour.setVisibility(View.GONE);

        LinearLayout hideLayout = view.findViewById(R.id.teacher_list_linear);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        hideLayout.setLayoutParams(params);


        Button mailButton = view.findViewById(R.id.teacher_mail);
        mailButton.setOnClickListener((View v) -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + entry.getShort() + External_Const.MAIL_ENDING));
            try {
                startActivity(emailIntent);
            } catch (Exception e) {
                try {
                    emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(emailIntent);
                } catch (ActivityNotFoundException e2) {
                    ChocoBar.builder().setActivity(this).setText(getString(R.string.no_email_app)).setDuration(ChocoBar.LENGTH_LONG).red().show();
                }
            }
        });

        try {
            FrameLayout root = view.findViewById(R.id.teacher_rootLayout);
            root.setOnClickListener((View v) -> {
                hour.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                hideLayout.setLayoutParams(params2);
                vname.setTextIsSelectable(true);
                nname.setTextIsSelectable(true);
            });
        } catch (Exception e) {
            //NullPointerException for root on API 17
            e.printStackTrace();
        }

        return view;
    }

    public boolean startApp(@NonNull String... packageNames) {
        Intent intent;
        for (String s : packageNames) {
            intent = getPackageManager().getLaunchIntentForPackage(s);
            if (intent != null) {
                startActivity(intent);
                return true;
            }
        }
        return false;
    }

    protected boolean openAppInStore(@NonNull String... packageNames) {
        boolean run = false;
        for (String s : packageNames) {
            try {
                //Open Free Version
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + s)));
                run = true;
            } catch (android.content.ActivityNotFoundException anfe) {
                run = false;
            }
            if (run)
                break;
        }
        return run;
    }


    //Permissions
    private Sheriff sheriffPermission;
    private static final int REQUEST_MULTIPLE_PERMISSION = 101;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        sheriffPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void requestPermission(Runnable runAfter, SheriffPermission... permissions) {
        PermissionListener pl = new MyPermissionListener(runAfter);

        sheriffPermission = Sheriff.Builder()
                .with(this)
                .requestCode(REQUEST_MULTIPLE_PERMISSION)
                .setPermissionResultCallback(pl)
                .askFor(permissions)
                .rationalMessage(getContext().getString(R.string.sheriff_permission_rational))
                .build();

        sheriffPermission.requestPermissions();
    }

    private class MyPermissionListener implements PermissionListener {
        final Runnable runAfter;

        MyPermissionListener(Runnable r) {
            runAfter = r;
        }

        @Override
        public void onPermissionsGranted(int requestCode, ArrayList<String> acceptedPermissionList) {
            if (runAfter == null)
                return;
            try {
                runAfter.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPermissionsDenied(int requestCode, ArrayList<String> deniedPermissionList) {
            // setup the alert builder
            MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
            builder.title(getContext().getString(R.string.permission_required));
            builder.content(getContext().getString(R.string.permission_required_description));

            // add the buttons
            builder.onPositive((dialog, which) -> {
                openAppPermissionSettings();
                dialog.dismiss();
            });
            builder.positiveText(getContext().getString(R.string.permission_ok_button));

            builder.negativeText(getContext().getString(R.string.permission_cancel_button));
            builder.onNegative((dialog, which) -> dialog.dismiss());

            // create and show the alert dialog
            MaterialDialog dialog = builder.build();
            dialog.show();
        }
    }

    private void openAppPermissionSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }


    //DownloadManager
    private long downloadID;

    private void startDownload(String url, String title, String description, String dirType, String subPath, BroadcastReceiver onComplete) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermission(() -> startDownload(url, title, description, dirType, subPath, onComplete), SheriffPermission.STORAGE);
            return;

        }

        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        registerReceiver(onNotificationClick, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

        Uri uri = Uri.parse(url);

        DownloadManager mgr = (DownloadManager) getContext().getSystemService(DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                        DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(title)
                .setDescription(description)
                .setDestinationInExternalPublicDir(dirType, subPath)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        downloadID = Objects.requireNonNull(mgr).enqueue(request);

        Toast.makeText(this, getString(R.string.download_start), Toast.LENGTH_LONG).show();

    }

    private class installApk extends BroadcastReceiver {
        final String subPath;

        public installApk(String subPath) {
            this.subPath = subPath;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(Context ctxt, @NonNull Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadID == id) {
//                installApk(ctxt.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + subPath);
                installApk(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + subPath);
                unregisterReceiver(this);
            }
        }
    }

    @NonNull
    private final
    BroadcastReceiver onNotificationClick = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Intent i = new Intent(getContext(), MainActivity.class);
            startActivity(i);
            unregisterReceiver(this);
        }
    };


    //Apk Installer
    private void installApk(@NonNull String path) {
        File apkFile = new File(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", apkFile);
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }


    //Make Call
    protected void makeCall(String telNr) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + telNr));
        startActivity(intent);
    }


    //Grades Management
    private final static String gradesFileName = "Notenverwaltung.xlsx";
    private final static String downloadGradesTable = "https://gitlab.com/asdoi/Overview-about-your-grades/raw/master/Gesamtes_Notenbild.xlsx?inline=false";

    @SuppressWarnings("deprecation")
    protected void checkGradesFile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(this::checkGradesFile, SheriffPermission.STORAGE);
            return;
        }

//        String path = this.getExternalFilesDir(Build.VERSION.SDK_INT >= 19 ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS) + File.separator + gradesFileName;
        String path = Environment.getExternalStoragePublicDirectory(Build.VERSION.SDK_INT >= 19 ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS) + File.separator + gradesFileName;
        File file = new File(path);
        if (file.exists()) {
            openGradesFile();
        } else {
            startDownload(downloadGradesTable, getString(R.string.grades_management), getString(R.string.grades_down_title), Build.VERSION.SDK_INT >= 19 ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS, gradesFileName, openGradesFile);
        }
    }

    @NonNull
    private final
    BroadcastReceiver openGradesFile = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            openGradesFile();
            unregisterReceiver(this);
        }
    };

    @SuppressWarnings("deprecation")
    private void openGradesFile() {
//        String path = this.getExternalFilesDir(Build.VERSION.SDK_INT >= 19 ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS) + File.separator + gradesFileName;
        String path = Environment.getExternalStoragePublicDirectory(Build.VERSION.SDK_INT >= 19 ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS) + File.separator + gradesFileName;
        File file = new File(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        intent.setDataAndType(fileUri, "application/vnd.ms-excel");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, this.getString(R.string.grades_no_app), Toast.LENGTH_LONG).show();
            if (!startApp(External_Const.excel_packageNames)) {
                if (!openAppInStore(External_Const.excel_packageNames))
                    //Open Browser to Download
                    tabIntent(External_Const.downloadApp_libreoffice);
            }
        }
    }


    //Language
    @Override
    protected void attachBaseContext(Context newBase) {
//        newBase = LocaleChanger.configureBaseContext(newBase);
        super.attachBaseContext(newBase);
    }


    //Register Installation
    protected void checkRegistration() {
        if (PreferenceUtil.isPhoneRegistered() || !ApplicationFeatures.isNetworkAvailable())
            return;

        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.title(R.string.registration_dialog_title);

        // Set up the input
        builder.content(R.string.registration_dialog_content);

        // Set up the buttons
        builder.onPositive((MaterialDialog dialog, DialogAction which) -> {
            dialog.dismiss();
            register(getContext());
        });

        builder.onNegative((MaterialDialog dialog, DialogAction which) -> dialog.dismiss());

        builder.onNeutral((MaterialDialog dialog, DialogAction which) -> {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            editor.putBoolean("registered", true);
            editor.apply();
            dialog.dismiss();
        });

        builder.positiveText(R.string.yes);
        builder.negativeText(R.string.no);
        builder.neutralText(R.string.dont_show_again);
        builder.negativeColor(ApplicationFeatures.getAccentColor(getContext()));
        builder.positiveColor(ApplicationFeatures.getAccentColor(getContext()));
        builder.neutralColor(ApplicationFeatures.getAccentColor(getContext()));
        builder.build().show();


    }

    private void register(@NonNull Context context) {
        try {
            WebView wv = new WebView(this);
            wv.getSettings().setJavaScriptEnabled(true);
            wv.loadUrl(External_Const.REGISTER_URL);
        } catch (Exception e) {
            Toast.makeText(this, R.string.noInternetConnection, Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("registered", true);
        editor.apply();
    }


    //For Maps Navigation
    protected void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e2) {
                ChocoBar.builder().setActivity(this).setText(getString(R.string.no_navigation_app)).setDuration(ChocoBar.LENGTH_LONG).red().show();
            }
        }
    }
}
