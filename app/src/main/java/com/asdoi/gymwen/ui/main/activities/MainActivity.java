package com.asdoi.gymwen.ui.main.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.lehrerliste.Lehrerliste;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.receivers.AlarmReceiver;
import com.asdoi.gymwen.ui.main.fragments.ColoRushFragment;
import com.asdoi.gymwen.ui.main.fragments.LehrerlisteFragment;
import com.asdoi.gymwen.ui.main.fragments.VertretungFragment;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;
import com.github.javiersantos.appupdater.enums.Display;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActivityFeatures implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    public static int vertretungFragmentState;
    public static int lastLoaded; // 0 = Vertretung, 1 = Tabs, 2 = Lehrerliste
    public static final int lastLoadedVertretung = 0;
    public static final int lastLoadedTabs = 1;
    public static final int lastLoadedLehrerliste = 2;

    public static int lastLoadedInTabs;
    public static final int lastLoadedTabsSpecific = 10;
    public static final int lastLoadedTabsAll = 11;

    public static final int refreshFragment = 104;

    public SectionsPagerAdapter sectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

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

        navigationView.setNavigationItemSelectedListener(this);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), false, new String[]{getString(R.string.today), getString(R.string.tomorrow)});
        VertretungFragment.changedSectionsPagerAdapterTitles = false;
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);
        navView.setOnNavigationItemSelectedListener((MenuItem item) -> {
            onNavigationItemSelected(item.getItemId());
            return true;
        });

        if (!ApplicationFeatures.initSettings(false, true)) {
            finish();
            return;
        }

        ApplicationFeatures.sendNotification();

        initSpinner();

        if (!VertretungsPlanFeatures.isUninit())
            onNavigationItemSelected(R.id.nav_both);
        toggle.syncState();

        lastLoadedInTabs = lastLoadedTabsSpecific;

        checkUpdates(Display.DIALOG, false);
        showChangelogCK(true);

        if (!ApplicationFeatures.isAlarmOn()) {
            ApplicationFeatures.cancelAlarm(getContext(), AlarmReceiver.class);
        }

        //Enable disabled Views
        Menu menu = navigationView.getMenu();
        ArrayList<MenuItem> itemsEnable = new ArrayList<>(0);
        ArrayList<MenuItem> itemsDisable = new ArrayList<>(0);

        if (ApplicationFeatures.isBetaEnabled()) {
        } else {
        }
        if (ApplicationFeatures.isSections()) {
            itemsEnable.add(menu.findItem(R.id.nav_filtered_days));
            itemsEnable.add(menu.findItem(R.id.nav_unfiltered_days));
            itemsDisable.add(menu.findItem(R.id.nav_days));
        } else {
            itemsDisable.add(menu.findItem(R.id.nav_filtered_days));
            itemsDisable.add(menu.findItem(R.id.nav_unfiltered_days));
            itemsEnable.add(menu.findItem(R.id.nav_days));
        }
        if (ApplicationFeatures.isParents()) {
            itemsEnable.add(menu.findItem(R.id.nav_claxss));
            itemsEnable.add(menu.findItem(R.id.nav_forms));
            itemsDisable.add(menu.findItem(R.id.nav_mebis));
            itemsDisable.add(menu.findItem(R.id.nav_timetable));
            itemsDisable.add(menu.findItem(R.id.nav_grades));
        } else {
            itemsDisable.add(menu.findItem(R.id.nav_claxss));
            itemsDisable.add(menu.findItem(R.id.nav_forms));
            itemsEnable.add(menu.findItem(R.id.nav_mebis));
            itemsEnable.add(menu.findItem(R.id.nav_timetable));
            itemsEnable.add(menu.findItem(R.id.nav_grades));
        }

        try {
            for (MenuItem i : itemsEnable) {
                i.setEnabled(true);
                i.setVisible(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (MenuItem i : itemsDisable) {
                i.setEnabled(false);
                i.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        saveDocs();
        super.onPause();
    }

    private void initSpinner() {
        //Set Profiles
        Spinner parentSpinner = findViewById(R.id.main_profile_spinner);

        if (ProfileManagement.isMoreThanOneProfile()) {
            parentSpinner.setVisibility(View.VISIBLE);
            parentSpinner.setEnabled(true);
            List<String> list = ProfileManagement.getProfileList();
            list.add(getString(R.string.profiles_edit));
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            parentSpinner.setAdapter(dataAdapter);
            parentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String item = parent.getItemAtPosition(position).toString();
                    if (item.equals(getContext().getString(R.string.profiles_edit))) {
                        Intent intent = new Intent(getContext(), ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        ApplicationFeatures.initProfile(position, true);
                        onNavigationItemSelected(refreshFragment);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            parentSpinner.setSelection(0);
        } else {
            ApplicationFeatures.initProfile(0, true);
            parentSpinner.setVisibility(View.GONE);
            parentSpinner.setEnabled(false);
        }
    }

    private void setVisibiltySpinner(boolean visible) {
        if (!ProfileManagement.isMoreThanOneProfile())
            return;
        Spinner parentSpinner = findViewById(R.id.main_profile_spinner);
        if (visible)
            parentSpinner.setVisibility(View.VISIBLE);
        else
            parentSpinner.setVisibility(View.GONE);
    }

    @Override
    public void onPostCreate(Bundle b) {
        super.onPostCreate(b);
        try {
            findViewById(R.id.nav_header_main_icon).setOnClickListener((View v) -> {
                Intent intent = new Intent(this, WebsiteActivity.class);
//                intent.putExtra("url","gym-wen.de/information/unsere-schule/");
                startActivity(intent);
                drawer.closeDrawer(GravityCompat.START);
            });
        } catch (Exception e) {
        }
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
        NavController navController = Navigation.findNavController(this, R.id.fragment_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);

        onNavigationItemSelected(item.getItemId(), item.getTitle().toString());

        return true;
    }

    public void onNavigationItemSelected(int id) {
        onNavigationItemSelected(id, "");
    }

    public void onNavigationItemSelected(int id, String title) {
        // Handle navigation view item clicks here.
        Fragment fragment = null;
        Intent intent = null;

        switch (id) {
            default:
            case R.id.nav_both:
                setVisibiltySpinner(true);
                fragment = VertretungFragment.newInstance(VertretungFragment.Instance_Both);
                break;
            case R.id.nav_filtered_days:
                setVisibiltySpinner(true);
                findViewById(R.id.main_fab).setVisibility(View.GONE);
                findViewById(R.id.view_pager).setVisibility(View.VISIBLE);
                findViewById(R.id.tabs).setVisibility(View.VISIBLE);
                findViewById(R.id.fragment_main).setVisibility(View.GONE);
                findViewById(R.id.bottom_nav_view).setVisibility(View.GONE);
                sectionsPagerAdapter.setAll(false);
                sectionsPagerAdapter.notifyDataSetChanged();
                lastLoaded = lastLoadedTabs;
                break;
            case R.id.nav_unfiltered_days:
                setVisibiltySpinner(false);
                findViewById(R.id.main_fab).setVisibility(View.GONE);
                findViewById(R.id.view_pager).setVisibility(View.VISIBLE);
                findViewById(R.id.tabs).setVisibility(View.VISIBLE);
                findViewById(R.id.fragment_main).setVisibility(View.GONE);
                findViewById(R.id.bottom_nav_view).setVisibility(View.GONE);
                sectionsPagerAdapter.setAll(true);
                sectionsPagerAdapter.notifyDataSetChanged();
                lastLoaded = lastLoadedTabs;
                break;
            case R.id.nav_days:
                setVisibiltySpinner(true);
                findViewById(R.id.main_fab).setVisibility(View.GONE);
                findViewById(R.id.view_pager).setVisibility(View.VISIBLE);
                findViewById(R.id.tabs).setVisibility(View.VISIBLE);
                findViewById(R.id.fragment_main).setVisibility(View.GONE);
                findViewById(R.id.bottom_nav_view).setVisibility(View.VISIBLE);

                if (lastLoadedInTabs == lastLoadedTabsAll) {
                    //Navigation all
                    sectionsPagerAdapter.setAll(true);
                    sectionsPagerAdapter.notifyDataSetChanged();
                    lastLoaded = lastLoadedTabs;
                    break;
                }

            case R.id.navigation_filter:
                setVisibiltySpinner(true);
                sectionsPagerAdapter.setAll(false);
                sectionsPagerAdapter.notifyDataSetChanged();
                lastLoaded = lastLoadedTabs;
                lastLoadedInTabs = lastLoadedTabsSpecific;
                break;
            case R.id.navigation_all:
                setVisibiltySpinner(false);
                findViewById(R.id.view_pager).setVisibility(View.VISIBLE);
                findViewById(R.id.tabs).setVisibility(View.VISIBLE);
                findViewById(R.id.fragment_main).setVisibility(View.GONE);
                findViewById(R.id.bottom_nav_view).setVisibility(View.VISIBLE);
                sectionsPagerAdapter.setAll(true);
                sectionsPagerAdapter.notifyDataSetChanged();
                lastLoaded = lastLoadedTabsAll;
                lastLoadedInTabs = lastLoadedTabsAll;
                break;

            case R.id.action_settings: //Fallthrough
            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                finish();
                drawer.closeDrawer(GravityCompat.START);
                return;
            case R.id.nav_website:
                intent = new Intent(this, WebsiteActivity.class);
//                intent.putExtra("url","gym-wen.de/information/unsere-schule/");
                startActivity(intent);
                drawer.closeDrawer(GravityCompat.START);
                return;
            case R.id.nav_mebis:
                tabIntent("https://lernplattform.mebis.bayern.de/my/");
                return;
            case R.id.nav_mensa:
                String packageName = "de.eezzy.admin.apnr40";
                intent = getPackageManager().getLaunchIntentForPackage(packageName);
                if (intent == null) {
                    tabIntent("https://www.kitafino.de/sys_k2/index.php?action=bestellen");
                } else {
                    startActivity(intent);
                }
                return;
            case R.id.nav_shop:
                tabIntent("http://shop.apromote-werbemittel.de/");
                return;
            case R.id.nav_impressum:
            case R.id.action_impressum: // Fallthrough
                intent = new Intent(this, ImpressumActivity.class);
                startActivity(intent);
                drawer.closeDrawer(GravityCompat.START);
                return;
            case R.id.action_refresh2:
            case R.id.action_refresh:
                switch (lastLoaded) {
                    case lastLoadedVertretung:
                        VertretungsPlanFeatures.setDocs(null, null);
                        fragment = VertretungFragment.newInstance(vertretungFragmentState);
                        break;
                    case lastLoadedTabs:
                        VertretungsPlanFeatures.setDocs(null, null);
                        sectionsPagerAdapter.notifyDataSetChanged();
                        break;
                    case lastLoadedLehrerliste:
                        Lehrerliste.setDoc(null);
                        fragment = new LehrerlisteFragment();
                        break;
                    default:
                        VertretungsPlanFeatures.setDocs(null, null);
                        fragment = VertretungFragment.newInstance(VertretungFragment.Instance_Both);
                        break;
                }
                break;
            case refreshFragment:
                switch (lastLoaded) {
                    case lastLoadedVertretung:
                        fragment = VertretungFragment.newInstance(vertretungFragmentState);
                        break;
                    case lastLoadedTabs:
                        sectionsPagerAdapter.notifyDataSetChanged();
                        break;
                    case lastLoadedLehrerliste:
                        fragment = new LehrerlisteFragment();
                        break;
                    default:
                        fragment = VertretungFragment.newInstance(VertretungFragment.Instance_Both);
                        break;
                }
                break;
            case R.id.action_update:
                checkUpdates(Display.DIALOG, true);
                return;
            case R.id.action_changelog:
                showChangelogCK(false);
                return;
            case R.id.action_language:
                ApplicationFeatures.getLanguageSwitcher().showChangeLanguageDialog(this);
                return;
            case R.id.nav_teacherlist:
                setVisibiltySpinner(false);
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
                return;
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
                return;
            case R.id.nav_grades:
                /*packageName = "com.example.user.notendings";
                intent = getPackageManager().getLaunchIntentForPackage(packageName);
                //If app is not installed
                if (intent == null) {
                    //Open Browser to Download
                    tabIntent("https://github.com/Tebra/Android-Grades/blob/master/app/app-release.apk");
                } else {
                    startActivity(intent);nö
                }*/
                checkGradesFile();
                return;
            case R.id.nav_colorush:
                intent = getPackageManager().getLaunchIntentForPackage(ColoRushFragment.packageName);
                if (intent != null) {
                    startActivity(intent);
                    return;
                } else {
                    setVisibiltySpinner(false);
                    fragment = new ColoRushFragment();
                }
                break;
            case R.id.action_profiles:
                intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                finish();
                return;
            case R.id.nav_claxss:
                String link = "https://gym-wendelstein.schule-eltern.info/infoline/claxss";
                tabIntent(link);
                return;
            case R.id.nav_call_office:
                String nr = "0049 9171 818 800";
                makeCall(nr);
                return;
            case R.id.nav_public_transport:
                String packageNameOeffi = "de.schildbach.oeffi";
                String packageNameVGN = "com.mdv.VGNCompanion";
                String packageNameDB = "de.hafas.android.db";

                intent = getPackageManager().getLaunchIntentForPackage(packageNameOeffi) == null ? getPackageManager().getLaunchIntentForPackage(packageNameVGN) : getPackageManager().getLaunchIntentForPackage(packageNameOeffi);
                if (intent == null)
                    intent = getPackageManager().getLaunchIntentForPackage(packageNameDB);

                //If app is not installed
                if (intent == null) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageNameOeffi)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageNameVGN)));
                        } catch (android.content.ActivityNotFoundException a) {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageNameDB)));
                            } catch (android.content.ActivityNotFoundException a2) {
                                //Open Browser to Download
                                tabIntent("https://f-droid.org/de/packages/de.schildbach.oeffi/");
                            }
                        }
                    }
                } else {
                    startActivity(intent);
                }
                return;
            case R.id.nav_forms:
                link = "http://www.gym-wen.de/material/formulare-merkblaetter/";
                tabIntent(link);
                return;
        }


        if (fragment != null) {
            if (fragment instanceof LehrerlisteFragment)
                lastLoaded = lastLoadedLehrerliste;
            else
                lastLoaded = lastLoadedVertretung;

            //Display NavHost FragmentFeatures
            findViewById(R.id.view_pager).setVisibility(View.GONE);
            findViewById(R.id.tabs).setVisibility(View.GONE);
            findViewById(R.id.fragment_main).setVisibility(View.VISIBLE);
            findViewById(R.id.bottom_nav_view).setVisibility(View.GONE);

            //Set Fab
            if (fragment instanceof VertretungFragment) {
                FloatingActionButton fab = findViewById(R.id.main_fab);
                fab.setEnabled(true);
                fab.bringToFront();
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener((VertretungFragment) fragment);
            } else {
                FloatingActionButton fab = findViewById(R.id.main_fab);
                fab.setEnabled(false);
                fab.setVisibility(View.GONE);
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_main, fragment).commit();
        }

        if (!title.trim().isEmpty())
            getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onNavigationItemSelected(item.getItemId());
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
            saveDocs();
            finish();
            System.exit(1);
            android.os.Process.killProcess(android.os.Process.myPid());
        } else {
            Toast.makeText(getApplicationContext(), R.string.back_button, Toast.LENGTH_LONG).show();
            pressedBack = true;
            saveDocs();
        }
    }

    //Tabs
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        String[] tab_titles;
        boolean all;

        SectionsPagerAdapter(FragmentManager fm, boolean all, String[] titles) {
            super(fm);
            this.tab_titles = titles;
            setAll(all);
        }

        public void setTitles(String... titles) {
            this.tab_titles = titles;
        }

        void setAll(boolean v) {
            all = v;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            position++;
            return VertretungFragment.newInstance(all ? position + 2 : position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tab_titles[position];
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return tab_titles.length;
        }

        @Override
        public int getItemPosition(Object object) {
            ((VertretungFragment) object).update(all);

            //don'AndroidManifest.xml return POSITION_NONE, avoid fragment recreation.
            return super.getItemPosition(object);
        }
    }
}
