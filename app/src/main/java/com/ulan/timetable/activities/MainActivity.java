package com.ulan.timetable.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.ajts.androidmads.library.ExcelToSQLite;
import com.ajts.androidmads.library.SQLiteToExcel;
import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlan;
import com.asdoi.gymwen.ui.activities.ProfileActivity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.NavigationViewUtil;
import com.pd.chocobar.ChocoBar;
import com.ulan.timetable.TimeTableBuilder;
import com.ulan.timetable.adapters.FragmentsTabAdapter;
import com.ulan.timetable.databaseUtils.DBUtil;
import com.ulan.timetable.fragments.WeekdayFragment;
import com.ulan.timetable.receivers.DailyReceiver;
import com.ulan.timetable.utils.AlertDialogsHelper;
import com.ulan.timetable.utils.NotificationUtil;
import com.ulan.timetable.utils.PreferenceUtil;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import info.isuru.sheriff.enums.SheriffPermission;


public class MainActivity extends ActivityFeatures implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentsTabAdapter adapter;
    private ViewPager viewPager;
    private boolean switchSevenDays;

    private int profilePos = -1;
    @Nullable
    private SubstitutionPlan substitutionPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getIntent().getExtras() != null) {
                int themeId = getIntent().getExtras().getInt(TimeTableBuilder.CUSTOM_THEME, -1);
                if (themeId != -1) {
                    setTheme(themeId);
                }
            }
            profilePos = DBUtil.getProfilePosition(this);
            substitutionPlan = DBUtil.getSubstitutionPlanFromActivity(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.timetable_activity_main);
    }

    @Override
    public void onStart() {
        super.onStart();
        NotificationUtil.sendNotificationCurrentLesson(getContext(), false);
        PreferenceUtil.setDoNotDisturb(this, PreferenceUtil.doNotDisturbDontAskAgain());
        initAll();
        initSpinner();
    }

    @Override
    public void setupColors() {
        findViewById(R.id.main_spinner_relative).setBackgroundColor(ApplicationFeatures.getPrimaryColor(this));
        TabLayout tabs = findViewById(R.id.tabLayout);
        tabs.setBackgroundColor(ApplicationFeatures.getPrimaryColor(this));
        tabs.setSelectedTabIndicatorColor(ApplicationFeatures.getAccentColor(this));
        if (Build.VERSION.SDK_INT >= 21)
            findViewById(R.id.fab).setBackgroundTintList(ColorStateList.valueOf(ApplicationFeatures.getAccentColor(this)));
        int accentColor = ThemeStore.accentColor(this);
        NavigationViewUtil.setItemIconColors(findViewById(R.id.nav_view), ThemeStore.textColorSecondary(this), accentColor);
        NavigationViewUtil.setItemTextColors(findViewById(R.id.nav_view), ThemeStore.textColorPrimary(this), accentColor);
        findViewById(R.id.toolbar).setBackgroundColor(ApplicationFeatures.getPrimaryColor(this));
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
        appBarLayout.setBackgroundColor(ApplicationFeatures.getPrimaryColor(this));
    }

    private boolean dontfire = true;

    private void initSpinner() {
        //Set Profiles
        Spinner parentSpinner = findViewById(R.id.main_profile_spinner);

        if (ProfileManagement.isMoreThanOneProfile()) {
            parentSpinner.setVisibility(View.VISIBLE);
            parentSpinner.setEnabled(true);
            List<String> list = ProfileManagement.getProfileListNames();
            list.add(getString(R.string.profiles_edit));
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            parentSpinner.setAdapter(dataAdapter);
            dontfire = true;
            parentSpinner.setSelection(profilePos);
            parentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
                    if (dontfire) {
                        dontfire = false;
                        return;
                    }

                    String item = parent.getItemAtPosition(position).toString();
                    if (item.equals(getContext().getString(R.string.profiles_edit))) {
                        Intent intent = new Intent(getContext(), ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        new TimeTableBuilder(position, substitutionPlan).start(getContext());
                        finish();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } else {
            parentSpinner.setVisibility(View.GONE);
            parentSpinner.setEnabled(false);
        }
    }

    private void initAll() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerview = navigationView.getHeaderView(0);
        headerview.findViewById(R.id.nav_header_main_settings).setOnClickListener((View v) -> startActivity(new Intent(this, SettingsActivity.class)));
        TextView title = headerview.findViewById(R.id.nav_header_main_title);
        title.setText(R.string.timetable_activity_title);

        TextView desc = headerview.findViewById(R.id.nav_header_main_desc);
        desc.setText(R.string.timetable_credit);

        PreferenceManager.setDefaultValues(this, R.xml.timetable_settings, false);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        setupSevenDaysPref();
        setupFragments();
        setupCustomDialog();

        if (switchSevenDays) changeFragments(true);

        setDailyAlarm();
    }

    private void setupFragments() {
        adapter = new FragmentsTabAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        WeekdayFragment mondayFragment = new WeekdayFragment(WeekdayFragment.KEY_MONDAY_FRAGMENT);
        WeekdayFragment tuesdayFragment = new WeekdayFragment(WeekdayFragment.KEY_TUESDAY_FRAGMENT);
        WeekdayFragment wednesdayFragment = new WeekdayFragment(WeekdayFragment.KEY_WEDNESDAY_FRAGMENT);
        WeekdayFragment thursdayFragment = new WeekdayFragment(WeekdayFragment.KEY_THURSDAY_FRAGMENT);
        WeekdayFragment fridayFragment = new WeekdayFragment(WeekdayFragment.KEY_FRIDAY_FRAGMENT);

        int codeTod = -1;
        int codeTom = -1;
        if (substitutionPlan != null) {
            codeTod = substitutionPlan.getTodayTitle().getDayCode();
            codeTom = substitutionPlan.getTomorrowTitle().getDayCode();
        }

        switch (codeTod) {
            case Calendar.MONDAY:
                mondayFragment = new WeekdayFragment(substitutionPlan.getTodaySummarized(), substitutionPlan.getSenior(), WeekdayFragment.KEY_MONDAY_FRAGMENT);
                break;
            case Calendar.TUESDAY:
                tuesdayFragment = new WeekdayFragment(substitutionPlan.getTodaySummarized(), substitutionPlan.getSenior(), WeekdayFragment.KEY_TUESDAY_FRAGMENT);
                break;
            case Calendar.WEDNESDAY:
                wednesdayFragment = new WeekdayFragment(substitutionPlan.getTodaySummarized(), substitutionPlan.getSenior(), WeekdayFragment.KEY_WEDNESDAY_FRAGMENT);
                break;
            case Calendar.THURSDAY:
                thursdayFragment = new WeekdayFragment(substitutionPlan.getTodaySummarized(), substitutionPlan.getSenior(), WeekdayFragment.KEY_THURSDAY_FRAGMENT);
                break;
            case Calendar.FRIDAY:
                fridayFragment = new WeekdayFragment(substitutionPlan.getTodaySummarized(), substitutionPlan.getSenior(), WeekdayFragment.KEY_FRIDAY_FRAGMENT);
                break;
        }

        switch (codeTom) {
            case Calendar.MONDAY:
                mondayFragment = new WeekdayFragment(substitutionPlan.getTomorrowSummarized(), substitutionPlan.getSenior(), WeekdayFragment.KEY_MONDAY_FRAGMENT);
                break;
            case Calendar.TUESDAY:
                tuesdayFragment = new WeekdayFragment(substitutionPlan.getTomorrowSummarized(), substitutionPlan.getSenior(), WeekdayFragment.KEY_TUESDAY_FRAGMENT);
                break;
            case Calendar.WEDNESDAY:
                wednesdayFragment = new WeekdayFragment(substitutionPlan.getTomorrowSummarized(), substitutionPlan.getSenior(), WeekdayFragment.KEY_WEDNESDAY_FRAGMENT);
                break;
            case Calendar.THURSDAY:
                thursdayFragment = new WeekdayFragment(substitutionPlan.getTomorrowSummarized(), substitutionPlan.getSenior(), WeekdayFragment.KEY_THURSDAY_FRAGMENT);
                break;
            case Calendar.FRIDAY:
                fridayFragment = new WeekdayFragment(substitutionPlan.getTomorrowSummarized(), substitutionPlan.getSenior(), WeekdayFragment.KEY_FRIDAY_FRAGMENT);
                break;
        }

        adapter.addFragment(mondayFragment, getResources().getString(R.string.monday));
        adapter.addFragment(tuesdayFragment, getResources().getString(R.string.tuesday));
        adapter.addFragment(wednesdayFragment, getResources().getString(R.string.wednesday));
        adapter.addFragment(thursdayFragment, getResources().getString(R.string.thursday));
        adapter.addFragment(fridayFragment, getResources().getString(R.string.friday));

        viewPager.setAdapter(adapter);

        int day = getFragmentChoosingDay();
        viewPager.setCurrentItem(day == 1 ? 6 : day - 2, true);

        tabLayout.setupWithViewPager(viewPager);
    }

    private void changeFragments(boolean isChecked) {
        if (isChecked) {
            TabLayout tabLayout = findViewById(R.id.tabLayout);
            int day = getFragmentChoosingDay();
            adapter.addFragment(new WeekdayFragment(WeekdayFragment.KEY_SATURDAY_FRAGMENT), getResources().getString(R.string.saturday));
            adapter.addFragment(new WeekdayFragment(WeekdayFragment.KEY_SUNDAY_FRAGMENT), getResources().getString(R.string.sunday));
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(day == 1 ? 6 : day - 2, true);
            tabLayout.setupWithViewPager(viewPager);
        } else {
            if (adapter.getFragmentList().size() > 5) {
                adapter.removeFragment(new WeekdayFragment(WeekdayFragment.KEY_SATURDAY_FRAGMENT), 5);
                adapter.removeFragment(new WeekdayFragment(WeekdayFragment.KEY_SUNDAY_FRAGMENT), 5);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private int getFragmentChoosingDay() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        //If its after 18 o'clock, show the next day
        if (hour >= 18) {
            day++;
        }
        if (day > 7) { //Calender.Saturday
            day = day - 7; //1 = Calendar.Sunday, 2 = Calendar.Monday etc.
        }
        //If Saturday/Sunday are hidden, switch to Monday
        if (!switchSevenDays && (day == Calendar.SUNDAY || day == Calendar.SATURDAY)) {
            day = Calendar.MONDAY;
        }
        return day;
    }

    private void setupCustomDialog() {
        final View alertLayout = getLayoutInflater().inflate(R.layout.timetable_dialog_add_subject, null);
        AlertDialogsHelper.getAddSubjectDialog(MainActivity.this, alertLayout, adapter, viewPager);
    }

    private void setupSevenDaysPref() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        switchSevenDays = sharedPref.getBoolean(SettingsActivity.KEY_SEVEN_DAYS_SETTING, false);
    }

    private void setDailyAlarm() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Calendar cur = Calendar.getInstance();

        if (cur.after(calendar)) {
            calendar.add(Calendar.DATE, 1);
        }

        Intent myIntent = new Intent(this, DailyReceiver.class);
        int ALARM1_ID = 10000;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, ALARM1_ID, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timetable_main, menu);
        boolean integration = PreferenceUtil.isTimeTableSubstitution();
        if (integration)
            menu.findItem(R.id.action_substitutionIntegration).setTitle(R.string.integrate_substitution_in_timetable_on);
        else
            menu.findItem(R.id.action_substitutionIntegration).setTitle(R.string.integrate_substitution_in_timetable_off);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
            settings.putExtra(TimeTableBuilder.PROFILE_POS, profilePos);
            startActivity(settings);
            return true;
        } else if (item.getItemId() == R.id.action_substitutionIntegration) {
            boolean newValue = !PreferenceUtil.isTimeTableSubstitution();
            if (newValue)
                item.setTitle(R.string.integrate_substitution_in_timetable_on);
            else
                item.setTitle(R.string.integrate_substitution_in_timetable_off);
            PreferenceUtil.setTimeTableSubstitution(getContext(), newValue);
            recreate();
        } else if (item.getItemId() == R.id.action_timetable_backup) {
            backup();
        } else if (item.getItemId() == R.id.action_timetable_restore) {
            restore();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.exams) {
            Intent exams = new Intent(MainActivity.this, ExamsActivity.class);
            exams.putExtra(TimeTableBuilder.PROFILE_POS, profilePos);
            startActivity(exams);
        } else if (itemId == R.id.homework) {
            Intent homework = new Intent(MainActivity.this, HomeworksActivity.class);
            homework.putExtra(TimeTableBuilder.PROFILE_POS, profilePos);
            startActivity(homework);
        } else if (itemId == R.id.notes) {
            Intent note = new Intent(MainActivity.this, NotesActivity.class);
            note.putExtra(TimeTableBuilder.PROFILE_POS, profilePos);
            startActivity(note);
        } else if (itemId == R.id.settings) {
            Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settings);
        } else if (itemId == R.id.menu_main_app) {
            Intent settings = new Intent(MainActivity.this, com.asdoi.gymwen.ui.activities.MainActivity.class);
            startActivity(settings);
            finish();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private static final String filename = "Timetable_Backup.xls";

    @SuppressWarnings("deprecation")
    public void backup() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermission(this::backup, SheriffPermission.STORAGE);
            return;
        }

        String path = Environment.getExternalStoragePublicDirectory(Build.VERSION.SDK_INT >= 19 ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS).toString();
