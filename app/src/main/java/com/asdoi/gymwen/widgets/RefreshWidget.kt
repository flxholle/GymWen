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
class RefreshWidget : AppWidgetProvider() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.hasExtra(WIDGET_REFRESH_KEY)) {
            val ids = intent.extras!!.getIntArray(WIDGET_REFRESH_KEY)
            onUpdate(context!!, AppWidgetManager.getInstance(context), ids!!)
        } else
            super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        thread(true) {
//            setColors(getThemeInt(context), context)
            ApplicationFeatures.downloadSubstitutionplanDocsAlways(true, true)
            ApplicationFeatures.sendNotification()
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

    companion object {
        const val WIDGET_REFRESH_KEY = "mywidgetrefreshid"
    }
}

