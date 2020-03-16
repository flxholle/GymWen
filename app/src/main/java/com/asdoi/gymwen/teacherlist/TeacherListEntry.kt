package com.asdoi.gymwen.teacherlist


class TeacherList(var entries: MutableList<TeacherListEntry> = mutableListOf<TeacherListEntry>()) {
    private var noInternet: Boolean = false

    constructor(noInternet: Boolean) : this() {
        this.noInternet = true
    }

    fun add(entry: TeacherListEntry) {
        entries.add(entry)
    }

    fun getNoInternet(): Boolean {
        return noInternet
    }

    fun size(): Int {
        return entries.size
    }

    fun isEmpty(): Boolean {
        return entries.isEmpty()
    }
}

class TeacherListEntry(val short: String, val name: String, val first_name: String, val meeting: String)