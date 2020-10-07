package com.asdoi.gymwen.substitutionplan

import org.joda.time.LocalDate
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*

object Parse {
    private val teacherHeadlines = listOf("Lehrer", "Vertretung")

    private fun parseSubstitutionTitle(doc: Document): SubstitutionTitle? {
        try {
            val title = doc.select("h2.TextUeberschrift")[0].text()
            val titleElements = title.replace("Vertretungsplan für ", "").replace("(", ",").split(",")

            val parser = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date = parser.parse(titleElements[1].trim())!!

            val c = Calendar.getInstance()
            c.time = date
            val dayOfWeek = DayOfWeek.getMatchingDay(c[Calendar.DAY_OF_WEEK])

            val weekChar: Char = titleElements[2].trim().replace(")", "").replace("Woche", "")[0]

            return SubstitutionTitle(LocalDate.fromDateFields(date), dayOfWeek, weekChar)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun parseSubstitutionList(doc: Document): SubstitutionList? {
        try {
            val rows = doc.select("table.TabelleVertretungen tr")

            val substitutionList = SubstitutionList(parseSubstitutionTitle(doc)!!)

            val headline = rows[0].select("td").eachText()
            for (i in headline.indices) {
                headline[i] = headline[i].trim()
            }

            val courseIndex: Int = headline.indexOf("Klasse")
            val hourIndex: Int = headline.indexOf("Std")
            val subjectIndex: Int = headline.indexOf("Fach")
            var teacherIndex: Int = -1
            for (name in teacherHeadlines) {
                if (headline.indexOf(name) >= 0)
                    teacherIndex = headline.indexOf(name)
            }
            val roomIndex: Int = headline.indexOf("Raum")
            val moreInformationIndex: Int = headline.indexOf("Sonstiges")

            for (i in 1 until rows.size) {
                try {
                    val content = rows[i].select("td")

                    val course: String = if (courseIndex >= 0) content[courseIndex].text().trim() else ""
                    val hour: Int = if (hourIndex >= 0) content[hourIndex].text().trim().toInt() else 0
                    val subject: String = if (subjectIndex >= 0) content[subjectIndex].text().trim() else ""
                    val teacher: String = if (teacherIndex >= 0) content[teacherIndex].text().trim() else ""
                    val room: String = if (roomIndex >= 0) content[roomIndex].text().trim() else ""
                    val moreInformation: String = if (moreInformationIndex >= 0) content[moreInformationIndex].text() else ""

                    substitutionList.add(
                            SubstitutionEntry(
                                    course, hour, subject, teacher, room, moreInformation
                            )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return substitutionList.sort()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}