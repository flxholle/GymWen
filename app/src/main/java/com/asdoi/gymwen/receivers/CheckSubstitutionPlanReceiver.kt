package com.asdoi.gymwen.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.asdoi.gymwen.ApplicationFeatures
import java.util.*

class CheckSubstitutionPlanReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        ApplicationFeatures.initSubstitutionPlanReceiver()
        when (intent?.action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED ->
                return
        }
        ApplicationFeatures.checkSubstitutionPlan(true)
    }

    companion object {
        private val times = listOf(listOf(6, 30, 0),
                listOf(6, 50, 0),
                listOf(7, 10, 0),
                listOf(7, 30, 0),
                listOf(7, 45, 0),
                listOf(12, 0, 0),
                listOf(13, 30, 0),
                listOf(17, 0, 0),
                listOf(20, 0, 0),
                listOf(23, 0, 0))

        fun getNextTime(): List<Int> {
            val today = Calendar.getInstance()
            for (time in times) {
                val customCalendar = Calendar.getInstance()
                customCalendar[Calendar.HOUR_OF_DAY] = time[0]
                customCalendar[Calendar.MINUTE] = time[1]
                customCalendar[Calendar.SECOND] = time[2]

                if (customCalendar.after(today)) {
                    return time
                }
            }
            return times[0]
        }
    }
}