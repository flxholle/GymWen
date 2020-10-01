package com.asdoi.gymwen.coronalive

import org.jsoup.nodes.Document

object ParseCoronaLiveTicker {
    fun parseLiveTicker(doc: Document, city: String): CoronaTicker? {
        try {
            val table = doc.select("table#tableLandkreise")
            val rows = table.select("tbody tr")

            val headline = rows[0].select("th").eachText()
            val cityIndex: Int = headline.indexOf("Landkreis/Stadt")
            val infectionsIndex: Int = headline.indexOf("Anzahl der Fälle")
            val infectionsYesterdayTodayIndex: Int = headline.indexOf("Fälle Änderung zum Vortag")
            val infectionsPerOneHundredThousandsIndex: Int = headline.indexOf("Fallzahl pro 100.000 Einwohner")
            val infectionsInTheLastSevenDaysIndex: Int = headline.indexOf("Fälle der letzten 7 Tage")
            val sevenDayIncidencePerOneHundredThousandsIndex: Int = headline.indexOf("7-Tage-Inzidenz pro 100.000 Einwohner")
            val deathsIndex: Int = headline.indexOf("Anzahl der Todesfälle")
            val deathsYesterdayTodayIndex: Int = headline.indexOf("Todesfälle Änderung zum Vortag")

            for (line in 1 until rows.size) {
                val row = rows[line].select("td").eachText()
                if (row[cityIndex] == city) {
                    val infections = row[infectionsIndex].toInt()
                    val infectionsYesterdayTodayString = row[infectionsYesterdayTodayIndex]
                            .replace("(", "").replace(")", "").replace("+", "").replace(" ", "").trim()
                    val infectionsYesterdayToday =
                            if (infectionsYesterdayTodayString == "-")
                                0
                            else infectionsYesterdayTodayString.trim().toInt()
                    val infectionsPerOneHundredThousands = row[infectionsPerOneHundredThousandsIndex].replace(",", ".").toFloat()
                    val infectionsInTheLastSevenDays = row[infectionsInTheLastSevenDaysIndex].toInt()
                    val sevenDayIncidencePerOneHundredThousands = row[sevenDayIncidencePerOneHundredThousandsIndex].replace(",", ".").toFloat()
                    val deaths = row[deathsIndex].toInt()
                    val deathsYesterdayTodayString = row[deathsYesterdayTodayIndex]
                            .replace("(", "").replace(")", "").replace("+", "").replace(" ", "").trim()
                    val deathsYesterdayToday =
                            if (deathsYesterdayTodayString == "-")
                                0
                            else deathsYesterdayTodayString.toInt()

                    return CoronaTicker(infections, infectionsYesterdayToday, infectionsPerOneHundredThousands, infectionsInTheLastSevenDays, sevenDayIncidencePerOneHundredThousands, deaths, deathsYesterdayToday)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}