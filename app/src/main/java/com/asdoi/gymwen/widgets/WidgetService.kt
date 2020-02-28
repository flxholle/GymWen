package com.asdoi.gymwen.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.asdoi.gymwen.ApplicationFeatures
import com.asdoi.gymwen.R
import com.asdoi.gymwen.profiles.Profile
import com.asdoi.gymwen.profiles.ProfileManagement
import com.asdoi.gymwen.ui.fragments.VertretungFragment
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures

private const val nothing = -1
private const val day = -2
private const val profile = -3
private const val headline = -4
private const val content = -5
private const val internet = -6


class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return WidgetFactory(this.applicationContext)
    }
}


class WidgetFactory(val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var contentList: MutableList<EntryHelper> = mutableListOf()

    override fun onCreate() {
        reset()
        if (!ProfileManagement.isLoaded())
            ProfileManagement.reload()

        var noInternet = false

        val todayEntryList = mutableListOf<EntryHelper>()
        val tomorrowEntryList = mutableListOf<EntryHelper>()
        var today = ""
        var tomorrow = ""

        for (p in ProfileManagement.getProfileList()) {
            val tempVertretungsplan = VertretungsPlanFeatures.createTempVertretungsplan(ApplicationFeatures.isHour(), p.courses.split(Profile.coursesSeparator).toTypedArray())

            //Today
            today = tempVertretungsplan.getTitleString(true)
            val todayList = tempVertretungsplan.getDay(true)
            if (todayList == null) {
                noInternet = true
                break
            }
            todayEntryList.addAll(getEntryListForProfile(todayList, if (ProfileManagement.sizeProfiles() == 1) null else p.name, tempVertretungsplan.oberstufe))


            //Tomorrow
            tomorrow = tempVertretungsplan.getTitleString(true)
            val tomorrowList = tempVertretungsplan.getDay(true)
            if (tomorrowList == null) {
                noInternet = true
                break
            }
            tomorrowEntryList.addAll(getEntryListForProfile(tomorrowList, if (ProfileManagement.sizeProfiles() == 1) null else p.name, tempVertretungsplan.oberstufe))
        }

        if (noInternet) {
            contentList = mutableListOf(EntryHelper(arrayOf(), internet))
            return
        }

        contentList.add(EntryHelper(arrayOf(today), day))
        contentList.addAll(todayEntryList)
        contentList.add(EntryHelper(arrayOf(tomorrow), day))
        contentList.addAll(tomorrowEntryList)

    }

    private fun reset() {
        contentList = mutableListOf()
    }

    private fun getEntryListForProfile(dayList: Array<Array<String>>, name: String? = null, oberstufe: Boolean): List<EntryHelper> {
        val entryList = mutableListOf<EntryHelper>()
        if (name != null) entryList.add(EntryHelper(arrayOf(name), profile))

        val todaySonstiges: Boolean
        if (dayList.isEmpty())
            entryList.add(EntryHelper(arrayOf(), nothing))
        else {
            todaySonstiges = VertretungFragment.isSonstiges(dayList)
            entryList.add(EntryHelper(generateHeadline(context, true, oberstufe), headline, todaySonstiges, oberstufe))
            for (string in dayList) {
                entryList.add(EntryHelper(string, content, todaySonstiges))
            }
        }
        return entryList
    }

    override fun onDestroy() {
    }

    override fun getCount(): Int {
        return contentList.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val entry: EntryHelper = contentList.get(position)
        return when (entry.code) {
            day -> getTitleText(context, entry.entry[0])
            profile -> getTitleText(context, entry.entry[0])
            nothing -> getNothing(context, context.getString(R.string.nothing))
            headline -> getHeadline(entry.entry, entry.sonstiges)
            content -> getEntrySpecific(entry.entry, entry.oberstufe, entry.sonstiges)
            internet -> getTitleText(context, context.getString(R.string.noInternetConnection))
            else -> getTitleText(context, context.getString(R.string.noInternetConnection))
        }
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun onDataSetChanged() {
        onCreate()
    }

    class EntryHelper(val entry: Array<String>, val code: Int = nothing, val sonstiges: Boolean = false, val oberstufe: Boolean = false)


    //View creators

    //From VertretungFragment (from Java imported)
    private fun getTitleText(context: Context, text: String): RemoteViews {
        val view = getRemoteViews(context)
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewHour, View.GONE)
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewSubject, View.GONE)
        view.setTextViewText(R.id.vertretung_specific_entry_textViewTeacher, text)
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, 25f)
        view.setTextColor(R.id.vertretung_specific_entry_textViewTeacher, VertretungWidgetProvider.textColorPrimary)
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewRoom, View.GONE)
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewOther, View.GONE)
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewClass, View.GONE)
        //        view.setOnClickPendingIntent(R.id.widget_entry_linear, VertretungWidgetProvider.getPendingSelfIntent(context, VertretungWidgetProvider.WIDGET_ON_CLICK));
        return view
    }

    private fun getHeadline(headline: Array<String>, sonstiges: Boolean): RemoteViews {
        val view = getRemoteViews(context)
        val textSize = 17
        view.setTextViewText(R.id.vertretung_specific_entry_textViewHour, headline[0])
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewHour, TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
        view.setTextViewText(R.id.vertretung_specific_entry_textViewSubject, headline[1])
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewSubject, TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
        view.setTextViewText(R.id.vertretung_specific_entry_textViewTeacher, headline[2])
        view.setTextViewText(R.id.vertretung_specific_entry_textViewRoom, headline[3])
        view.setTextColor(R.id.vertretung_specific_entry_textViewRoom, VertretungWidgetProvider.textColorSecondary)
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewRoom, TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewOther, if (sonstiges) View.VISIBLE else View.GONE)
        view.setTextViewText(R.id.vertretung_specific_entry_textViewOther, headline[4])
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewOther, TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
        view.setTextViewText(R.id.vertretung_specific_entry_textViewClass, headline[5])
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewClass, TypedValue.COMPLEX_UNIT_SP, 10f)
        //        view.setOnClickPendingIntent(R.id.widget_entry_linear, VertretungWidgetProvider.getPendingSelfIntent(context, VertretungWidgetProvider.WIDGET_ON_CLICK));
        return view
    }

    private fun getEntrySpecific(entry: Array<String>, oberstufe: Boolean, sonstiges: Boolean): RemoteViews {
        val view = getRemoteViews(context)
        view.setTextViewText(R.id.vertretung_specific_entry_textViewHour, entry[1])
        view.setTextViewText(R.id.vertretung_specific_entry_textViewSubject, if (oberstufe) entry[0] else entry[2])
        if (!(entry[3] == "entfällt" || entry[3] == "entf")) {
            view.setTextViewText(R.id.vertretung_specific_entry_textViewTeacher, entry[3])
            view.setViewVisibility(R.id.vertretung_specific_entry_textViewRoom, View.VISIBLE)
            val content = SpannableString(entry[4])
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            view.setTextViewText(R.id.vertretung_specific_entry_textViewRoom, content)
        } else {
            val content = SpannableString(entry[3])
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            view.setTextViewText(R.id.vertretung_specific_entry_textViewTeacher, content)
            //            view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, 20);
            view.setTextColor(R.id.vertretung_specific_entry_textViewTeacher, ContextCompat.getColor(context, R.color.colorAccent))
            //            view.setViewVisibility(R.id.vertretung_specific_entry_textViewRoom, View.GONE);
        }
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewOther, if (sonstiges) View.VISIBLE else View.GONE)
        view.setTextViewText(R.id.vertretung_specific_entry_textViewOther, entry[5])
        view.setTextViewText(R.id.vertretung_specific_entry_textViewClass, if (oberstufe) entry[2] else entry[0])
        //        view.setOnClickPendingIntent(R.id.widget_entry_linear, VertretungWidgetProvider.getPendingSelfIntent(context, VertretungWidgetProvider.WIDGET_ON_CLICK));
        return view
    }

    private fun getRemoteViews(context: Context): RemoteViews {
        val view = RemoteViews(context.packageName, R.layout.widget_list_entry)
        resetView(view)
        return view
    }

    private fun getNothing(context: Context, text: String): RemoteViews {
        val view = getRemoteViews(context)
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewHour, View.GONE)
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewSubject, View.GONE)
        view.setTextViewText(R.id.vertretung_specific_entry_textViewTeacher, text)
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, 21f)
        view.setTextColor(R.id.vertretung_specific_entry_textViewTeacher, VertretungWidgetProvider.textColorSecondary)
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewRoom, View.GONE)
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewOther, View.GONE)
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewClass, View.GONE)
        //        view.setOnClickPendingIntent(R.id.widget_entry_linear, VertretungWidgetProvider.getPendingSelfIntent(context, VertretungWidgetProvider.WIDGET_ON_CLICK));
        return view
    }

    private fun resetView(view: RemoteViews) {
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewHour, View.VISIBLE)
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewHour, TypedValue.COMPLEX_UNIT_SP, 36f)
        view.setTextViewText(R.id.vertretung_specific_entry_textViewHour, "")
        view.setTextColor(R.id.vertretung_specific_entry_textViewHour, Color.WHITE)
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewSubject, View.VISIBLE)
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewSubject, TypedValue.COMPLEX_UNIT_SP, 18f)
        view.setTextViewText(R.id.vertretung_specific_entry_textViewSubject, "")
        view.setTextColor(R.id.vertretung_specific_entry_textViewSubject, VertretungWidgetProvider.textColorSecondary)
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewTeacher, View.VISIBLE)
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, 14f)
        view.setTextViewText(R.id.vertretung_specific_entry_textViewTeacher, "")
        view.setTextColor(R.id.vertretung_specific_entry_textViewTeacher, VertretungWidgetProvider.textColorSecondary)
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewRoom, View.VISIBLE)
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewRoom, TypedValue.COMPLEX_UNIT_SP, 24f)
        view.setTextViewText(R.id.vertretung_specific_entry_textViewRoom, "")
        view.setTextColor(R.id.vertretung_specific_entry_textViewRoom, ContextCompat.getColor(context, R.color.colorAccent))
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewOther, View.VISIBLE)
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewOther, TypedValue.COMPLEX_UNIT_SP, 16f)
        view.setTextViewText(R.id.vertretung_specific_entry_textViewOther, "")
        view.setTextColor(R.id.vertretung_specific_entry_textViewOther, VertretungWidgetProvider.textColorSecondary)
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewClass, View.VISIBLE)
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewClass, TypedValue.COMPLEX_UNIT_SP, 12f)
        view.setTextViewText(R.id.vertretung_specific_entry_textViewClass, "")
        view.setTextColor(R.id.vertretung_specific_entry_textViewClass, VertretungWidgetProvider.textColorSecondary)
    }

    private fun generateHeadline(context: Context, isShort: Boolean, oberstufe: Boolean): Array<String> {
        val headline: Array<String>
        headline = if (oberstufe) {
            arrayOf(if (isShort) context.getString(R.string.hours_short_three) else context.getString(R.string.hours), if (isShort) context.getString(R.string.courses_short) else context.getString(R.string.courses), if (isShort) context.getString(R.string.teacher_short) else context.getString(R.string.teacher), if (isShort) context.getString(R.string.room_short) else context.getString(R.string.room), context.getString(R.string.other_short), context.getString(R.string.subject))
        } else {
            arrayOf(if (isShort) context.getString(R.string.hours_short_three) else context.getString(R.string.hours), context.getString(R.string.subject), if (isShort) context.getString(R.string.teacher_short) else context.getString(R.string.teacher), if (isShort) context.getString(R.string.room_short) else context.getString(R.string.room), context.getString(R.string.other_short), if (isShort) context.getString(R.string.classes_short) else context.getString(R.string.classes))
        }
        return headline
    }
}