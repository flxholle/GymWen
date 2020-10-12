package com.asdoi.gymwen.substitutionplan

import android.content.Context
import com.asdoi.gymwen.R
import com.asdoi.gymwen.util.External_Const
import com.asdoi.gymwen.util.PreferenceUtil
import org.joda.time.LocalTime
import java.text.SimpleDateFormat
import java.util.*

data class SubstitutionEntry(
        val course: String,
        val startLesson: Int,
        val endLesson: Int,
        val startTime: LocalTime,
        val endTime: LocalTime,
        val subject: String,
        val teacher: String,
        val room: String,
        val moreInformation: String
) {

    constructor(course: String,
                startLesson: Int,
                endLesson: Int,
                subject: String,
                teacher: String,
                room: String,
                moreInformation: String) : this(course, startLesson, endLesson, getStartLocalTimeOfLesson(startLesson), getEndLocalTimeOfLesson(endLesson), subject, teacher, room, moreInformation)

    constructor(course: String,
                startLesson: Int,
                subject: String,
                teacher: String,
                room: String,
                moreInformation: String) : this(course, startLesson, startLesson, subject, teacher, room, moreInformation)

    /**
     * @return boolean if the subject equals the specific value for "the hour is omitted"
     */
    fun isNothing(): Boolean {
        for (omitted in External_Const.nothing) {
            if (omitted.equals(teacher, true))
                return true
        }
        return false
    }

    fun isStartEqualEnd() = startLesson == endLesson

    fun isContentEqual(compareEntry: SubstitutionEntry) =
            course == compareEntry.course && subject == compareEntry.subject && teacher == compareEntry.teacher && room == compareEntry.room && moreInformation == compareEntry.moreInformation

    fun getStart(time: Boolean): String = if (time) startTime.toString("HH:mm") else startLesson.toString()

    fun getEnd(time: Boolean): String = if (time) endTime.toString("HH:mm") else endLesson.toString()

    fun getTime() =
            if (isStartEqualEnd())
                getStart(PreferenceUtil.isHour())
            else
                "${getStart(PreferenceUtil.isHour())}-${getEnd(PreferenceUtil.isHour())}"

    fun getTimeSegment(context: Context) =
            if (PreferenceUtil.isHour()) {
                if (isStartEqualEnd())
                    "${context.getString(R.string.lesson_at)} ${getStart(true)}"
                else
                    "${context.getString(R.string.lessons_from)} ${getStart(true)}-${getEnd(true)}"
            } else {
                if (isStartEqualEnd())
                    "${getStart(false)}. ${context.getString(R.string.lesson)}"
                else
                    "${getStart(false)}.-${getEnd(false)}. ${context.getString(R.string.lesson)}"
            }


    companion object {
        fun getStartLocalTimeOfLesson(lesson: Int): LocalTime {
            val time = when (lesson) {
                0 -> "00:00"
                1 -> "08:10"
                2 -> "08:55"
                3 -> "09:55"
                4 -> "10:40"
                5 -> "11:40"
                6 -> "12:25"
                7 -> "13:15"
                8 -> "14:00"
                9 -> "14:45"
                10 -> "15:30"
                11 -> "16:15"
                12 -> "17:00"
                13 -> "17:45"
                else ->
                    //Breaks are excluded
                    ("" + (45 * lesson + 8 * 60 + 10) / 60).replace(",".toRegex(), ".")
            }
            try {
                val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                return LocalTime.fromDateFields(dateFormat.parse(time))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return LocalTime.MIDNIGHT
        }

        fun getEndLocalTimeOfLesson(lesson: Int): LocalTime {
            val time = when (lesson) {
                0 -> "00:00"
                1 -> "08:55"
                2 -> "09:40"
                3 -> "10:40"
                4 -> "11:25"
                5 -> "12:25"
                6 -> "13:10"
                7 -> "14:00"
                8 -> "14:45"
                9 -> "15:30"
                10 -> "16:15"
                11 -> "17:00"
                12 -> "17:45"
                13 -> "18:30"
                else ->
                    //Breaks are excluded
                    ("" + (45 * (lesson + 1) + 8 * 60 + 10) / 60).replace(",".toRegex(), ".")
            }

            try {
                val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                return LocalTime.fromDateFields(dateFormat.parse(time))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return LocalTime.MIDNIGHT
        }
    }
}