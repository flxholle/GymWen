package com.asdoi.gymwen.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import com.asdoi.gymwen.ActivityFeatures


class WidgetSelectProfileActivity : ActivityFeatures() {
    val TAG = "ExampleAppWidgetConfigure"
    private val PREFS_NAME = "com.example.android.apis.appwidget.ExampleAppWidgetProvider"
    private val PREF_PREFIX_KEY = "prefix_"
    var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    var mAppWidgetPrefix: EditText? = null

    override fun setupColors() {
        setToolbar(true)
    }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(AppCompatActivity.RESULT_CANCELED)
        // Set the view layout resource to use.
//        setContentView(R.layout.appwidget_configure)
        // Find the EditText
//        mAppWidgetPrefix = findViewById<View>(R.id.appwidget_prefix) as EditText
//        // Bind the action for the save button.
//        findViewById<View>(R.id.save_button).setOnClickListener(mOnClickListener)
        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId === AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }
    }

    // Write the prefix to the SharedPreferences object for this widget
    fun saveTitlePref(context: Context, appWidgetId: Int, text: String?) {
        val prefs: SharedPreferences.Editor = context.getSharedPreferences(PREFS_NAME, 0).edit()
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
        prefs.commit()
    }
}