package com.asdoi.gymwen.teacherlist

import org.jsoup.nodes.Document

object Parse {
    fun parseTeacherList(doc: Document): TeacherList? {
        try {
            //abbreviation - last name - first name - day - office hour
            val lines = doc.select("div.table-responsive").select("tbody").select("tr").drop(1)
            val entries = mutableListOf<Teacher>()

            for (lineRaw in lines.drop(1)) {
                try {
                    val line = lineRaw.select("td")

                    if (line.size < 3)
                        continue

                    val firstColumn = line[0].text()
                    val indexSpace = firstColumn.indexOf(' ')
                    val indexComma = firstColumn.indexOf(", ")

                    val abbreviation = firstColumn.substring(0, indexSpace)

                    val firstName = firstColumn.substring(indexComma + 2)
                    val lastName = firstColumn.substring(indexSpace + 1, indexComma)

                    val offTmp = line[2].text()
                    val officeHour =
                        if (offTmp.trim().isEmpty())
                            line[1].text()
                        else
                            line[1].text() + ", " + offTmp

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