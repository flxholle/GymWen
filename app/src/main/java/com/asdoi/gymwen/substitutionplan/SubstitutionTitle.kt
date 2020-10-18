package com.asdoi.gymwen.substitutionplan

import android.content.Context
import com.asdoi.gymwen.R
import com.asdoi.gymwen.util.PreferenceUtil
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import java.util.*

data class SubstitutionTitle(val date: LocalDate, val dayOfWeek: DayOfWeek, val week: WeekChar) {
    fun isPast(): Boolean {
        return LocalDate().isAfter(date)
    }

    fun isToday(): Boolean {
        return LocalDate().isEqual(date)
    }

    fun isTomorrow(): Boolean {
        val tomorrow = LocalDate().plusDays(1)
        return tomorrow.isEqual(date)
    }

    fun isFuture(): Boolean {
        return LocalDate().isBefore(date)
    }

    fun isCustomToday() =
            if (PreferenceUtil.isIntelligentHide())
                LocalDate().isEqual(date) && LocalTime.now().hourOfDay < PreferenceUtil.getIntelligentHideHour()
            else
                isToday()

    fun showDay(): Boolean {
        return !PreferenceUtil.isIntelligentHide() || (isCustomToday() || isFuture())
    }

    fun getStringWithoutDayOfWeek(past: String?, today: String?, tomorrow: String?, future: String?) =
            "${date.toString("dd.MM.yyyy")}, ${getTimeString(past, today, tomorrow, future)}, $week"

    fun getStringWithDayOfWeek(past: String?, today: String?, tomorrow: String?, future: String?, monday: String, tuesday: String, wednesday: String, thursday: String, friday: String, saturday: String, sunday: String) =
            "${date.toString("dd.MM.yyyy")}, ${getDayOfWeekString(past, today, tomorrow, future, monday, tuesday, wednesday, thursday, friday, saturday, sunday)}, $week"

    fun getTimeString(past: String?, today: String?, tomorrow: String?, future: String?) =
            if (isPast())
                past
            else if (isToday())
                today
            else if (isTomorrow())
                tomorrow
            else
                future

    fun getDayOfWeekString(past: String?, today: String?, tomorrow: String?, future: String?, monday: String, tuesday: String, wednesday: String, thursday: String, friday: String, saturday: String, sunday: String) =
            if (getTimeString(past, today, tomorrow, future) == null)
                getDayOfWeek(monday, tuesday, wednesday, thursday, friday, saturday, sunday)
            else
                "${getDayOfWeek(monday, tuesday, wednesday, thursday, friday, saturday, sunday)} (${getTimeString(past, today, tomorrow, future)})"

    fun getDayOfWeek(monday: String, tuesday: String, wednesday: String, thursday: String, friday: String, saturday: String, sunday: String) =
            when (dayOfWeek) {
                DayOfWeek.MONDAY -> monday
                DayOfWeek.TUESDAY -> tuesday
                DayOfWeek.WEDNESDAY -> wednesday
                DayOfWeek.THURSDAY -> thursday
                DayOfWeek.FRIDAY -> friday
                DayOfWeek.SATURDAY -> saturday
                DayOfWeek.SUNDAY -> sunday
            }


    //Context Help Methods
    private fun getDayOfWeek(context: Context) =
            getDayOfWeek(context.getString(R.string.monday), context.getString(R.string.tuesday), context.getString(R.string.wednesday), context.getString(R.string.thursday), context.getString(R.string.friday), context.getString(R.string.saturday), context.getString(R.string.sunday))

    fun getDayOfWeekString(context: Context) =
            if (PreferenceUtil.showDayOfWeek())
                getDayOfWeekString(context.getString(R.string.day_past), context.getString(R.string.today), context.getString(R.string.tomorrow), null, context.getString(R.string.monday), context.getString(R.string.tuesday), context.getString(R.string.wednesday), context.getString(R.string.thursday), context.getString(R.string.friday), context.getString(R.string.saturday), context.getString(R.string.sunday))
            else
                getTimeString(getDayOfWeek(context) + " (" + context.getString(R.string.day_past) + ")", context.getString(R.string.today), context.getString(R.string.tomorrow), getDayOfWeek(context))

    fun getStringWithDayOfWeek(context: Context) =
            getStringWithDayOfWeek(context.getString(R.string.day_past), context.getString(R.string.today), context.getString(R.string.tomorrow), null, context.getString(R.string.monday), context.getString(R.string.tuesday), context.getString(R.string.wednesday), context.getString(R.string.thursday), context.getString(R.string.friday), context.getString(R.string.saturday), context.getString(R.string.sunday))

    fun toString(context: Context, showDayOfWeek: Boolean) =
            if (showDayOfWeek)
                getStringWithDayOfWeek(context)
            else
                getStringWithoutDayOfWeek(getDayOfWeek(context) + " (" + context.getString(R.string.day_past) + ")", context.getString(R.string.today), context.getString(R.string.tomorrow), getDayOfWeek(context))
}

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    companion object {
        fun getMatchingDay(calendarInt: Int): DayOfWeek {
            return when (calendarInt) {
                Calendar.MONDAY -> MONDAY
                Calendar.TUESDAY -> TUESDAY
                Calendar.WEDNESDAY -> WEDNESDAY
                Calendar.THURSDAY -> THURSDAY
                Calendar.FRIDAY -> FRIDAY
                Calendar.SATURDAY -> SATURDAY
                Calendar.SUNDAY -> SUNDAY
                else -> MONDAY
            }
        }
    }
}

enum class WeekChar {
    WEEK_A, WEEK_B;

    override fun toString(): String {
        return when (this) {
            WEEK_A -> "A"
            WEEK_B -> "B"
        }
    }

    companion object {
        fun valueOf(value: String) =
                if (value.isNotEmpty())
                    valueOf(value[0])
                else
                    WEEK_B


        fun valueOf(weekChar: Char): WeekChar {
            return if (weekChar == 'A' || weekChar == 'a')
                WEEK_A
            else
                WEEK_B
        }
    }
}

