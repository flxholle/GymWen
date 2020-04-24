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
import android.graphics.Color
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.PreferenceManager
import com.asdoi.gymwen.ApplicationFeatures
import com.asdoi.gymwen.R
import com.asdoi.gymwen.ui.activities.MainActivity
import com.asdoi.gymwen.ui.activities.RoomPlanActivity
import com.asdoi.gymwen.ui.activities.TeacherListActivity
import com.asdoi.gymwen.ui.activities.WebsiteActivity


@RequiresApi(25)
class ShortcutUtils {

    companion object {
        private val background = ContextCompat.getDrawable(ApplicationFeatures.getContext(), R.drawable.shortcuts_background)
        private const val maxShortcuts = 4

        fun createShortcuts() {
            val context = ApplicationFeatures.getContext()
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            val shortcutList = mutableListOf<ShortcutInfo>()
            var shortcuts: Array<String>

            val default = if (PreferenceUtil.isParents()) context.resources.getStringArray(R.array.shortcuts_array_values_default_parent_mode) else context.resources.getStringArray(R.array.shortcuts_array_values_default)

            val shortcutsPreference = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getStringSet("shortcuts_array", null)
            shortcuts = shortcutsPreference?.toTypedArray() ?: default

            //Only max 4 App-Shortcuts can be displayed
            if (shortcuts.size > maxShortcuts) {
                val s = mutableListOf<String>()
                for (i in 0 until maxShortcuts) {
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
                    "roomplan_search" -> shortcutList.add(createSearchRoomPlanShortcut())
                }
            }

            shortcutManager!!.dynamicShortcuts = shortcutList
        }


        private const val size = 256
        private const val padding = 65
        private fun createShortcut(id: String, shortLabel: String, iconId: Int, intent: Intent, context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            val icon = ContextCompat.getDrawable(context, iconId)
            icon?.setTint(Color.WHITE)
            val combined = LayerDrawable(arrayOf(background, icon))
            combined.setLayerInset(1, padding, padding, padding, padding)

            val combinedIcon = if (Build.VERSION.SDK_INT > 25) Icon.createWithAdaptiveBitmap(combined.toBitmap(size, size)) else Icon.createWithBitmap(combined.toBitmap(size, size))

            return ShortcutInfo.Builder(context, id)
                    .setShortLabel(shortLabel)
                    .setIcon(combinedIcon)
                    .setIntent(intent)
                    .build()
        }

        private fun createWebsiteShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("visit_website", context.getString(R.string.shortcut_website), R.drawable.ic_compass, Intent(context, WebsiteActivity::class.java).setAction(Intent.ACTION_VIEW))
        }

        private fun createMebisShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("mebis", context.getString(R.string.shortcut_mebis), R.drawable.ic_graduate_cap, Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_MEBIS))
        }

        private fun createMensaShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("mensa", context.getString(R.string.shortcut_mensa), R.drawable.ic_restaurant, Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_MENSA))
        }

        private fun createTeacherListShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("teacher_list", context.getString(R.string.shortcut_teacher_list), R.drawable.ic_teacher_at_the_blackboard, Intent(context, TeacherListActivity::class.java).setAction(Intent.ACTION_VIEW))
        }

        private fun createNavigationShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("navigation", context.getString(R.string.shortcut_navigation), R.drawable.ic_navigation_black_24dp, Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_NAVIGATION))
        }

        private fun createCallOfficeShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("call_office", context.getString(R.string.shortcut_call_office), R.drawable.ic_call_black_24dp, Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_CALL_OFFICE))
        }

        private fun createNewsShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("news", context.getString(R.string.shortcut_newspaper), R.drawable.ic_news, Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_NEWSPAPER))
        }

        private fun createGradeManagementShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("grades", context.getString(R.string.shortcut_grades_management), R.drawable.ic_exam, Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_GRADES_MANAGEMENT))
        }

        private fun createClaXssShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("claxss", context.getString(R.string.shortcut_claxss), R.drawable.ic_internet, Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_CLAXSS))
        }

        private fun createFormsShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("forms", context.getString(R.string.shortcut_forms), R.drawable.ic_consent, Intent(context, MainActivity::class.java).setAction(MainActivity.SHORTCUT_ACTION_FORMS))
        }

        private fun createRoomPlanShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("roomplan", context.getString(R.string.shortcut_room_plan), R.drawable.ic_house_plan, Intent(context, RoomPlanActivity::class.java).setAction(Intent.ACTION_VIEW))
        }

        private fun createSearchRoomPlanShortcut(context: Context = ApplicationFeatures.getContext()): ShortcutInfo {
            return createShortcut("roomplan_search", context.getString(R.string.shortcut_room_plan_search), R.drawable.ic_search_black_24dp, Intent(context, RoomPlanActivity::class.java).setAction(RoomPlanActivity.SEARCH_ROOM))
        }
    }
}