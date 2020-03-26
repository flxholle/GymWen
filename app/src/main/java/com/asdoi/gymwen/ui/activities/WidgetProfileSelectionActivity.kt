package com.asdoi.gymwen.ui.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.asdoi.gymwen.ActivityFeatures
import com.asdoi.gymwen.R
import com.asdoi.gymwen.profiles.ProfileManagement

class WidgetProfileSelectionActivity : ActivityFeatures() {
    var appWidgetId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(AppCompatActivity.RESULT_CANCELED)

        setContentView(R.layout.activity_widget_profile_selection)

        // Find the widget id from the intent.
        appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If they gave us an intent without the widget id, just bail.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }

        if (ProfileManagement.isUninit())
            ProfileManagement.reload()

        val adapter = ArrayAdapter(this, R.layout.list_profiles_entry, ProfileManagement.getProfileList())

        val listView: ListView = findViewById(R.id.widget_creation_profile_list)
        listView.adapter = adapter

        listView.onItemClickListener = object : AdapterView.OnItemClickListener {

            override fun onItemClick(parent: AdapterView<*>, view: View,
                                     position: Int, id: Long) {

                // value of item that is clicked
                val itemValue = listView.getItemAtPosition(position) as String

                // Toast the values
                Toast.makeText(applicationContext,
                        "Position :$position\nItem Value : $itemValue", Toast.LENGTH_LONG)
                        .show()
            }
        }
    }

    override fun setupColors() {
        setToolbar(true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)

        /*  RemoteViews(context.packageName, R.layout.example_appwidget).also { views->
              appWidgetManager.updateAppWidget(appWidgetId, views)
          }*/

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}
