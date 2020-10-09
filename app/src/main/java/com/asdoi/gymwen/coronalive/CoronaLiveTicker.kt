package com.asdoi.gymwen.coronalive

import kotlin.math.roundToInt

data class CoronaTicker(val infections: Int,
                        val infectionsYesterdayToday: Int,
                        val infectionsPerOneHundredThousands: Double,
                        val infectionsInTheLastSevenDays: Int,
                        val sevenDayIncidencePerOneHundredThousands: Double,
                        val deaths: Int,
                        val deathsYesterdayToday: Int,
                        val coronaLightColor: CoronaLightColor) {

    constructor(infections: Int,
                infectionsYesterdayToday: Int,
                infectionsPerOneHundredThousands: Double,
                infectionsInTheLastSevenDays: Int,
                sevenDayIncidencePerOneHundredThousands: Double,
                deaths: Int,
                deathsYesterdayToday: Int) :
            this(infections,
                    infectionsYesterdayToday,
                    infectionsPerOneHundredThousands,
                    infectionsInTheLastSevenDays,
                    sevenDayIncidencePerOneHundredThousands,
                    deaths,
                    deathsYesterdayToday,
                    calculateCoronaLightColor(sevenDayIncidencePerOneHundredThousands.roundToInt().toFloat()))

    companion object {
        fun calculateCoronaLightColor(sevenDayIncidencePerOneHundredThousands: Float) =
                if (sevenDayIncidencePerOneHundredThousands < 35)
                    CoronaLightColor.GREEN
                else if (sevenDayIncidencePerOneHundredThousands < 50)
                    CoronaLightColor.YELLOW
                else
                    CoronaLightColor.RED
    }
}

enum class CoronaLightColor {
    GREEN,
    YELLOW,
    RED
}