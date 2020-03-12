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

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_summary)


        //Set OnClick intent
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        remoteViews.setOnClickPendingIntent(R.id.widget_summary_layout, pendingIntent)

        //Set Button Image
        remoteViews.setImageViewBitmap(R.id.widget_summary_image, ApplicationFeatures.vectorToBitmap(R.drawable.ic_stat_assignment_late))

        remoteViews.setTextViewText(R.id.widget_summary_count, generateText())

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    private fun generateText(): String {
        var today = 0
        var tomorrow = 0

        if (!ProfileManagement.isLoaded())
            ProfileManagement.reload()
        for (p in ProfileManagement.getProfileList()) {
            val tempSubstitutionplan = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), p.courses.split(Profile.coursesSeparator).toTypedArray())
            val todayList = tempSubstitutionplan.getDay(true)
            today += todayList.size
            val tomorrowList = tempSubstitutionplan.getDay(false)
            tomorrow += tomorrowList.size
        }
        return "$today|$tomorrow"
    }

    companion object {
        const val SUMMARY_WIDGET_ID_KEY = "summarywidget"
    }
}

