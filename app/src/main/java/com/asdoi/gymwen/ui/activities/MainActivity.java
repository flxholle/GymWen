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

package com.asdoi.gymwen.ui.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.service.quicksettings.TileService;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.asdoi.corona.Parser;
import com.asdoi.corona.model.LiveTicker;
import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.services.NotificationTileService;
import com.asdoi.gymwen.substitutionplan.MainSubstitutionPlan;
import com.asdoi.gymwen.ui.fragments.ColoRushFragment;
import com.asdoi.gymwen.ui.fragments.SubstitutionFragment;
import com.asdoi.gymwen.util.External_Const;
import com.asdoi.gymwen.util.PreferenceUtil;
import com.asdoi.gymwen.util.RSS_Feed;
import com.github.javiersantos.appupdater.enums.Display;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.NavigationViewUtil;
import com.pd.chocobar.ChocoBar;
import com.ulan.timetable.TimeTableBuilder;
import com.ulan.timetable.activities.NotesActivity;
import com.ulan.timetable.utils.ColorPalette;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends ActivityFeatures implements NavigationView.OnNavigationItemSelectedListener {
    public static final String SHORTCUT_ACTION_CALL_OFFICE = "com.asdoi.gymwen.ui.activities.MainActivity.CallOffice";
    public static final String SHORTCUT_ACTION_NAVIGATION = "com.asdoi.gymwen.ui.activities.MainActivity.Navigation";
    public static final String SHORTCUT_ACTION_NEWSPAPER = "com.asdoi.gymwen.ui.activities.MainActivity.News";
    public static final String SHORTCUT_ACTION_MEBIS = "com.asdoi.gymwen.ui.activities.MainActivity.Mebis";
    public static final String SHORTCUT_ACTION_MENSA = "com.asdoi.gymwen.ui.activities.MainActivity.Mensa";
    public static final String SHORTCUT_ACTION_GRADES_MANAGEMENT = "com.asdoi.gymwen.ui.activities.MainActivity.GradesManagement";
    public static final String SHORTCUT_ACTION_CLAXSS = "com.asdoi.gymwen.ui.activities.MainActivity.ClaXss";
    public static final String SHORTCUT_ACTION_FORMS = "com.asdoi.gymwen.ui.activities.MainActivity.Forms";
    public static final String SHORTCUT_ACTION_WEBSITE = "com.asdoi.gymwen.ui.activities.MainActivity.Website";

    public static final String LOADURL = "url";

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private Menu menu;

    private static int lastLoaded; // 0 = Substitution, 1 = Tabs, 2 = TeacherlistFeatures
    private static final int lastLoadedSubstitution = 0;
    private static final int lastLoadedTabs = 1;

    private static int lastLoadedInTabs;
    private static final int lastLoadedTabsSpecific = 10;
    private static final int lastLoadedTabsAll = 11;
    private static final int lastLoadedTabsToday = 12;
    private static final int lastLoadedTabsTomorrow = 13;

    private static final int refreshFragment = 104;

    public SectionsPagerAdapter sectionsPagerAdapter;

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setToolbar(false);

        if (!ApplicationFeatures.initSettings(false, true)) {
            finish();
            return;
        }

        ApplicationFeatures.deleteOfflineSubstitutionDocs();

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        drawer.addDrawerListener(toggle);
        navigationView.setNavigationItemSelectedListener(this);
        toggle.syncState();

        //Apply Shortcut functions
        Intent i = getIntent();
        if (i != null && i.getAction() != null) {
            switch (i.getAction()) {
                case SHORTCUT_ACTION_CALL_OFFICE:
                    onNavigationItemSelected(R.id.nav_call_office);
                    break;
                case SHORTCUT_ACTION_NAVIGATION:
                    onNavigationItemSelected(R.id.nav_navigation);
                    break;
                case SHORTCUT_ACTION_NEWSPAPER:
                    onNavigationItemSelected(R.id.nav_news);
                    break;
                case SHORTCUT_ACTION_MEBIS:
                    onNavigationItemSelected(R.id.nav_mebis);
                    break;
                case SHORTCUT_ACTION_MENSA:
                    onNavigationItemSelected(R.id.nav_mensa);
                    break;
                case SHORTCUT_ACTION_GRADES_MANAGEMENT:
                    onNavigationItemSelected(R.id.nav_grades);
                    break;
                case SHORTCUT_ACTION_CLAXSS:
                    onNavigationItemSelected(R.id.nav_claxss);
                    break;
                case SHORTCUT_ACTION_FORMS:
                    onNavigationItemSelected(R.id.nav_forms);
                    break;
                case SHORTCUT_ACTION_WEBSITE:
                    onNavigationItemSelected(R.id.nav_website);
                    break;
                case LOADURL:
                    tabIntent(i.getStringExtra(LOADURL));
                    break;
            }
            setIntent(null);
        }

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), true, true, new String[]{getString(R.string.today), getString(R.string.tomorrow)});
        SubstitutionFragment.changedSectionsPagerAdapterTitles = false;
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);
        navView.setOnNavigationItemSelectedListener((MenuItem item) -> {
            onNavigationItemSelected(item.getItemId());
            return true;
        });

        PreferenceUtil.setMainNotifForAllProfiles(PreferenceUtil.isParents());
        ApplicationFeatures.initSubstitutionPlanReceiver();
        ApplicationFeatures.sendNotifications();

        initSpinner();

        onNavigationItemSelected(R.id.nav_at_one_glance);
        navigationView.setCheckedItem(R.id.nav_at_one_glance);

        lastLoadedInTabs = lastLoadedTabsSpecific;

        if (!ApplicationFeatures.isNetworkAvailable()) {
            ChocoBar.builder().setActivity(this)
                    .setActionText(getString(R.string.ok))
                    .setText(getString(R.string.noInternetConnection))
                    .setDuration(5 * 1000)
                    .setIcon(R.drawable.ic_no_wifi)
                    .orange()
                    .show();
        }/* else
            showCoronaLiveTicker(true);*/

        setupMenuItems(navigationView);
        View headerview = navigationView.getHeaderView(0);
        headerview.findViewById(R.id.nav_header_main_settings).setOnClickListener((View v) -> onNavigationItemSelected(R.id.action_settings));
        headerview.findViewById(R.id.nav_header_main_settings).setOnLongClickListener((View v) -> {
            onNavigationItemSelected(R.id.action_imprint);
            return true;
        });
        headerview.findViewById(R.id.nav_header_main_icon).setOnClickListener((View v) -> {
//            Intent intent = new Intent(this, WebsiteActivity.class);
//            intent.putExtra("url", "gym-wen.de/information/unsere-schule/");
//            startActivity(intent);
            tabIntent(External_Const.homepage);
            drawer.closeDrawer(GravityCompat.START);
        });

        if (PreferenceUtil.isBackgroundUpdateCheck())
            checkUpdates(Display.DIALOG, false);
        showChangelog(true);
        checkRegistration();
        RSS_Feed.checkRSS(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            TileService.requestListeningState(this, new ComponentName(this, NotificationTileService.class));
        }
    }

    public void setupColors() {
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
        appBarLayout.setBackgroundColor(ApplicationFeatures.getPrimaryColor(this));

        findViewById(R.id.main_spinner_relative).setBackgroundColor(ApplicationFeatures.getPrimaryColor(this));

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setBackgroundColor(ApplicationFeatures.getPrimaryColor(this));
        tabs.setSelectedTabIndicatorColor(ApplicationFeatures.getAccentColor(this));
        if (Build.VERSION.SDK_INT >= 21)
            findViewById(R.id.main_fab).setBackgroundTintList(ColorStateList.valueOf(ApplicationFeatures.getAccentColor(this)));
        int accentColor = ThemeStore.accentColor(this);
        NavigationViewUtil.setItemIconColors(findViewById(R.id.nav_view), ThemeStore.textColorSecondary(this), accentColor);
        NavigationViewUtil.setItemTextColors(findViewById(R.id.nav_view), ThemeStore.textColorPrimary(this), accentColor);
    }

    private static void setupMenuItems(@NonNull NavigationView navigationView) {
        //Enable disabled Views
        Menu menu = navigationView.getMenu();

        ArrayList<MenuItem> itemsEnable = new ArrayList<>(0);
        ArrayList<MenuItem> itemsDisable = new ArrayList<>(0);

        if (PreferenceUtil.isFilteredUnfiltered()) {
            itemsEnable.add(menu.findItem(R.id.nav_filtered_days));
            itemsEnable.add(menu.findItem(R.id.nav_unfiltered_days));
            itemsDisable.add(menu.findItem(R.id.nav_days));

            if (PreferenceUtil.isFilteredMenu())
                itemsEnable.add(menu.findItem(R.id.nav_filtered_days));
            else
                itemsDisable.add(menu.findItem(R.id.nav_filtered_days));

            if (PreferenceUtil.isUnfilteredMenu())
                itemsEnable.add(menu.findItem(R.id.nav_unfiltered_days));
            else
                itemsDisable.add(menu.findItem(R.id.nav_unfiltered_days));

            itemsDisable.add(menu.findItem(R.id.nav_today_both));
            itemsDisable.add(menu.findItem(R.id.nav_tomorrow_both));
        } else if (PreferenceUtil.isMenuDays()) {
            itemsDisable.add(menu.findItem(R.id.nav_filtered_days));
            itemsDisable.add(menu.findItem(R.id.nav_unfiltered_days));
            itemsDisable.add(menu.findItem(R.id.nav_days));

            itemsEnable.add(menu.findItem(R.id.nav_today_both));
            itemsEnable.add(menu.findItem(R.id.nav_tomorrow_both));
        } else {
            //Show No Sections
            itemsDisable.add(menu.findItem(R.id.nav_filtered_days));
            itemsDisable.add(menu.findItem(R.id.nav_unfiltered_days));
            itemsDisable.add(menu.findItem(R.id.nav_today_both));
            itemsDisable.add(menu.findItem(R.id.nav_tomorrow_both));

            itemsEnable.add(menu.findItem(R.id.nav_days));
        }

        if (PreferenceUtil.isParents()) {
            itemsEnable.add(menu.findItem(R.id.nav_claxss));
            itemsEnable.add(menu.findItem(R.id.nav_forms));
            itemsDisable.add(menu.findItem(R.id.nav_mebis));
            itemsDisable.add(menu.findItem(R.id.nav_timetable));
            itemsDisable.add(menu.findItem(R.id.nav_grades));
            itemsDisable.add(menu.findItem(R.id.nav_colorush));
        } else {
            itemsDisable.add(menu.findItem(R.id.nav_claxss));
            itemsDisable.add(menu.findItem(R.id.nav_forms));
            itemsEnable.add(menu.findItem(R.id.nav_mebis));
            itemsEnable.add(menu.findItem(R.id.nav_timetable));
            itemsEnable.add(menu.findItem(R.id.nav_grades));
            itemsEnable.add(menu.findItem(R.id.nav_colorush));
        }

        if (PreferenceUtil.isAtOneGlanceMenu()) {
            itemsEnable.add(menu.findItem(R.id.nav_at_one_glance));
        } else
            itemsDisable.add(menu.findItem(R.id.nav_at_one_glance));

        if (PreferenceUtil.isTransportMenu())
            itemsEnable.add(menu.findItem(R.id.nav_public_transport));
        else
            itemsDisable.add(menu.findItem(R.id.nav_public_transport));

        if (PreferenceUtil.isNotesMenu())
            itemsEnable.add(menu.findItem(R.id.nav_notes));
        else
            itemsDisable.add(menu.findItem(R.id.nav_notes));

        if (PreferenceUtil.isTimetableMenu())
            itemsEnable.add(menu.findItem(R.id.nav_timetable));
        else
            itemsDisable.add(menu.findItem(R.id.nav_timetable));

        if (PreferenceUtil.isNews())
            itemsEnable.add(menu.findItem(R.id.nav_news));
        else
            itemsDisable.add(menu.findItem(R.id.nav_news));

        if (PreferenceUtil.isShop())
            itemsEnable.add(menu.findItem(R.id.nav_shop));
        else
            itemsDisable.add(menu.findItem(R.id.nav_shop));

        if (PreferenceUtil.isNavigation())
            itemsEnable.add(menu.findItem(R.id.nav_navigation));
        else
            itemsDisable.add(menu.findItem(R.id.nav_navigation));

        if (PreferenceUtil.isMensa())
            itemsEnable.add(menu.findItem(R.id.nav_mensa));
        else
            itemsDisable.add(menu.findItem(R.id.nav_mensa));

        if (PreferenceUtil.isPodcast())
            itemsEnable.add(menu.findItem(R.id.nav_podcast));
        else
            itemsDisable.add(menu.findItem(R.id.nav_podcast));

        //Enable items
        try {
            for (MenuItem i : itemsEnable) {
                i.setEnabled(true);
                i.setVisible(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Disable items
        try {
            for (MenuItem i : itemsDisable) {
                i.setEnabled(false);
                i.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTodayMenuItemTitle(String title) {
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_today_both).setTitle(title);
    }

    public void setTomorrowMenuItemTitle(String title) {
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_tomorrow_both).setTitle(title);
    }

    private void initSpinner() {
        //Set Profiles
        Spinner parentSpinner = findViewById(R.id.main_profile_spinner);

        if (ProfileManagement.isMoreThanOneProfile()) {
            parentSpinner.setVisibility(View.VISIBLE);
            List<String> list = ProfileManagement.getProfileListNames();
            list.add(getString(R.string.profiles_edit));
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            parentSpinner.setAdapter(dataAdapter);
            parentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
                    String item = parent.getItemAtPosition(position).toString();
                    if (item.equals(requireContext().getString(R.string.profiles_edit))) {
                        Intent intent = new Intent(requireContext(), ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        ApplicationFeatures.initProfile(position, true);
                        try {
                            onNavigationItemSelected(refreshFragment);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            parentSpinner.setSelection(ProfileManagement.loadPreferredProfilePosition());
        } else {
            ApplicationFeatures.initProfile(0, true);
            parentSpinner.setVisibility(View.GONE);
        }
    }

    private void setVisibilitySpinner(boolean visible) {
        if (!ProfileManagement.isMoreThanOneProfile())
            visible = false;
        Spinner parentSpinner = findViewById(R.id.main_profile_spinner);
        if (visible)
            parentSpinner.setVisibility(View.VISIBLE);
        else
            parentSpinner.setVisibility(View.GONE);
    }

    @Override
    public void onPostCreate(Bundle b) {
        super.onPostCreate(b);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
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

    /**
     * @param title The string through which the title in action should be replaced, empty if not
     *              Here the code is centralized, to be called from other methods, without creating a MenuItem or others
     */
    @SuppressLint("NonConstantResourceId")
    public void onNavigationItemSelected(int id, @NonNull String title) {
        // Handle navigation view item clicks here.
        Fragment fragment = null;
        Intent intent;

        switch (id) {
            default:
            case R.id.nav_at_one_glance:
                setVisibilitySpinner(true);
                fragment = SubstitutionFragment.newInstance(SubstitutionFragment.Instance_AtOneGlance);
                setDesignChangerVisibility(true);
                break;
            case R.id.nav_filtered_days:
                setVisibilitySpinner(true);
                findViewById(R.id.main_fab).setVisibility(View.VISIBLE);
                findViewById(R.id.view_pager).setVisibility(View.VISIBLE);
                findViewById(R.id.tabs).setVisibility(View.VISIBLE);
                findViewById(R.id.fragment_main).setVisibility(View.GONE);
                findViewById(R.id.bottom_nav_view).setVisibility(View.GONE);
                sectionsPagerAdapter.setAll(false);
                sectionsPagerAdapter.notifyDataSetChanged();
                lastLoaded = lastLoadedTabs;
                setDesignChangerVisibility(true);
                break;
            case R.id.nav_unfiltered_days:
                setVisibilitySpinner(false);
                findViewById(R.id.main_fab).setVisibility(View.GONE);
                findViewById(R.id.view_pager).setVisibility(View.VISIBLE);
                findViewById(R.id.tabs).setVisibility(View.VISIBLE);
                findViewById(R.id.fragment_main).setVisibility(View.GONE);
                findViewById(R.id.bottom_nav_view).setVisibility(View.GONE);
                sectionsPagerAdapter.setAll(true);
                sectionsPagerAdapter.notifyDataSetChanged();
                lastLoaded = lastLoadedTabs;
                setDesignChangerVisibility(false);
                break;
            case R.id.nav_days:
                setVisibilitySpinner(true);
                findViewById(R.id.main_fab).setVisibility(View.VISIBLE);
                findViewById(R.id.view_pager).setVisibility(View.VISIBLE);
                findViewById(R.id.tabs).setVisibility(View.VISIBLE);
                findViewById(R.id.fragment_main).setVisibility(View.GONE);
                findViewById(R.id.bottom_nav_view).setVisibility(View.VISIBLE);
                setDesignChangerVisibility(true);

                if (lastLoadedInTabs == lastLoadedTabsAll) {
                    //Navigation all
                    sectionsPagerAdapter.setAll(true);
                    setVisibilitySpinner(false);
                    sectionsPagerAdapter.notifyDataSetChanged();
                    lastLoaded = lastLoadedTabs;
                    findViewById(R.id.main_fab).setVisibility(View.GONE);
                    break;
                }

            case R.id.navigation_filter:
                findViewById(R.id.main_fab).setVisibility(View.VISIBLE);
                setVisibilitySpinner(true);
                sectionsPagerAdapter.setAll(false);
                sectionsPagerAdapter.notifyDataSetChanged();
                lastLoaded = lastLoadedTabs;
                lastLoadedInTabs = lastLoadedTabsSpecific;
                setDesignChangerVisibility(true);
                break;
            case R.id.navigation_all:
                setVisibilitySpinner(false);
                findViewById(R.id.main_fab).setVisibility(View.GONE);
                findViewById(R.id.view_pager).setVisibility(View.VISIBLE);
                findViewById(R.id.tabs).setVisibility(View.VISIBLE);
                findViewById(R.id.fragment_main).setVisibility(View.GONE);
                findViewById(R.id.bottom_nav_view).setVisibility(View.VISIBLE);
                sectionsPagerAdapter.setAll(true);
                sectionsPagerAdapter.notifyDataSetChanged();
                lastLoaded = lastLoadedTabsAll;
                lastLoadedInTabs = lastLoadedTabsAll;
                setDesignChangerVisibility(false);
                break;
            case R.id.nav_today_both:
                setVisibilitySpinner(true);
                findViewById(R.id.main_fab).setVisibility(View.VISIBLE);
                findViewById(R.id.view_pager).setVisibility(View.VISIBLE);
                findViewById(R.id.tabs).setVisibility(View.VISIBLE);
                findViewById(R.id.fragment_main).setVisibility(View.GONE);
                findViewById(R.id.bottom_nav_view).setVisibility(View.GONE);
                sectionsPagerAdapter.setDay(true);
                sectionsPagerAdapter.setTitles(getString(R.string.menu_filtered), getString(R.string.menu_unfiltered));
                sectionsPagerAdapter.notifyDataSetChanged();
                lastLoaded = lastLoadedTabs;
                lastLoadedInTabs = lastLoadedTabsToday;
                setDesignChangerVisibility(true);
                break;
            case R.id.nav_tomorrow_both:
                setVisibilitySpinner(true);
                findViewById(R.id.main_fab).setVisibility(View.VISIBLE);
                findViewById(R.id.view_pager).setVisibility(View.VISIBLE);
                findViewById(R.id.tabs).setVisibility(View.VISIBLE);
                findViewById(R.id.fragment_main).setVisibility(View.GONE);
                findViewById(R.id.bottom_nav_view).setVisibility(View.GONE);
                sectionsPagerAdapter.setDay(false);
                sectionsPagerAdapter.setTitles(getString(R.string.menu_filtered), getString(R.string.menu_unfiltered));
                sectionsPagerAdapter.notifyDataSetChanged();
                lastLoaded = lastLoadedTabs;
                lastLoadedInTabs = lastLoadedTabsTomorrow;
                setDesignChangerVisibility(true);
                break;
            case R.id.action_settings: //Fallthrough
            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                finish();
                drawer.closeDrawer(GravityCompat.START);
                return;
            case R.id.nav_website:
//                intent = new Intent(this, WebsiteActivity.class);
//                intent.putExtra("url","gym-wen.de/information/unsere-schule/");
//                startActivity(intent);
                tabIntent(External_Const.homepage);
                drawer.closeDrawer(GravityCompat.START);
                return;
            case R.id.nav_mebis:
                boolean appAvailable = isAppAvailable(External_Const.mebis_packageName);
                if (PreferenceUtil.showMebisDialog() && !appAvailable) {
                    new MaterialDialog.Builder(requireContext())
                            .title(R.string.download_mebis_app)
                            .content(R.string.download_mebis_app_msg)
                            .positiveText(R.string.yes)
                            .onPositive((dialog, which) -> {
                                if (!startApp(External_Const.mebis_packageName)) {
                                    startDownload(External_Const.mebis_download_link, "Mebis App", requireContext().getString(R.string.download_mebis_app_title), Environment.DIRECTORY_DOWNLOADS, "mebis.apk", new installApk("mebis.apk"));
                                }
                            })
                            .negativeText(R.string.no)
                            .onNegative((dialog, which) -> tabIntent(External_Const.mebis_Link))
                            .neutralText(R.string.dont_show_again)
                            .onNeutral((dialog, which) -> {
                                PreferenceUtil.setShowMebisDialog(false);
                                tabIntent(External_Const.mebis_Link);
                            })
                            .show();
                } else if (appAvailable) {
                    startApp(External_Const.mebis_packageName);
                } else
                    tabIntent(External_Const.mebis_Link);
                return;
//            case R.id.nav_backup:
//                backup();
//                return;
//            case R.id.nav_teams:
//                if (!startApp(External_Const.msTeams_packageName)) {
//                    Toast.makeText(this, this.getString(R.string.no_teams_app_installed), Toast.LENGTH_LONG).show();
//                    if (!openAppInStore(External_Const.msTeams_packageName))
//                        //Open Browser to Download
//                        tabIntent(External_Const.teamsAppOnline);
//                }
//                return;
            case R.id.nav_mensa:
                if (!startApp(External_Const.cafeteria_packageName)) {
                    tabIntent(External_Const.cafeteria_Link);
                }
                return;
            case R.id.nav_shop:
                tabIntent(External_Const.shop_Link);
                return;
            case R.id.nav_imprint:
            case R.id.action_imprint: // Fallthrough
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                drawer.closeDrawer(GravityCompat.START);
                return;
            case R.id.action_refresh:
                ApplicationFeatures.deleteOfflineSubstitutionDocs();
                switch (lastLoaded) {
                    case lastLoadedTabs:
                        sectionsPagerAdapter.notifyDataSetChanged();
                        break;
                    case lastLoadedSubstitution:
                    default:
                        fragment = SubstitutionFragment.newInstance(SubstitutionFragment.Instance_AtOneGlance);
                        break;
                }
                ApplicationFeatures.sendNotifications();
                break;
            case refreshFragment:
                switch (lastLoaded) {
                    case lastLoadedTabs:
                        sectionsPagerAdapter.notifyDataSetChanged();
                        break;
                    case lastLoadedSubstitution:
                    default:
                        fragment = SubstitutionFragment.newInstance(SubstitutionFragment.Instance_AtOneGlance);
                        break;
                }
                break;
            case R.id.action_update:
                checkUpdates(Display.DIALOG, true);
                return;
            case R.id.action_changelog:
                showChangelog(false);
                return;
            case R.id.nav_teacherlist:
                intent = new Intent(this, TeacherListActivity.class);
                startActivity(intent);
                return;
            case R.id.nav_notes:
                //If app is not installed
                if (!startApp(External_Const.notes_packageNames)) {
                   /* if (!openAppInStore(External_Const.notes_packageNames))
                        //Open Browser to Download
                        tabIntent(External_Const.downloadApp_notes);*/
                    new TimeTableBuilder(ProfileManagement.loadPreferredProfilePosition(), MainSubstitutionPlan.INSTANCE.getInstance(ProfileManagement.getProfile(ApplicationFeatures.getSelectedProfilePosition()).getCoursesArray())).start(this, NotesActivity.class);
                }
                return;
            case R.id.nav_timetable:
                /*if (!startApp(External_Const.timetable_packageNames)) {
                    if (!openAppInStore(External_Const.timetable_packageNames))
                        //Open Browser to Download
                        tabIntent(External_Const.downloadApp_timetable);
                }*/
                new TimeTableBuilder(ApplicationFeatures.getSelectedProfilePosition(), MainSubstitutionPlan.INSTANCE.getInstance(ProfileManagement.getProfile(ApplicationFeatures.getSelectedProfilePosition()).getCoursesArray())).start(this);
                return;
            case R.id.nav_grades:
                checkGradesFile();
                return;
            case R.id.nav_colorush:
                setDesignChangerVisibility(false);
                if (!startApp(External_Const.coloRush_packageNames)) {
                    setVisibilitySpinner(false);
                    fragment = new ColoRushFragment();
                }
                break;
            case R.id.action_profiles:
                intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                finish();
                return;
            case R.id.nav_claxss:
                tabIntent(External_Const.claxss_Link);
                return;
            case R.id.nav_call_office:
                makeCall(External_Const.office_TelNr);
                return;
            case R.id.nav_public_transport:
                if (!startApp(External_Const.publicTransport_packageNames)) {
                    if (!openAppInStore(External_Const.publicTransport_packageNames))
                        //Open Browser to Download
                        tabIntent(External_Const.downloadApp_publicTransport);
                }
                return;
            case R.id.nav_forms:
                tabIntent(External_Const.forms_Link);
                return;
            case R.id.action_switch_design:
                PreferenceUtil.changeDesign(this);
                onNavigationItemSelected(refreshFragment);
                return;
            case R.id.nav_news:
                tabIntent(External_Const.news_Link);
                return;
            case R.id.nav_navigation:
                Uri gymwenOnMap = Uri.parse(External_Const.location);
                showMap(gymwenOnMap);
                return;
            case R.id.nav_podcast:
                tabIntent(External_Const.podcast_Link);
                return;
            case R.id.nav_room_plan:
                intent = new Intent(this, RoomPlanActivity.class);
                startActivity(intent);
                return;
            case R.id.nav_holiday:
                importHolidays();
                return;
        }


        if (fragment != null) {
            lastLoaded = lastLoadedSubstitution;

            //Display NavHost Fragment
            findViewById(R.id.view_pager).setVisibility(View.GONE);
            findViewById(R.id.tabs).setVisibility(View.GONE);
            findViewById(R.id.fragment_main).setVisibility(View.VISIBLE);
            findViewById(R.id.bottom_nav_view).setVisibility(View.GONE);

            //Set Fab
            if (fragment instanceof SubstitutionFragment) {
                FloatingActionButton fab = findViewById(R.id.main_fab);
                fab.bringToFront();
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener((SubstitutionFragment) fragment);
            } else {
                FloatingActionButton fab = findViewById(R.id.main_fab);
                fab.setVisibility(View.GONE);
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_main, fragment).commit();
        }

        if (!title.trim().isEmpty())
            Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onNavigationItemSelected(item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    private static boolean pressedBack = false;

    @Override
    public void onBackPressed() {
        //If teacher is clicked
        if (findViewById(ApplicationFeatures.substitution_teacher_view_id) != null) {
            try {
                ((ViewGroup) findViewById(ApplicationFeatures.substitution_teacher_view_id).getParent()).removeView(findViewById(ApplicationFeatures.substitution_teacher_view_id));
            } catch (NullPointerException e) {
                findViewById(ApplicationFeatures.substitution_teacher_view_id).setVisibility(View.GONE);
            }
            return;
        }
        ApplicationFeatures.saveDocs();
        if (pressedBack) {
//            com.asdoi.gymwen.util.PreferenceUtil.setUnlockedPid(0);
            finishAffinity();
            pressedBack = false;
        } else {
            Toast.makeText(getApplicationContext(), R.string.back_button, Toast.LENGTH_LONG).show();
            pressedBack = true;
        }
    }

    private void setDesignChangerVisibility(boolean visible) {
        if (menu != null)
            menu.findItem(R.id.action_switch_design).setVisible(visible);
    }

    //Tabs
    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        String[] tab_titles;
        boolean all;
        boolean day;
        boolean today;

        SectionsPagerAdapter(@NonNull FragmentManager fm, boolean all, String[] titles) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.tab_titles = titles;
            setAll(all);
        }

        SectionsPagerAdapter(@NonNull FragmentManager fm, boolean day, boolean today, String[] titles) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.tab_titles = titles;
            setDay(today);
        }

        public void setTitles(String... titles) {
            this.tab_titles = titles;
        }

        void setAll(boolean v) {
            all = v;
            day = false;
            today = false;
        }

        void setDay(boolean today) {
            day = true;
            this.today = today;
            all = false;
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if (day) {
                if (position == 0) {
                    if (today)
                        return SubstitutionFragment.newInstance(SubstitutionFragment.Instance_Today);
                    else
                        return SubstitutionFragment.newInstance(SubstitutionFragment.Instance_Tomorrow);
                } else {
                    if (today)
                        return SubstitutionFragment.newInstance(SubstitutionFragment.Instance_Today_All);
                    else
                        return SubstitutionFragment.newInstance(SubstitutionFragment.Instance_Tomorrow_All);

                }
            } else if (all)
                return SubstitutionFragment.newInstance(position == 0 ? SubstitutionFragment.Instance_Today_All : SubstitutionFragment.Instance_Tomorrow_All, true);
            else
                return SubstitutionFragment.newInstance(position == 0 ? SubstitutionFragment.Instance_Today : SubstitutionFragment.Instance_Tomorrow, true);
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
        public int getItemPosition(@NotNull Object object) {
            if (day)
                ((SubstitutionFragment) object).updateDay(today);
            else
                ((SubstitutionFragment) object).update(all);

            //don't return POSITION_NONE, avoid fragment recreation.
            return super.getItemPosition(object);
        }
    }
}
