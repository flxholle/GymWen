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

package com.ulan.timetable.appwidget.Dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * From https://github.com/SubhamTyagi/TimeTable
 */
class DBManager {

    private static final AtomicInteger sOpenCounter = new AtomicInteger();

    static synchronized SQLiteDatabase getDb(Context context) {
        sOpenCounter.incrementAndGet();
        return new DataBaseHelper(context).getWritableDatabase();
    }


    static synchronized void close(@Nullable SQLiteDatabase database) {
        if (sOpenCounter.decrementAndGet() == 0) {
            if (database != null) {
                database.close();
            }
        }
    }
}
