package com.asdoi.gymwen.substitutionplan

class SubstitutionEntry(val course: String, val hour: Int, val subject: String, val teacher: String, val room: String, val moreInformation: String)

class SubstitutionTitle(var date: String = "", var dayOfWeek: String = "", var week: String = "", var titleCode: Int = -1) {
    private var noInternet: Boolean = false

    constructor(noInternet: Boolean) : this() {
        this.noInternet = true
    }

    override fun toString(): String {
        return "$date, $dayOfWeek, $week"
    }

    fun getNoInternet(): Boolean {
        return noInternet
    }
}
