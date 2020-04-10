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

package com.asdoi.gymwen.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.asdoi.gymwen.ApplicationFeatures
import com.asdoi.gymwen.R
import com.asdoi.gymwen.profiles.Profile
import com.asdoi.gymwen.profiles.ProfileManagement
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures
import com.asdoi.gymwen.ui.activities.MainActivity
import com.asdoi.gymwen.ui.activities.SubstitutionWidgetActivity
import com.asdoi.gymwen.util.PreferenceUtil
import kotlin.concurrent.thread

/**
 * Implementation of App Widget functionality.
 */

class SummaryWidget : AppWidgetProvider() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.hasExtra(SUMMARY_WIDGET_ID_KEY)) {
            val ids = intent.extras!!.getIntArray(SUMMARY_WIDGET_ID_KEY)
            onUpdate(context!!, AppWidgetManager.getInstance(context), ids!!)
        } else
            super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        thread(true) {
//            SubstitutionWidgetProvider.setColors(SubstitutionWidgetProvider.getThemeInt(context), context)
            ApplicationFeatures.downloadSubstitutionplanDocsAlways(true, true)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        const val SUMMARY_WIDGET_ID_KEY = "summarywidget"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_summary)

            val profilesPos = SubstitutionWidgetActivity.loadPref(context, appWidgetId)
            if (profilesPos.size == 0)
                profilesPos.add(0)

            var profiles = mutableListOf<Profile>()

            ProfileManagement.initProfiles()

            if (profilesPos != null && profilesPos.size > 0) {
                for (i in profilesPos) {
                    if (i < ProfileManagement.getSize()) {
                        profiles.add(ProfileManagement.getProfile(i))
                    }
                }
            }

            if (profiles.size == 0)
                profiles = ProfileManagement.getProfileList()


            //Set OnClick intent
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            remoteViews.setOnClickPendingIntent(R.id.widget_summary_layout, pendingIntent)

            //Set Button Image
            remoteViews.setImageViewBitmap(R.id.widget_summary_image, ApplicationFeatures.vectorToBitmap(R.drawable.ic_assignment_late))

            remoteViews.setTextViewText(R.id.widget_summary_count, generateText(profiles))

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }

        private fun generateText(profileList: List<Profile>): String {
            var today = 0
            var tomorrow = 0

            for (p in profileList) {
                val tempSubstitutionplan = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), p.courses.split(Profile.coursesSeparator).toTypedArray())
                val todayList = tempSubstitutionplan.getDay(true)
                todayList //No Internet
                today += todayList.entries.size
                val tomorrowList = tempSubstitutionplan.getDay(false)
                tomorrow += tomorrowList.entries.size
            }
            return "$today|$tomorrow"
        }
    }
}

