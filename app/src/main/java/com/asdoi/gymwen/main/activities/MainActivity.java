package com.asdoi.gymwen.main.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.main.fragments.LehrerlisteFragment;
import com.asdoi.gymwen.main.fragments.VertretungFragment;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;
import com.github.javiersantos.appupdater.enums.Display;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends ActivityFeatures implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    Fragment lastLoadedFragment = null;

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

        navigationView.setNavigationItemSelectedListener(this);

        if (!VertretungsPlanFeatures.isUninit())
            onNavigationItemSelected(navigationView.getMenu().getItem(0));
        toggle.syncState();

        if (!ApplicationFeatures.initSettings(false, true)) {
            finish();
        }
        checkUpdates(Display.DIALOG, false);
        showChangelogCK(true);

        Menu menu = navigationView.getMenu();
        if (ApplicationFeatures.isBetaEnabled()) {
            try {
                //Enable disabled Views
                MenuItem[] items = new MenuItem[]{menu.findItem(R.id.nav_grades)};
                for (MenuItem i : items) {
                    i.setEnabled(true);
                    i.setVisible(true);
                }
            } catch (Exception e) {

            }
        }

        if (ApplicationFeatures.isDateOff()) {
            try {
                //Enable disabled Views
                MenuItem[] items = new MenuItem[]{menu.findItem(R.id.nav_today), menu.findItem(R.id.nav_tomorrow)};
                for (MenuItem i : items) {
                    i.setEnabled(false);
                    i.setVisible(false);
                }
            } catch (Exception e) {

            }
        }

        if (ApplicationFeatures.isGesamtOff()) {
            try {
                //Enable disabled Views
                MenuItem[] items = new MenuItem[]{menu.findItem(R.id.nav_all_classes_today), menu.findItem(R.id.nav_all_classes_tomorrow)};
                for (MenuItem i : items) {
                    i.setEnabled(false);
                    i.setVisible(false);
                }
            } catch (Exception e) {

            }
        }

    }

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

        /*ModalBottomSheetDialogFragment.Builder mb = new ModalBottomSheetDialogFragment.Builder();
        mb.add(R.menu.menu_main).show(getSupportFragmentManager(), "options");
        mb.show(getSupportFragmentManager(),"s");*/
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
        Fragment fragment = null;
        Intent intent = null;
        String itemTitle = "" + item.getTitle();

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
                    tabIntent("https://www.kitafino.de/sys_k2/index.php?action=bestellen");
                } else {
                    startActivity(intent);
                }
                break;
            case R.id.nav_shop:
                tabIntent("http://shop.apromote-werbemittel.de/");
                break;
            case R.id.nav_impressum:
            case R.id.action_impressum: // Fallthrough
                intent = new Intent(this, ImpressumActivity.class);
                startActivity(intent);
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.action_refresh2:
            case R.id.action_refresh:
                item.setTitle(getSupportActionBar().getTitle());
                VertretungsPlanFeatures.setDocs(null, null);
                if (lastLoadedFragment == null)
                    fragment = new VertretungFragment(true);
                else {
                    if (lastLoadedFragment instanceof VertretungFragment) {
                        if (VertretungFragment.both)
                            fragment = new VertretungFragment(true);
                        else
                            fragment = new VertretungFragment(VertretungFragment.today, VertretungFragment.all);
                    } else {
                        fragment = new LehrerlisteFragment();
                    }
                }
                break;
            case R.id.action_update:
                checkUpdates(Display.DIALOG, true);
                break;
            case R.id.action_changelog:
                showChangelogCK(false);
                break;
            case R.id.nav_teacherlist:
                fragment = new LehrerlisteFragment();
                break;
            case R.id.nav_notes:
                packageName = "com.simplemobiletools.notes";
                String packageNamePro = "com.simplemobiletools.notes.pro";
                String samsungNotes = "com.samsung.android.app.notes";
                //Check the two notes versions
                intent = getPackageManager().getLaunchIntentForPackage(packageName) == null ? getPackageManager().getLaunchIntentForPackage(packageNamePro) : getPackageManager().getLaunchIntentForPackage(packageName);
                //Check Samsung notes
                if (intent == null)
                    intent = getPackageManager().getLaunchIntentForPackage(samsungNotes);

                //If app is not installed
                if (intent == null) {
                    try {
                        //Open Free Version
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        try {
                            //Open Pro Version
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageNamePro)));
                        } catch (android.content.ActivityNotFoundException a) {
                            //Open Browser to Download
                            tabIntent("https://f-droid.org/de/packages/com.simplemobiletools.notes.pro/");
                        }
                    }
                } else {
                    startActivity(intent);
                }
                break;
            case R.id.nav_timetable:
                packageName = "juliushenke.smarttt";
                intent = getPackageManager().getLaunchIntentForPackage(packageName);
                //If app is not installed
                if (intent == null) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                    } catch (android.content.ActivityNotFoundException a) {
                        //Open Browser to Download
                        tabIntent("https://apt.izzysoft.de/fdroid/index/apk/juliushenke.smarttt");
                    }
                } else {
                    startActivity(intent);
                }
                break;
            case R.id.nav_grades:
                packageName = "com.example.user.notendings";
                intent = getPackageManager().getLaunchIntentForPackage(packageName);
                //If app is not installed
                if (intent == null) {
                    //Open Browser to Download
                    tabIntent("https://github.com/Tebra/Android-Grades/blob/master/app/app-release.apk");
                } else {
                    startActivity(intent);
                }
                break;
            default:
                break;

        }

        lastLoadedFragment = fragment;

        if (fragment != null) {
            if (!item.getTitle().toString().trim().isEmpty())
                getSupportActionBar().setTitle(item.getTitle());
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
        }

        item.setTitle(itemTitle);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onNavigationItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    private static boolean pressedBack = false;

    @Override
    public void onBackPressed() {
        //If teacher is clicked
        if (findViewById(ApplicationFeatures.vertretung_teacher_view_id) != null) {
            try {
                ((ViewGroup) findViewById(ApplicationFeatures.vertretung_teacher_view_id).getParent()).removeView(findViewById(ApplicationFeatures.vertretung_teacher_view_id));
            } catch (NullPointerException e) {
                findViewById(ApplicationFeatures.vertretung_teacher_view_id).setVisibility(View.GONE);
            }
            return;
        }
        if (pressedBack) {
            finish();
            System.exit(1);
            android.os.Process.killProcess(android.os.Process.myPid());

        } else {
            Toast.makeText(getApplicationContext(), R.string.back_button, Toast.LENGTH_LONG).show();
            pressedBack = true;
        }
        VertretungsPlanFeatures.saveDocs();
    }
}
