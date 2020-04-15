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

package com.asdoi.gymwen.substitutionplan

import com.asdoi.gymwen.util.External_Const
import com.asdoi.gymwen.util.PreferenceUtil
import java.text.SimpleDateFormat
import java.util.*

class SubstitutionList(var entries: MutableList<SubstitutionEntry> = mutableListOf()) {
    private var noInternet: Boolean = false

    constructor(notInternet: Boolean) : this() {
        this.noInternet = true
    }

    fun add(entry: SubstitutionEntry) {
        entries.add(entry)
    }

    fun getNoInternet(): Boolean {
        return noInternet
    }

    fun size(): Int {
        return entries.size
    }

    fun isEmpty(): Boolean {
        return entries.isEmpty()
    }

    fun sortList(): SubstitutionList {
        entries = sortList(entries)
        return this
    }

    private fun sortList(value: List<SubstitutionEntry>): MutableList<SubstitutionEntry> {
        val numbers = IntArray(value.size)
        for (i in value.indices) {
            numbers[i] = value[i].hour.toInt()
        }
        Arrays.sort(numbers)
        val returnValue = mutableListOf<SubstitutionEntry>()
        var j = 0
        while (j < numbers.size) {
            for (i in value.indices) {
                if (j < numbers.size) {
                    if ("" + numbers[j] == value[i].hour) {
                        returnValue.add(value[i])
                        j++
                    }
                }
            }
        }
        return returnValue
    }

    protected fun changeHourToTime() {
        entries = changeToTime(entries)
    }

