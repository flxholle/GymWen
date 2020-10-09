package com.asdoi.gymwen.teacherlist

open class TeacherList(entries: List<Teacher>) {
    var entries: List<Teacher> = entries
        protected set

    fun findTeacher(regex: String): Teacher? {
        for (teacher in entries) {
            if (teacher.abbreviation.equals(regex, true)
                    || teacher.firstName.equals(regex, true)
                    || teacher.lastName.equals(regex, true)
                    || teacher.officeHour.equals(regex, true))
                return teacher
        }
        return null
    }

    fun getMatches(regex: String): TeacherList {
        val matches = mutableListOf<Teacher>()
        for (teacher in entries) {
            if (teacher.abbreviation.contains(regex, true)
                    || teacher.firstName.contains(regex, true)
                    || teacher.lastName.contains(regex, true))
                matches.add(teacher)
        }
        return TeacherList(matches)
    }

    fun getContainingItems(regex: String): TeacherList {
        val matches = mutableListOf<Teacher>()
        for (teacher in entries) {
            if (teacher.abbreviation.contains(regex, true)
                    || teacher.firstName.contains(regex, true)
                    || teacher.lastName.contains(regex, true)
                    || teacher.officeHour.contains(regex, true))
                matches.add(teacher)
        }
        return TeacherList(matches)
    }
}