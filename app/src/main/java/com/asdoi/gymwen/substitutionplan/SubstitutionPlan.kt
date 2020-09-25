package com.asdoi.gymwen.substitutionplan

import android.content.Context
import com.asdoi.gymwen.R
import com.asdoi.gymwen.util.PreferenceUtil
import org.jsoup.nodes.Document

class SubstitutionPlan(val courses: Array<String>) {

    constructor(className: String) : this(arrayOf(className))

    private var todayList: SubstitutionList? = null
    private var tomorrowList: SubstitutionList? = null

    val senior
        get() = courses.size > 1

    fun setDocuments(todayDocument: Document?, tomorrowDocument: Document?) {
        todayList = if (todayDocument != null)
            parseList(todayDocument)
        else
            null

        tomorrowList = if (tomorrowDocument != null)
            parseList(tomorrowDocument)
        else
            null
    }

    fun areDocumentsSet() = todayList == null && tomorrowList == null

    private fun parseList(document: Document): SubstitutionList? {
        return Parse.parseSubstitutionList(document)
    }

    fun getDay(today: Boolean): SubstitutionList? {
        return if (today) {
            todayList
        } else {
            tomorrowList
        }
    }

    fun getDaySummarized(today: Boolean): SubstitutionList? = getDay(today)?.summarize()

    fun getDayFiltered(today: Boolean) = getDay(today)?.filter(courses)

    fun getDayFilteredSummarized(today: Boolean) = getDayFiltered(today)?.summarize()

    fun isContentEqual(newDocument: Document): Boolean {
        val newList = parseList(newDocument) ?: return false

        return when (newList.title) {
            todayList?.title ->
                todayList?.isContentEqual(newList) ?: false
            tomorrowList?.title ->
                tomorrowList?.isContentEqual(newList) ?: false
            else ->
                false
        }
    }


    //Easier Access Methods
    fun getToday() = getDay(true)

    fun getTomorrow() = getDay(false)

    fun getTodaySummarized() = getDaySummarized(true)

    fun getTomorrowSummarized() = getDaySummarized(false)

    fun getTodayFiltered() = getDayFiltered(true)

    fun getTomorrowFiltered() = getDayFiltered(false)

    fun getTodayTitle() = getToday()?.title

    fun getTomorrowTitle() = getTomorrow()?.title

    fun hasContentChanged(newDocument: Document) = !isContentEqual(newDocument)


    //Context Methods
    fun getFormattedTitleString(context: Context, today: Boolean, showDayOfWeek: Boolean): String {
        val title: SubstitutionTitle =
                if (today)
                    getTodayTitle() ?: return context.getString(R.string.noInternetConnection)
                else
                    getTomorrowTitle() ?: return context.getString(R.string.noInternetConnection)

        return title.toString(context, showDayOfWeek)
    }

    fun getFormattedTitleString(context: Context, today: Boolean) = getFormattedTitleString(context, today, PreferenceUtil.showDayOfWeek())

    fun getTodayFormattedTitleString(context: Context) = getFormattedTitleString(context, true)

    fun getTomorrowFormattedTitleString(context: Context) = getFormattedTitleString(context, false)
}