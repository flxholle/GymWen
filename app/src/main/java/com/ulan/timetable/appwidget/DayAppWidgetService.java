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

package com.ulan.timetable.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.NonNull;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.substitutionplan.SubstitutionList;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlan;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures;
import com.ulan.timetable.appwidget.Dao.AppWidgetDao;
import com.ulan.timetable.databaseUtils.DbHelper;
import com.ulan.timetable.model.Week;
import com.ulan.timetable.utils.PreferenceUtil;
import com.ulan.timetable.utils.WeekUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.ulan.timetable.utils.NotificationUtil.getCurrentDay;

/**
 * From https://github.com/SubhamTyagi/TimeTable
 */
public class DayAppWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new DayAppWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class DayAppWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context mContext;
    private ArrayList<Week> content;
    private final int mAppWidgetId;

    DayAppWidgetRemoteViewsFactory(Context context, Intent intent) {
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        mContext = context;
    }

    @Override
    public void onCreate() {
        ProfileManagement.initProfiles();
        SubstitutionPlan substitutionPlan = SubstitutionPlanFeatures.createTempSubstitutionplan(false, ProfileManagement.getProfile(ProfileManagement.loadPreferredProfilePosition()).getCoursesArray());

        long currentTime = AppWidgetDao.getAppWidgetCurrentTime(mAppWidgetId, System.currentTimeMillis(), mContext);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        calendar.setTime(removeTime(calendar.getTime()));

        try {
            if (!PreferenceUtil.isTimeTableSubstitution())
                throw new Exception();

            SubstitutionList substitutionlist;
            if (!substitutionPlan.getToday().getNoInternet()) {
                DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                Date startDate = null;
                try {
                    startDate = df.parse(substitutionPlan.getTodayTitle().getDate());
                } catch (Exception ignore) {
                }
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(removeTime(startDate));

                Date startDate2 = null;
                try {
                    startDate2 = df.parse(substitutionPlan.getTomorrowTitle().getDate());
                } catch (Exception ignore) {
                }
                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime(removeTime(startDate2));

                if (calendar1.getTimeInMillis() == calendar.getTimeInMillis())
                    substitutionlist = substitutionPlan.getTodaySummarized();
                else if (calendar2.getTimeInMillis() == calendar.getTimeInMillis())
                    substitutionlist = substitutionPlan.getTomorrowSummarized();
                else
                    substitutionlist = new SubstitutionList(true);
            } else
                substitutionlist = new SubstitutionList(true);

            DbHelper db = new DbHelper(mContext);
            content = WeekUtils.compareSubstitutionAndWeeks(mContext, db.getWeek(getCurrentDay(calendar.get(Calendar.DAY_OF_WEEK))),
                    substitutionlist, ProfileManagement.getProfile(ProfileManagement.loadPreferredProfilePosition()).isSenior(), db);
        } catch (Exception ignore) {
            content = new DbHelper(mContext).getWeek(getCurrentDay(calendar.get(Calendar.DAY_OF_WEEK)));
        }
    }

    @Override
    public void onDataSetChanged() {
        onCreate();
    }

    @Override
    public void onDestroy() {
        content.clear();
    }

    @Override
    public int getCount() {
        return content.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.timetable_item_day_appwidget);
        Week week = content.get(position);

//        String lessons = getLessons(content, mContext);
        if (week != null) {
            String time;
            if (PreferenceUtil.showTimes(mContext))
                time = week.getFromTime() + " - " + week.getToTime();
            else {
                int start = WeekUtils.getMatchingScheduleBegin(week.getFromTime(), PreferenceUtil.getStartTime(mContext), PreferenceUtil.getPeriodLength(mContext));
                int end = WeekUtils.getMatchingScheduleEnd(week.getToTime(), PreferenceUtil.getStartTime(mContext), PreferenceUtil.getPeriodLength(mContext));
                if (start == end) {
                    time = start + ". " + mContext.getString(R.string.lesson);
                } else {
                    time = start + ".-" + end + ". " + mContext.getString(R.string.lesson);
                }
            }

            String text = week.getSubject() + ": " + time + ", " + week.getRoom() + " (" + week.getTeacher() + ")";
            rv.setTextViewText(R.id.widget_text, text);
        }

        //Set OpenApp Button intent
        Intent intent = new Intent();
        intent.putExtra("keyData", position);
        rv.setOnClickFillInIntent(R.id.widget_linear, intent);
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * @param date Date
     * @return param Date with removed time (only the day).
     */
    @NonNull
    private static Date removeTime(@NonNull Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}