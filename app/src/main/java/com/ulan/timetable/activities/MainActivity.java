package com.ulan.timetable.activities;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlan;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.ulan.timetable.TimeTableBuilder;
import com.ulan.timetable.adapters.FragmentsTabAdapter;
import com.ulan.timetable.fragments.WeekdayFragment;
import com.ulan.timetable.utils.AlertDialogsHelper;
import com.ulan.timetable.utils.DBUtil;
import com.ulan.timetable.utils.DailyReceiver;
import com.ulan.timetable.utils.DoNotDisturbReceiversKt;
import com.ulan.timetable.utils.NotificationUtil;
import com.ulan.timetable.utils.PreferenceUtil;

import java.util.Calendar;


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
            substitutionPlan = DBUtil.getSubstitutionplanFromGSON(this);
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.timetable_activity_main);

        setDoNotDisturb();
        initAll();
        NotificationUtil.sendNotification(getContext(), false);
    }

    @Override
    public void setupColors() {
        TabLayout tabs = findViewById(R.id.tabLayout);
        tabs.setBackgroundColor(ApplicationFeatures.getPrimaryColor(this));
        tabs.setSelectedTabIndicatorColor(ApplicationFeatures.getAccentColor(this));
        if (Build.VERSION.SDK_INT >= 21)
            findViewById(R.id.fab).setBackgroundTintList(ColorStateList.valueOf(ApplicationFeatures.getAccentColor(this)));
        ((Toolbar) findViewById(R.id.toolbar)).setBackgroundColor(ApplicationFeatures.getPrimaryColor(this));
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
        appBarLayout.setBackgroundColor(ApplicationFeatures.getPrimaryColor(this));
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

        setupFragments();
        setupCustomDialog();
        setupSevenDaysPref();

        if (switchSevenDays) changeFragments(true);

        setDailyAlarm();
    }

    private void setupFragments() {
        adapter = new FragmentsTabAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

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
        viewPager.setCurrentItem(day == 1 ? 6 : day - 2, true);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void changeFragments(boolean isChecked) {
        if (isChecked) {
            TabLayout tabLayout = findViewById(R.id.tabLayout);
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK);
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

    private void setDoNotDisturb() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if the notification policy access has been granted for the app.
            if (!notificationManager.isNotificationPolicyAccessGranted() && !PreferenceUtil.doNotDisturbDontAskAgain()) {
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_do_not_disturb_on_black_24dp);
                try {
                    Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
                    DrawableCompat.setTint(wrappedDrawable, ApplicationFeatures.getTextColorPrimary(getContext()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                new MaterialDialog.Builder(getContext())
                        .title(R.string.permission_required)
                        .content(R.string.do_not_disturb_permission_desc)
                        .onPositive((dialog, which) -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            startActivity(intent);
                            finish();
                        })
                        .positiveText(R.string.permission_ok_button)
                        .negativeText(R.string.permission_cancel_button)
                        .onNegative(((dialog, which) -> dialog.dismiss()))
                        .icon(drawable)
                        .onNeutral(((dialog, which) -> PreferenceUtil.setDoNotDisturbDontAskAgain(getContext(), true)))
                        .neutralText(R.string.dont_show_again)
                        .show();
            } else {
                DoNotDisturbReceiversKt.setDoNotDisturbReceivers(getContext());
            }
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settings);
            return true;
        } else if (item.getItemId() == R.id.action_substitutionIntegration) {
            PreferenceUtil.setTimeTableSubstitution(getContext(), !PreferenceUtil.isTimeTableSubstitution());
            recreate();
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
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
