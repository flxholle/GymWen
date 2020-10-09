package com.asdoi.gymwen.substitutionplan

import java.util.*

class SubstitutionList(
        private var entries: MutableList<SubstitutionEntry>,
        val title: SubstitutionTitle
) {
    constructor(title: SubstitutionTitle) : this(mutableListOf<SubstitutionEntry>(), title)

    fun add(entry: SubstitutionEntry) {
        entries.add(entry)
    }

    fun getEntry(index: Int): SubstitutionEntry = entries[index]

    fun size(): Int = entries.size

    fun isEmpty(): Boolean = size() == 0

    fun isContentEqual(compareList: SubstitutionList) = entries == compareList.entries

    fun filter(courses: Array<String>): SubstitutionList {
        val filteredList = if (courses.size == 1)
            filterClass(courses[0])
        else
            filterCourses(courses)

        return SubstitutionList(filteredList, title)
    }

    private fun filterClass(className: String): MutableList<SubstitutionEntry> {
        val filteredList = mutableListOf<SubstitutionEntry>()
        val classLetter = className[className.length - 1]
        val classNumber = className.substring(0, className.length - 1)
        for (i in 0 until size()) {
            val entryCourse = getEntry(i).course
            if (entryCourse.contains(classLetter) && entryCourse.contains(classNumber))
                filteredList.add(getEntry(i))
        }
        return filteredList
    }

    private fun filterCourses(courses: Array<String>): MutableList<SubstitutionEntry> {
        val filteredList = mutableListOf<SubstitutionEntry>()
        for (i in 0 until size())
            if (getEntry(i).course in courses)
                filteredList.add(getEntry(i))
        return filteredList
    }

    fun sort(): SubstitutionList {
        val courses = mutableListOf<String>()
        for (entry in entries) {
            if (entry.course !in courses) {
                courses.add(entry.course)
            }
        }
        courses.sort()

        val sortedList = mutableListOf<SubstitutionEntry>()
        for (course in courses) {
            sortedList.addAll(filterClass(course))
        }

        return SubstitutionList(sortedList, title)
    }

    fun sortByStart(): SubstitutionList {
        val numbers = IntArray(entries.size)
        for (i in entries.indices) {
            numbers[i] = getEntry(i).startLesson
        }
        Arrays.sort(numbers)

        val sortedList = mutableListOf<SubstitutionEntry>()
        var j = 0
        while (j < entries.size) {
            for (i in entries.indices) {
                if (j < entries.size) {
                    if (numbers[j] == getEntry(i).startLesson) {
                        sortedList.add(getEntry(i))
                        j++
                    }
                }
            }
        }
        return SubstitutionList(sortedList, title)
    }

    fun summarize(): SubstitutionList {
        val summarizedList = mutableListOf<SubstitutionEntry>()

        for (i in entries.indices) {
            if (summarizedList.size == 0) {
                summarizedList.add(getEntry(i))
            } else {
                val entryBefore = summarizedList[summarizedList.size - 1]
                val currentEntry = getEntry(i)
                if (entryBefore.isContentEqual(currentEntry)) {
                    val summarizedEntry =
                            SubstitutionEntry(entryBefore.course, entryBefore.startLesson,
                                    currentEntry.endLesson, entryBefore.startTime, currentEntry.endTime,
                                    entryBefore.subject, entryBefore.teacher, entryBefore.room, entryBefore.moreInformation)

                    summarizedList[summarizedList.size - 1] = summarizedEntry
                } else {
                    summarizedList.add(currentEntry)
                }
            }

        }

        return SubstitutionList(summarizedList, title)
    }

    fun replaceRegex(regex: String, replacement: String): SubstitutionList {
        val replacedList = mutableListOf<SubstitutionEntry>()
        for (i in 0 until size()) {
            val oldEntry = getEntry(i)
            replacedList.add(
                    SubstitutionEntry(
                            oldEntry.course.replace(regex, replacement),
                            oldEntry.startLesson,
                            oldEntry.endLesson,
                            oldEntry.startTime,
                            oldEntry.endTime,
                            oldEntry.subject.replace(regex, replacement),
                            oldEntry.teacher.replace(regex, replacement),
                            oldEntry.room.replace(regex, replacement),
                            oldEntry.moreInformation.replace(regex, replacement)
                    ))
        }
        return SubstitutionList(replacedList, title)
    }
}