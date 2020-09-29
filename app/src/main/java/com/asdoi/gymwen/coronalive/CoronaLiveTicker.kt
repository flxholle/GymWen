package com.asdoi.gymwen.coronalive

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
                    calculateCoronaLightColor(infectionsInTheLastSevenDays))

    companion object {
        fun calculateCoronaLightColor(infectionsInTheLastSevenDays: Int) =
                if (infectionsInTheLastSevenDays < 20)
                    CoronaLightColor.GREEN
                else if (infectionsInTheLastSevenDays < 30)
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