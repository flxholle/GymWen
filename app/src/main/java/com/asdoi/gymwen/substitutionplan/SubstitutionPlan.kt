package com.asdoi.gymwen.substitutionplan

import android.content.Context
import com.asdoi.gymwen.R
import com.asdoi.gymwen.util.PreferenceUtil
import org.jsoup.nodes.Document

open class SubstitutionPlan(courses: Array<String>) {

    constructor(className: String) : this(arrayOf(className))

    var courses: Array<String> = courses
        protected set
    val senior
        get() = courses.size > 1

    private var todayList: SubstitutionList? = null
    private var tomorrowList: SubstitutionList? = null

    var todayDocument: Document? = null
        set(value) {
            todayList = value?.let { parseList(it) }
            field = value
        }
    var tomorrowDocument: Document? = null
        set(value) {
            tomorrowList = value?.let { parseList(it) }
            field = value
        }

    fun setDocuments(todayDocument: Document?, tomorrowDocument: Document?) {
        this.todayDocument = todayDocument
        this.tomorrowDocument = tomorrowDocument
    }

    fun areDocumentsSet() = todayDocument == null && tomorrowDocument == null

    private fun parseList(document: Document): SubstitutionList? {
        return Parse.parseSubstitutionList(document)
    }

    open fun getDay(today: Boolean): SubstitutionList? {
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
            getToday()?.title ->
                getToday()?.isContentEqual(newList) ?: false
            getTomorrow()?.title ->
                getTomorrow()?.isContentEqual(newList) ?: false
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