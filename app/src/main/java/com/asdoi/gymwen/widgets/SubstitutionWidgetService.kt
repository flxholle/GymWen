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
import com.asdoi.gymwen.R
import com.asdoi.gymwen.profiles.Profile
import com.asdoi.gymwen.profiles.ProfileManagement
import com.asdoi.gymwen.substitutionplan.SubstitutionEntry
import com.asdoi.gymwen.substitutionplan.SubstitutionList
import com.asdoi.gymwen.substitutionplan.SubstitutionPlan
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures
import com.asdoi.gymwen.ui.activities.SubstitutionWidgetActivity
import com.asdoi.gymwen.ui.fragments.SubstitutionFragment
import com.asdoi.gymwen.util.PreferenceUtil

private const val nothing = -1
private const val day = -2
private const val profile = -3
private const val headline = -4
private const val content = -5
private const val internet = -6


class SubstitutionWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        var profiles = mutableListOf<Profile>()
        ProfileManagement.initProfiles()

        if (intent.hasExtra(SubstitutionWidgetActivity.PROFILES)) {
            val profilesPos = intent.getIntArrayExtra(SubstitutionWidgetActivity.PROFILES)
            if (profilesPos != null && profilesPos.isNotEmpty()) {
                for (i in profilesPos) {
                    if (i < ProfileManagement.getSize()) {
                        profiles.add(ProfileManagement.getProfile(i))
                    }
                }
            }

            if (profiles.size == 0)
                profiles = ProfileManagement.getProfileList()
        } else {
            profiles = ProfileManagement.getProfileList()
        }

        return SubstitutionWidgetFactory(this.applicationContext, profiles)
    }
}


class SubstitutionWidgetFactory(val context: Context, val profiles: List<Profile>) : RemoteViewsService.RemoteViewsFactory {
    private var contentList: MutableList<EntryHelper> = mutableListOf()

    override fun onCreate() {
        contentList = mutableListOf()

        var noInternet = false
        val summarize = PreferenceUtil.isSummarizeUp() && PreferenceUtil.isSummarizeOld()

        val todayEntryList = mutableListOf<EntryHelper>()
        val tomorrowEntryList = mutableListOf<EntryHelper>()
        var today = ""
        var tomorrow = ""

        var showToday = !PreferenceUtil.isIntelligentHide() || !SubstitutionPlanFeatures.getTodayTitle().isTitleCodeInPast()
        var showTomorrow = !PreferenceUtil.isIntelligentHide() || !SubstitutionPlanFeatures.getTomorrowTitle().isTitleCodeInPast()
        if (!showToday && !showTomorrow) {
            if (SubstitutionPlanFeatures.getTodayTitle().titleCode == SubstitutionPlan.todayCode) showToday = true else showTomorrow = true
        }

        for (p in profiles) {
            val tempSubstitutionplan = SubstitutionPlanFeatures.createTempSubstitutionplan(PreferenceUtil.isHour(), p.coursesArray)

            //Today
            if (showToday) {
                today = tempSubstitutionplan.getTitleString(true)
                var todayList = tempSubstitutionplan.today
                if (summarize) todayList = tempSubstitutionplan.todaySummarized
                if (todayList.getNoInternet()) {
                    noInternet = true
                    break
                }
                todayEntryList.addAll(getEntryListForProfile(todayList, if (profiles.size == 1) null else p.name, tempSubstitutionplan.senior))
            }

            //Tomorrow
            if (showTomorrow) {
                tomorrow = tempSubstitutionplan.getTitleString(false)
                var tomorrowList = tempSubstitutionplan.tomorrow
                if (summarize) tomorrowList = tempSubstitutionplan.tomorrowSummarized
                if (tomorrowList.getNoInternet()) {
                    noInternet = true
                    break
                }
                tomorrowEntryList.addAll(getEntryListForProfile(tomorrowList, if (profiles.size == 1) null else p.name, tempSubstitutionplan.senior))
            }
        }

        if (noInternet) {
            contentList = mutableListOf(EntryHelper(code = internet))
            return
        }

        if (showToday) {
            contentList.add(EntryHelper(title = today, code = day))
            contentList.addAll(todayEntryList)
        }
        if (showTomorrow) {
            contentList.add(EntryHelper(title = tomorrow, code = day))
            contentList.addAll(tomorrowEntryList)
        }

    }

