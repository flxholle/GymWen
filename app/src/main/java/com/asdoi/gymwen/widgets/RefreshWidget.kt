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
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.asdoi.gymwen.ApplicationFeatures
import com.asdoi.gymwen.R
import java.util.*
import kotlin.concurrent.thread

/**
 * Implementation of App Widget functionality.
 */
const val WIDGET_REFRESH_KEY = "mywidgetrefreshid"

class RefreshWidget : AppWidgetProvider() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.hasExtra(WIDGET_REFRESH_KEY)) {
            val ids = intent.extras!!.getIntArray(WIDGET_REFRESH_KEY)
            thread(true) {
                ApplicationFeatures.downloadSubstitutionplanDocsAlways(true, true)
                ApplicationFeatures.sendNotifications(true)
                onUpdate(context!!, AppWidgetManager.getInstance(context), ids!!)
            }

        } else
            super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        // Construct the RemoteViews object
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_refresh)

        val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, RefreshWidget::class.java))
        val intent = Intent()
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(WIDGET_REFRESH_KEY, ids)
        val pendingIntent = PendingIntent.getBroadcast(context, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

        remoteViews.setOnClickPendingIntent(R.id.widget_refresh_image, pendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent)

        //Set Button Image
        remoteViews.setImageViewBitmap(R.id.widget_refresh_image, ApplicationFeatures.vectorToBitmap(R.drawable.ic_refresh_white_24dp))

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }
}

