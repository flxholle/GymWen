package com.asdoi.gymwen.teacherlist

import org.jsoup.nodes.Document

object Parse {
    fun parseTeacherList(doc: Document): TeacherList? {
        try {
            //abbreviation - last name - first name - office hour
            val lines: List<String> = doc.select("div#content_left").select("div.csc-default")[1].select("p.bodytext").eachText()
            val entries = mutableListOf<Teacher>()

            for (line in lines) {
                try {
                    val abbreviation = line.substring(0, 3)

                    val indexOfComma = line.indexOf(',')
                    var lastName = line.substring(4, indexOfComma)

                    var indexNextWhitespace = line.indexOf(' ', indexOfComma + 2)
                    val firstName =
                            if (line.contains("Dr.")) {
                                indexNextWhitespace = line.indexOf(' ', indexOfComma + 6)
                                lastName = "Dr. $lastName"
                                line.substring(indexOfComma + 6, indexNextWhitespace)
                            } else
                                line.substring(indexOfComma + 2, indexNextWhitespace)

                    val officeHour = line.substring(indexNextWhitespace + 1)

                    entries.add(Teacher(abbreviation, firstName, lastName, officeHour))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return TeacherList(entries)

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}