    private fun getEntryListForProfile(dayList: SubstitutionList, name: String? = null, senior: Boolean): List<EntryHelper> {
        val entryList = mutableListOf<EntryHelper>()
        if (name != null) entryList.add(EntryHelper(title = name, code = profile))

        val miscellaneous: Boolean
        if (dayList.isEmpty())
            entryList.add(EntryHelper(code = nothing))
        else {
            miscellaneous = SubstitutionFragment.isMiscellaneous(dayList)
            entryList.add(EntryHelper(headline = generateHeadline(context, true, senior), code = headline, miscellaneous = miscellaneous, senior = senior))
            for (entry in dayList.entries) {
                entryList.add(EntryHelper(entry, content, miscellaneous))
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
        val entry: EntryHelper = contentList[position]
        val view = when (entry.code) {
            day -> getTitleText(context, entry.title)
            profile -> getTitleText(context, entry.title)
            nothing -> getTitleText(context, context.getString(R.string.nothing))
            headline -> getHeadline(entry.headline, entry.miscellaneous)
            content -> getEntrySpecific(entry.entry, entry.senior, entry.miscellaneous)
            internet -> getTitleText(context, context.getString(R.string.noInternetConnection))
            else -> getTitleText(context, context.getString(R.string.noInternetConnection))
        }

        //Set OpenApp Button intent
        val intent = Intent()
        intent.action = SubstitutionWidgetProvider.OPEN_APP
        view.setOnClickFillInIntent(R.id.widget_substitution_list_linear, intent)
        return view
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

    class EntryHelper(val entry: SubstitutionEntry = SubstitutionEntry("", "", "", "", "", ""), val code: Int = nothing, val miscellaneous: Boolean = false, val senior: Boolean = false, val title: String = "", val headline: Array<String> = arrayOf())


    //View creators

    //From SubstitutionFragment (from Java imported)
    private fun getTitleText(context: Context, text: String): RemoteViews {
        val view = getRemoteViews(context)
        view.setViewVisibility(R.id.substitution_specific_entry_textViewHour, View.GONE)
        view.setViewVisibility(R.id.substitution_specific_entry_textViewSubject, View.GONE)
        view.setTextViewText(R.id.substitution_specific_entry_textViewTeacher, text)
        view.setTextViewTextSize(R.id.substitution_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, 25f)
        view.setTextColor(R.id.substitution_specific_entry_textViewTeacher, SubstitutionWidgetProvider.textColorPrimary)
        view.setViewVisibility(R.id.substitution_specific_entry_textViewRoom, View.GONE)
        view.setViewVisibility(R.id.substitution_specific_entry_textViewOther, View.GONE)
        view.setViewVisibility(R.id.substitution_specific_entry_textViewClass, View.GONE)
        //        view.setOnClickPendingIntent(R.id.widget_entry_linear, SubstitutionWidgetProvider.getPendingSelfIntent(context, SubstitutionWidgetProvider.WIDGET_ON_CLICK));
        return view
    }

    private fun getHeadline(headline: Array<String>, miscellaneous: Boolean): RemoteViews {
        val view = getRemoteViews(context)
        val textSize = 17
        view.setTextViewText(R.id.substitution_specific_entry_textViewHour, headline[0])
        view.setTextViewTextSize(R.id.substitution_specific_entry_textViewHour, TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
        view.setTextViewText(R.id.substitution_specific_entry_textViewSubject, headline[1])
        view.setTextViewTextSize(R.id.substitution_specific_entry_textViewSubject, TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
        view.setTextViewTextSize(R.id.substitution_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
        view.setTextViewText(R.id.substitution_specific_entry_textViewTeacher, headline[2])
        view.setTextViewText(R.id.substitution_specific_entry_textViewRoom, headline[3])
        view.setTextColor(R.id.substitution_specific_entry_textViewRoom, SubstitutionWidgetProvider.textColorSecondary)
        view.setTextViewTextSize(R.id.substitution_specific_entry_textViewRoom, TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
        view.setViewVisibility(R.id.substitution_specific_entry_textViewOther, if (miscellaneous) View.VISIBLE else View.GONE)
        view.setTextViewText(R.id.substitution_specific_entry_textViewOther, headline[4])
        view.setTextViewTextSize(R.id.substitution_specific_entry_textViewOther, TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
        view.setTextViewText(R.id.substitution_specific_entry_textViewClass, headline[5])
        view.setTextViewTextSize(R.id.substitution_specific_entry_textViewClass, TypedValue.COMPLEX_UNIT_SP, 10f)
        //        view.setOnClickPendingIntent(R.id.widget_entry_linear, SubstitutionWidgetProvider.getPendingSelfIntent(context, SubstitutionWidgetProvider.WIDGET_ON_CLICK));
        return view
    }

    private fun getEntrySpecific(entry: SubstitutionEntry, senior: Boolean, miscellaneous: Boolean): RemoteViews {
        val view = getRemoteViews(context)
        view.setTextViewText(R.id.substitution_specific_entry_textViewHour, entry.hour)
        view.setTextViewText(R.id.substitution_specific_entry_textViewSubject, if (senior) entry.course else entry.subject)
        if (!entry.isNothing()) {
            view.setTextViewText(R.id.substitution_specific_entry_textViewTeacher, entry.teacher)
            view.setViewVisibility(R.id.substitution_specific_entry_textViewRoom, View.VISIBLE)
            val content = SpannableString(entry.room)
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            view.setTextViewText(R.id.substitution_specific_entry_textViewRoom, content)
        } else {
            val content = SpannableString(entry.teacher)
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            view.setTextViewText(R.id.substitution_specific_entry_textViewTeacher, content)
            //            view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, 20);
            view.setTextColor(R.id.substitution_specific_entry_textViewTeacher, ContextCompat.getColor(context, R.color.colorAccent))
            //            view.setViewVisibility(R.id.vertretung_specific_entry_textViewRoom, View.GONE);
        }
        view.setViewVisibility(R.id.substitution_specific_entry_textViewOther, if (miscellaneous) View.VISIBLE else View.GONE)
        view.setTextViewText(R.id.substitution_specific_entry_textViewOther, entry.moreInformation)
        view.setTextViewText(R.id.substitution_specific_entry_textViewClass, if (senior) entry.subject else entry.course)
        //        view.setOnClickPendingIntent(R.id.widget_entry_linear, SubstitutionWidgetProvider.getPendingSelfIntent(context, SubstitutionWidgetProvider.WIDGET_ON_CLICK));
        return view
    }

    private fun getRemoteViews(context: Context): RemoteViews {
        val view = RemoteViews(context.packageName, R.layout.widget_list_entry)
        resetView(view)
        return view
    }

    private fun resetView(view: RemoteViews) {
        view.setViewVisibility(R.id.substitution_specific_entry_textViewHour, View.VISIBLE)
        view.setTextViewTextSize(R.id.substitution_specific_entry_textViewHour, TypedValue.COMPLEX_UNIT_SP, 36f)
        view.setTextViewText(R.id.substitution_specific_entry_textViewHour, "")
        view.setTextColor(R.id.substitution_specific_entry_textViewHour, Color.WHITE)
        view.setViewVisibility(R.id.substitution_specific_entry_textViewSubject, View.VISIBLE)
        view.setTextViewTextSize(R.id.substitution_specific_entry_textViewSubject, TypedValue.COMPLEX_UNIT_SP, 18f)
        view.setTextViewText(R.id.substitution_specific_entry_textViewSubject, "")
        view.setTextColor(R.id.substitution_specific_entry_textViewSubject, SubstitutionWidgetProvider.textColorSecondary)
        view.setViewVisibility(R.id.substitution_specific_entry_textViewTeacher, View.VISIBLE)
        view.setTextViewTextSize(R.id.substitution_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, 17f)
        view.setTextViewText(R.id.substitution_specific_entry_textViewTeacher, "")
        view.setTextColor(R.id.substitution_specific_entry_textViewTeacher, SubstitutionWidgetProvider.textColorSecondary)
        view.setViewVisibility(R.id.substitution_specific_entry_textViewRoom, View.VISIBLE)
        view.setTextViewTextSize(R.id.substitution_specific_entry_textViewRoom, TypedValue.COMPLEX_UNIT_SP, 24f)
        view.setTextViewText(R.id.substitution_specific_entry_textViewRoom, "")
        view.setTextColor(R.id.substitution_specific_entry_textViewRoom, ContextCompat.getColor(context, R.color.colorAccent))
        view.setViewVisibility(R.id.substitution_specific_entry_textViewOther, View.VISIBLE)
        view.setTextViewTextSize(R.id.substitution_specific_entry_textViewOther, TypedValue.COMPLEX_UNIT_SP, 16f)
        view.setTextViewText(R.id.substitution_specific_entry_textViewOther, "")
        view.setTextColor(R.id.substitution_specific_entry_textViewOther, SubstitutionWidgetProvider.textColorSecondary)
        view.setViewVisibility(R.id.substitution_specific_entry_textViewClass, View.VISIBLE)
        view.setTextViewTextSize(R.id.substitution_specific_entry_textViewClass, TypedValue.COMPLEX_UNIT_SP, 12f)
        view.setTextViewText(R.id.substitution_specific_entry_textViewClass, "")
        view.setTextColor(R.id.substitution_specific_entry_textViewClass, SubstitutionWidgetProvider.textColorSecondary)
    }

    private fun generateHeadline(context: Context, isShort: Boolean, senior: Boolean): Array<String> {
        return if (senior) {
            arrayOf(if (isShort) context.getString(R.string.hours_short_three) else context.getString(R.string.hours), if (isShort) context.getString(R.string.courses_short) else context.getString(R.string.courses), if (isShort) context.getString(R.string.teacher_short) else context.getString(R.string.teacher), if (isShort) context.getString(R.string.room_short) else context.getString(R.string.room), context.getString(R.string.miscellaneous_short), context.getString(R.string.subject))
        } else {
            arrayOf(if (isShort) context.getString(R.string.hours_short_three) else context.getString(R.string.hours), context.getString(R.string.subject), if (isShort) context.getString(R.string.teacher_short) else context.getString(R.string.teacher), if (isShort) context.getString(R.string.room_short) else context.getString(R.string.room), context.getString(R.string.miscellaneous_short), if (isShort) context.getString(R.string.classes_short) else context.getString(R.string.classes))
        }
    }
}