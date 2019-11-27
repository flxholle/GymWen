package com.asdoi.gymwen.main;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
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
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.main.Fragments.VertretungFragment;
import com.asdoi.gymwen.vertretungsplanInternal.VertretungsPlan;
import com.github.javiersantos.appupdater.enums.Display;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

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

        if (!ApplicationFeatures.initSettings(false)) {
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

        ApplicationFeatures.proofeNotification();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onNavigationItemSelected(item);
        return super.onOptionsItemSelected(item);
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
}