    /**
     * @return a plan array with all hours replaced with their matching times
     */
    //Times
    private fun changeToTime(list: MutableList<SubstitutionEntry>): MutableList<SubstitutionEntry> {
        for (i in list.indices) {
            try {
                list[i].hour = getMatchingStartTime(list[i].hour.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return list
    }

    protected fun summarizeUp(separator: String = "-", hours: Boolean = PreferenceUtil.isHour()): SubstitutionList {
        if (noInternet)
            return this
        val newList = mutableListOf<SubstitutionEntry>()

        for (i in entries.indices) {
            if (newList.size == 0) {
                newList.add(entries[i])
                continue
            }

            val before = newList[newList.size - 1]
            val check = checkEntries(before, entries[i])
            if (check) {
                val valuesAddedToBefore: Int = before.hour.indexOf(separator)
                before.hour = before.hour.substring(0, if (valuesAddedToBefore > 0) valuesAddedToBefore else before.hour.length) + separator + entries[i].hour
                newList[newList.size - 1] = before
            } else {
                newList.add(entries[i])
            }

        }

        entries = newList

        if (hours) {
            try {
                for (entry in entries) {
                    val separatorIndex = entry.hour.indexOf(separator)
                    if (separatorIndex > 0) {
                        var begin = entry.hour.substring(0, separatorIndex)
                        var end = entry.hour.substring(separatorIndex + 1)
                        begin = getMatchingStartTime(begin.toInt())
                        end = getMatchingEndTime(end.toInt())
                        entry.hour = begin + separator + end
                    } else {
                        entry.hour = getMatchingStartTime(entry.hour.toInt())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return this
    }

    private fun checkEntries(value1: SubstitutionEntry, value2: SubstitutionEntry): Boolean {
        var check = true

        //Do not check hour
        when {
            value1.course != value2.course -> {
                check = false
            }
            value1.subject != value2.subject -> check = false
            value1.teacher != value2.teacher -> check = false
            value1.room != value2.room -> check = false
            value1.moreInformation != value2.moreInformation -> check = false
        }

        return check
    }

    fun replaceAll(regex: String, replacement: String): SubstitutionList {
        for (i in entries.indices) {
            entries[i] = entries[i].replaceAll(regex, replacement)
        }
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null || javaClass != other.javaClass)
            return false

        val list2: SubstitutionList = other as SubstitutionList

        if (entries.size != list2.entries.size)
            return false

        if (entries.size <= 0)
            return true

        for (i in entries.indices) {
            if (entries[i] != list2.entries[i])
                return false
        }

        return true
    }

    companion object {
        /**
         * @param lesson the lesson
         * @return the matching time
         */
        fun getMatchingStartTime(lesson: Int): String {
            return when (lesson) {
                1 -> "8:10"
                2 -> "8:55"
                3 -> "9:55"
                4 -> "10:40"
                5 -> "11:40"
                6 -> "12:25"
                7 -> "13:15"
                8 -> "14:00"
                9 -> "14:45"
                10 -> "15:30"
                11 -> "16:15"
                else ->                 //Breaks are excluded
                    ("" + (45 * lesson + 8 * 60 + 10) / 60).replace(",".toRegex(), ".")
            }
        }

        /**
         * @param lesson the lesson
         * @return the matching time
         */
        fun getMatchingEndTime(lesson: Int): String {
            return when (lesson) {
                1 -> "8:55"
                2 -> "9:40"
                3 -> "10:40"
                4 -> "11:25"
                5 -> "12:25"
                6 -> "13:10"
                7 -> "14:00"
                8 -> "14:45"
                9 -> "15:30"
                10 -> "16:15"
                11 -> "17:00"
                else ->                 //Breaks are excluded
                    ("" + (45 * (lesson + 1) + 8 * 60 + 10) / 60).replace(",".toRegex(), ".")
            }
        }
    }
}

class SubstitutionEntry(var course: String, var hour: String, var subject: String, var teacher: String, var room: String, var moreInformation: String) {
    fun replaceAll(regex: String, replacement: String): SubstitutionEntry {
        course = course.replace(regex, replacement)
        hour = hour.replace(regex, replacement)
        subject = subject.replace(regex, replacement)
        teacher = teacher.replace(regex, replacement)
        moreInformation = moreInformation.replace(regex, replacement)

        return this
    }

    /**
     * @return boolean if the subject equals the specific value for the hour is omitted
     */
    fun isNothing(): Boolean {
        for (s in External_Const.nothing) {
            if (s.equals(teacher, ignoreCase = true))
                return true
        }
        return false
    }

    fun getMatchingBeginTime(separator: String = "-"): String {
        try {
            val time =
                    if (hour.contains(separator))
                        hour.substring(0, hour.indexOf("-"))
                    else
                        hour
            return SubstitutionList.getMatchingStartTime(Integer.parseInt(time))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return hour
    }

    fun getMatchingEndTime(separator: String = "-"): String {
        try {
            val time =
                    if (hour.contains(separator))
                        hour.substring(hour.indexOf("-") + 1)
                    else
                        hour
            return SubstitutionList.getMatchingEndTime(Integer.parseInt(time))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return hour
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null || javaClass != other.javaClass)
            return false
        val entry: SubstitutionEntry = other as SubstitutionEntry
        return course == entry.course && hour == entry.hour && subject == entry.subject && teacher == entry.teacher && room == entry.room && moreInformation == entry.moreInformation
    }
}

class SubstitutionTitle(var date: String = "", var dayOfWeek: String = "", var week: String = "", var titleCode: Int = -1) {
    private var noInternet: Boolean = false

    constructor(noInternet: Boolean) : this() {
        this.noInternet = true
    }

    override fun toString(): String {
        return "$date, $dayOfWeek, $week"
    }

    fun getNoInternet(): Boolean {
        return noInternet
    }

    fun isTitleCodeInPast(): Boolean {
        var isPast = titleCode == SubstitutionPlan.pastCode
        if (!isPast && titleCode == SubstitutionPlan.todayCode) {
            try {
                val string1 = PreferenceUtil.hideDayAfterTime
                val mydate = removeDate(SimpleDateFormat("HH:mm:ss").parse(string1))
                val now = removeDate(Date())
                if (now.after(mydate)) {
                    isPast = true
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return isPast
    }

    fun isTitleCodeToday(): Boolean {
        return titleCode == SubstitutionPlan.todayCode
    }

    fun getDayCode(): Int {
        if (isTitleCodeInPast())
            return -1

        val df = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val startDate = removeTime(df.parse(date))

        val c = Calendar.getInstance()
        c.time = startDate // yourdate is an object of type Date
        return c[Calendar.DAY_OF_WEEK] // this will for example return 3 for tuesday


/*        return when (dayOfWeek) {
            "Montag" -> SubstitutionPlan.monday
            "Dienstag" -> SubstitutionPlan.tuesday
            "Mittwoch" -> SubstitutionPlan.wednesday
            "Donnerstag" -> SubstitutionPlan.thursday
            "Freitag" -> SubstitutionPlan.friday
            "Samstag" -> SubstitutionPlan.saturday
            "Sonntag" -> SubstitutionPlan.sunday
            else -> -1
        }*/
    }

    /**
     * @param date Date
     * @return param Date with removed time (only the day).
     */
    private fun removeDate(date: Date): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal[Calendar.YEAR] = 0
        cal[Calendar.MONTH] = 0
        cal[Calendar.DATE] = 0
        return cal.time
    }

    /**
     * @param date Date
     * @return param Date with removed time (only the day).
     */
    private fun removeTime(date: Date): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0
        return cal.time
    }
}
