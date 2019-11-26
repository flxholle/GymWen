package com.asdoi.gymwen.main;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.MainApplication;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.VertretungsplanInternal.VertretungsPlan;
import com.asdoi.gymwen.main.Fragments.VertretungFragment;
import com.github.javiersantos.appupdater.enums.Display;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

public class MainActivity extends ActivityFeatures implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private FloatingActionButton fab;
    VertretungFragment lastLoadedFragment = null;

    // Unique request code.
    private static final int WRITE_REQUEST_CODE = 43;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);


        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
//        supportActionBar?drawer.setDisplayHomeAsUpEnabled(true);
//        supportActionBar?.setHomeButtonEnabled(true);
        drawer.addDrawerListener(toggle);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_today, R.id.nav_tomorrow, R.id.nav_settings,
                R.id.nav_all_classes_today, R.id.nav_all_classes_tomorrow)
                .setDrawerLayout(drawer)
                .build();
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController)

        navigationView.setNavigationItemSelectedListener(this);

        fab = findViewById(R.id.main_fab);
//        VertretungsPlan.reloadDocs();
//        VertretungsPlan.refresh();

        if (!VertretungsPlan.isUninit())
            onNavigationItemSelected(navigationView.getMenu().getItem(0));
        toggle.syncState();
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        if (!MainApplication.initSettings(false)) {
            finish();
        }
        checkUpdates(Display.DIALOG, false);
    }

    public static boolean homepageFragment = false;
    private static boolean pressedBack = false;

    @Override
    public void onPostCreate(Bundle b) {
        super.onPostCreate(b);
        toggle.syncState();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);

        // Handle navigation view item clicks here.
        int id = item.getItemId();
        VertretungFragment fragment = null;
        Intent intent = null;

        switch (id) {
            case R.id.nav_both:
                fragment = new VertretungFragment(true);
                break;
            case R.id.nav_today:
                fragment = new VertretungFragment(true, false);
                break;
            case R.id.nav_all_classes_today:
                fragment = new VertretungFragment(true, true);
                break;
            case R.id.nav_tomorrow:
                fragment = new VertretungFragment(false, false);
                break;
            case R.id.nav_all_classes_tomorrow:
                fragment = new VertretungFragment(false, true);
                break;
            case R.id.action_settings: //Fallthrough
            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                finish();
                drawer.closeDrawer(GravityCompat.START);
                return true;
            case R.id.nav_website:
                intent = new Intent(this, WebsiteActivity.class);
//                intent.putExtra("url","gym-wen.de/information/unsere-schule/");
                startActivity(intent);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            case R.id.nav_mebis:
                tabIntent("https://lernplattform.mebis.bayern.de/my/");
                break;
            case R.id.nav_mensa:
                String packageName = "de.eezzy.admin.apnr40";
                intent = getPackageManager().getLaunchIntentForPackage(packageName);
                if (intent == null) {
//                    ProgressDialog progDailog = ProgressDialog.show(this, "Laden","Bitte warten...", true);
//                    progDailog.setCancelable(false);
                    tabIntent("https://www.kitafino.de/sys_k2/index.php?action=bestellen");
//                    progDailog.dismiss();
                } else {
                    startActivity(intent);
                }
                break;
            case R.id.action_impressum: // Fallthrough
//            case R.id.nav_impressum:
                intent = new Intent(this, ImpressumActivity.class);
                startActivity(intent);
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.action_refresh2:
            case R.id.action_refresh:
                VertretungsPlan.setDocs(null, null);
                if (lastLoadedFragment == null)
                    fragment = new VertretungFragment(true);
                else {
                    if (VertretungFragment.both)
                        fragment = new VertretungFragment(true);
                    else
                        fragment = new VertretungFragment(VertretungFragment.today, VertretungFragment.all);
                }
                item.setTitle("");
                break;
            /*case R.id.nav_gradesManagement:
//                createFile("text/plain","test.txt");
//                downloadFile("https://gitlab.com/asdoi/colorrush/raw/master/VersionFile.txt");
                Snackbar snackbar = Snackbar.make(findViewById(R.id.nav_host_fragment), "Dieses Feature ist noch nicht benutzbar", Snackbar.LENGTH_SHORT);
                snackbar.show();
                break;*/
            case R.id.action_update:
                checkUpdates(Display.SNACKBAR, true);
                break;
            default:
//                fragment = new All_Classes_today();
                break;

        }

        lastLoadedFragment = fragment;

        if (fragment != null) {
            if (!item.getTitle().toString().trim().isEmpty())
                getSupportActionBar().setTitle(item.getTitle());
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
        }

        MainApplication.proofeNotification();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       /* //Open Settings
        int id = item.getItemId();
        // ... Handle other options menu items.
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }*/
        onNavigationItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                String filePath = uri.toString();
                try {
                    OutputStream stream = getContentResolver().openOutputStream(uri);
                    PrintWriter writer = new PrintWriter(stream);
                    writer.write("test");
                    writer.flush();
                    stream.close();
                    openFile(uri);
                } catch (java.io.IOException e) {
                    Log.e(getLocalClassName(), "caught IOException", e);
                }
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (pressedBack) {
            finish();
            System.exit(1);
            android.os.Process.killProcess(android.os.Process.myPid());

        } else {
            Toast.makeText(getApplicationContext(), R.string.back_button, Toast.LENGTH_LONG).show();
            pressedBack = true;
        }
        VertretungsPlan.saveDocs();
    }

    //File Management for coming soon GradesManagement

    public void openFile(Uri uri) {
        String mimeType = "text/plain";
        Intent intent = new Intent();
        intent.setType(mimeType);
        intent.setAction(Intent.ACTION_VIEW); //Change if needed
        intent.setDataAndType(Uri.parse(uri.toString()), mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent/* Intent.createChooser(intent,"Notenverwaltung öffnen mit...")*/);
    }

    public long downloadFile(String url) {
        /*DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Some descrition");
        request.setTitle("Some title");
        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "name-of-the-file.ext");

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, filter);
        return manager.enqueue(request);*/

        File file = new File(getExternalFilesDir(null), "Dummy");

         /*
       Create a DownloadManager.Request with all the information necessary to start the download
        */
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle("Dummy File")// Title of the Download Notification
                .setDescription("Downloading")// Description of the Download Notification
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                .setDestinationUri(Uri.fromFile(file))// Uri of the destination file
                .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true);// Set if download is allowed on roaming network

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.
        return downloadID;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private void createFile(String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }

    public void saveFilePath(String path) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("filePath", path);
        editor.commit();
    }

    public String getFilePath() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString("filePath", "");
    }

    //Credits: https://stackoverflow.com/qusestions/54836448/android-opening-a-word-document-using-intents-and-fileprovider
    long downloadID;
    //File Management
    //https://developer.android.com/training/data-storage/files/external
    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Toast.makeText(MainActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
                System.out.println("downloaded");
            }
        }
    };
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            //check if the broadcast message is for our enqueued download
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (/*referenceId == 1*/true) {
                System.out.println("completed");
            }
        }
    };
}
