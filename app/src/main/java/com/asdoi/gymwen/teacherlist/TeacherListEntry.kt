/*
 * Copyright (c) 2020 Felix Hollederer
 *     This file is part of GymWenApp.
 *
 *     GymWenApp is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GymWenApp is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GymWenApp.  If not, see <https://www.gnu.org/licenses/>.
 */

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