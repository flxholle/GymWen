package com.asdoi.gymwen.coronalive

import kotlin.math.roundToInt

data class CoronaTicker(val infections: Int,
                        val infectionsYesterdayToday: Int,
                        val infectionsPerOneHundredThousands: Float,
                        val infectionsInTheLastSevenDays: Int,
                        val sevenDayIncidencePerOneHundredThousands: Float,
                        val deaths: Int,
                        val deathsYesterdayToday: Int,
                        val coronaLightColor: CoronaLightColor) {

    constructor(infections: Int,
                infectionsYesterdayToday: Int,
                infectionsPerOneHundredThousands: Float,
                infectionsInTheLastSevenDays: Int,
                sevenDayIncidencePerOneHundredThousands: Float,
                deaths: Int,
                deathsYesterdayToday: Int) :
            this(infections,
                    infectionsYesterdayToday,
                    infectionsPerOneHundredThousands,
                    infectionsInTheLastSevenDays,
                    sevenDayIncidencePerOneHundredThousands,
                    deaths,
                    deathsYesterdayToday,
                    calculateCoronaLightColor(sevenDayIncidencePerOneHundredThousands.roundToInt()))

    companion object {
        fun calculateCoronaLightColor(sevenDayIncidencePerOneHundredThousands: Int) =
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