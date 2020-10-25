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
                when (sevenDayIncidencePerOneHundredThousands.roundToInt()) {
                    in 0..35 -> CoronaLightColor.GREEN
                    in 35..50 -> CoronaLightColor.YELLOW
                    in 50..100 -> CoronaLightColor.RED
                    else -> CoronaLightColor.DEEP_RED
                }
    }
}

enum class CoronaLightColor {
    GREEN,
    YELLOW,
    RED,
    DEEP_RED
}