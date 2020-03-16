package com.asdoi.gymwen.substitutionplan

import java.util.*

class SubstitutionList(var entries: MutableList<SubstitutionEntry> = mutableListOf<SubstitutionEntry>()) {
    private var noInternet: Boolean = false

    constructor(noInternet: Boolean) : this() {
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

    fun changeHourToTime() {
        entries = changeToTime(entries)
    }

    /**
     * @param value a substitution plan array with hours as the second entry
     * @return a plan array with all hours replaced with their matching times
     */
    //Times
    fun changeToTime(list: MutableList<SubstitutionEntry>): MutableList<SubstitutionEntry> {
        for (i in list.indices) {
            try {
                list[i].hour = getMatchingTime(list[i].hour.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return list
    }

    /**
     * @param lesson the lesson
     * @return the matching time
     */
    private fun getMatchingTime(lesson: Int): String {
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

    fun summarizeUp(separator: String = "-"): SubstitutionList {
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
        return this
    }

    private fun checkEntries(value1: SubstitutionEntry, value2: SubstitutionEntry): Boolean {
        var check = true

        //Do not check hour
        if (value1.course != value2.course) {
            check = false
        } else if (value1.subject != value2.subject)
            check = false
        else if (value1.teacher != value2.teacher)
            check = false
        else if (value1.room != value2.room)
            check = false
        else if (value1.moreInformation != value2.moreInformation)
            check = false

        return check
    }

    fun replaceAll(regex: String, replacement: String): SubstitutionList {
        for (i in entries.indices) {
            entries[i] = entries[i].replaceAll(regex, replacement)
        }
        return this
    }

    companion object {
        fun areListsEqual(list1: SubstitutionList, list2: SubstitutionList): Boolean {
            if (list1.entries.size != list1.entries.size)
                return false

            return list1.entries == list2.entries
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
}
