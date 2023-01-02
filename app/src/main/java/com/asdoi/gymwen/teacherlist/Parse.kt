package com.asdoi.gymwen.teacherlist

import org.jsoup.nodes.Document

object Parse {
    fun parseTeacherList(doc: Document): TeacherList? {
        try {
            //abbreviation - last name - first name - office hour
            val lines = doc.select("div.table-responsive").select("tbody").select("tr").drop(1)
            val entries = mutableListOf<Teacher>()

            for (lineRaw in lines) {
                try {
                    val line = lineRaw.select("td")

                    val abbreviation = line[0].text().trim()

                    val name = line[1].text()
                    val indexComma = name.lastIndexOf(", ")
                    val firstName = name.substring(0, indexComma)
                    val lastName = name.substring(indexComma + 1, name.length)

                    val officeHour = line[2].text()

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