//        SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMdd");
//        Date myDate = new Date();
//        String filename = timeStampFormat.format(myDate);

        Activity activity = this;

        SQLiteToExcel sqliteToExcel = new SQLiteToExcel(this, DBUtil.getDBName(this), path);
        sqliteToExcel.exportAllTables(filename, new SQLiteToExcel.ExportListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onCompleted(String filePath) {
                runOnUiThread(() -> ChocoBar.builder().setActivity(activity)
                        .setText(getString(R.string.backup_successful, Build.VERSION.SDK_INT >= 19 ? getString(R.string.Documents) : getString(R.string.Downloads)))
                        .setDuration(ChocoBar.LENGTH_LONG)
                        .green()
                        .show());
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> ChocoBar.builder().setActivity(activity)
                        .setText(getString(R.string.backup_failed))
                        .setDuration(ChocoBar.LENGTH_LONG)
                        .red()
                        .show());
            }
        });
    }

    @SuppressWarnings("deprecation")
    public void restore() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermission(this::restore, SheriffPermission.STORAGE);
            return;
        }

        String path = Environment.getExternalStoragePublicDirectory(Build.VERSION.SDK_INT >= 19 ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + filename;
        File file = new File(path);
        if (!file.exists()) {
            ChocoBar.builder().setActivity(this)
                    .setText(getString(R.string.no_backup_found_in_downloads, Build.VERSION.SDK_INT >= 19 ? getString(R.string.Documents) : getString(R.string.Downloads)))
                    .setDuration(ChocoBar.LENGTH_LONG)
                    .red()
                    .show();
            return;
        }

        Activity activity = this;

        ExcelToSQLite excelToSQLite = new ExcelToSQLite(getApplicationContext(), DBUtil.getDBName(this), true);
        excelToSQLite.importFromFile(path, new ExcelToSQLite.ImportListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onCompleted(String filePath) {
                runOnUiThread(() -> ChocoBar.builder().setActivity(activity)
                        .setText(getString(R.string.import_successful))
                        .setDuration(ChocoBar.LENGTH_LONG)
                        .green()
                        .show());
                MainActivity.this.onStart();
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> ChocoBar.builder().setActivity(activity)
                        .setText(getString(R.string.import_failed))
                        .setDuration(ChocoBar.LENGTH_LONG)
                        .red()
                        .show());
            }
        });
    }
}
