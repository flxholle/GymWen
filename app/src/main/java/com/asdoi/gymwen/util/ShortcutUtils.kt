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

package com.asdoi.gymwen.util

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.asdoi.gymwen.ApplicationFeatures
import com.asdoi.gymwen.R
import com.asdoi.gymwen.ui.activities.MainActivity
import com.asdoi.gymwen.ui.activities.RoomPlanActivity
import com.asdoi.gymwen.ui.activities.WebsiteActivity


@RequiresApi(25)
class ShortcutUtils {

    companion object {
        fun createShortcuts() {
            val context = ApplicationFeatures.getContext()
            val shortcutManager = context.getSystemService<ShortcutManager>(ShortcutManager::class.java)
            val shortcutList = mutableListOf<ShortcutInfo>()
            var shortcuts: Array<String>

            val default = context.resources.getStringArray(R.array.shortcuts_array_values_default)

            val shortcutsPreference = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getStringSet("shortcuts_array", null)
            if (shortcutsPreference == null)
                shortcuts = default
            else
                shortcuts = shortcutsPreference.toTypedArray()

            //Only max 4 App-Shortcuts can be displayed
            if (shortcuts.size > 4) {
                val s = mutableListOf<String>()
                for (i in 0 until 4) {
                    s.add(shortcuts[i])
                }
                shortcuts = s.toTypedArray()
            }

            for (s in shortcuts) {
                when (s) {
                    "website" -> shortcutList.add(createWebsiteShortcut())
                    "mebis" -> shortcutList.add(createMebisShortcut())
                    "mensa" -> shortcutList.add(createMensaShortcut())
                    "grades_management" -> shortcutList.add(createGradeManagementShortcut())
                    "call_office" -> shortcutList.add(createCallOfficeShortcut())
                    "navigation" -> shortcutList.add(createNavigationShortcut())
                    "claxss" -> shortcutList.add(createClaXssShortcut())
                    "forms" -> shortcutList.add(createFormsShortcut())
                    "newspaper" -> shortcutList.add(createNewsShortcut())
                    "teacherlist" -> shortcutList.add(createTeacherListShortcut())
                    "roomplan" -> shortcutList.add(createRoomPlanShortcut())
                }
            }

            shortcutManager!!.dynamicShortcuts = shortcutList
        }


        private fun createShortcut(id: String, shortLabel: String, icon: Icon, intent: Intent, context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return ShortcutInfo.Builder(context, id)
                    .setShortLabel(shortLabel)
                    .setIcon(icon)
                    .setIntent(intent)
                    .build()
        }

        fun createWebsiteShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("visit_website", context.getString(R.string.shortcut_website), Icon.createWithResource(context, R.drawable.ic_compass), Intent(context, WebsiteActivity::class.java).setAction(Intent.ACTION_VIEW))
        }

        fun createMebisShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("mebis", context.getString(R.string.shortcut_mebis), Icon.createWithResource(context, R.drawable.ic_graduate_cap), Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_MEBIS))
        }

        fun createMensaShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("mensa", context.getString(R.string.shortcut_mensa), Icon.createWithResource(context, R.drawable.ic_restaurant), Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_MENSA))
        }

        fun createTeacherListShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("teacher_list", context.getString(R.string.shortcut_teacher_list), Icon.createWithResource(context, R.drawable.ic_teacher_at_the_blackboard), Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_TEACHER_LIST))
        }

        fun createNavigationShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("navigation", context.getString(R.string.shortcut_navigation), Icon.createWithResource(context, R.drawable.ic_navigation_black_24dp), Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_NAVIGATION))
        }

        fun createCallOfficeShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("call_office", context.getString(R.string.shortcut_call_office), Icon.createWithResource(context, R.drawable.ic_call_black_24dp), Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_CALL_OFFICE))
        }

        fun createNewsShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("news", context.getString(R.string.shortcut_newspaper), Icon.createWithResource(context, R.drawable.ic_news), Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_NEWSPAPER))
        }

        fun createGradeManagementShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("grades", context.getString(R.string.shortcut_grades_management), Icon.createWithResource(context, R.drawable.ic_exam), Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_GRADES_MANAGEMENT))
        }

        fun createClaXssShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("claxss", context.getString(R.string.shortcut_claxss), Icon.createWithResource(context, R.drawable.ic_internet), Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_CLAXSS))
        }

        fun createFormsShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("forms", context.getString(R.string.shortcut_forms), Icon.createWithResource(context, R.drawable.ic_consent), Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_FORMS))
        }

        fun createRoomPlanShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("roomplan", context.getString(R.string.shortcut_room_plan), Icon.createWithResource(context, R.drawable.ic_house_plan), Intent(context, RoomPlanActivity::class.java).setAction(Intent.ACTION_VIEW))
        }
    }
}