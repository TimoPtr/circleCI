/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.models

import androidx.annotation.Keep
import com.kolibree.android.clock.TrustedClock
import java.io.Serializable
import java.util.Locale
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.IsoFields.WEEK_BASED_YEAR
import org.threeten.bp.temporal.TemporalField
import org.threeten.bp.temporal.WeekFields

/**
 * This represents the concept of the weeks within the year where weeks
 * start on a fixed day-of-week, such as Monday.
 *
 * Week one(1) is the week starting on the [WeekFields.getFirstDayOfWeek]
 * where there are at least [WeekFields.getMinimalDaysInFirstWeek()] days in the year.
 * Thus, week one may start up to {@code minDays} days before the start of the year.
 * If the first week starts after the start of the year then the period before is week zero (0).
 *
 * Assuming ISO-8601 Locale that requires 4 days in first week:<br>
 * - if the 1st day of the year is a Monday, week one starts on the 1st and there is no week zero<br>
 * - if the 2nd day of the year is a Monday, week one starts on the 2nd and the 1st is in week zero<br>
 * - if the 4th day of the year is a Monday, week one starts on the 4th and the 1st to 3rd is in week zero<br>
 * - if the 5th day of the year is a Monday, week two starts on the 5th and the 1st to 4th is in week one<br>
 *
 * The value returned by Locale.getMinimalDaysInFirstWeek is the key. For example, Locale.US returns
 * getMinimalDaysInFirstWeek equals to 1, thus there's no Week 0. Even if January 1st is on Saturday, it'll be
 * considered as Week 1.
 */
@Keep
data class YearWeek(val year: Int, val week: Int) : Serializable {
    init {
        validateYear()
        validateWeek()
    }

    private fun validateWeek() {
        weekOfYearField().apply {
            range().checkValidValue(week.toLong(), this)
        }
    }

    private fun validateYear() {
        WEEK_BASED_YEAR.range().checkValidValue(year.toLong(), WEEK_BASED_YEAR)
    }

    override fun toString(): String {
        val weekZeroPadded = String.format(WEEK_ZERO_PADDED_FORMATTER, week)
        return "$year$SEPARATOR$weekZeroPadded"
    }

    internal fun dates(): List<LocalDate> {
        val weekField = WeekFields.of(Locale.getDefault())
        val weekOfYearField: TemporalField = weekField.weekOfYear()
        val now = TrustedClock.getNowLocalDate()
        var dateAtStartOfWeek = now.withYear(year).with(weekOfYearField, week.toLong())

        val firstDayOfWeek = weekField.firstDayOfWeek
        while (dateAtStartOfWeek.dayOfWeek != firstDayOfWeek) {
            dateAtStartOfWeek = dateAtStartOfWeek.minusDays(1)
        }

        return (0 until DAYS_IN_WEEK).map { dateAtStartOfWeek.plusDays(it) }.filter { it.year == year }
    }

    companion object {
        private fun weekField() = WeekFields.of(Locale.getDefault())
        private fun weekOfYearField(): TemporalField = weekField().weekOfYear()

        private const val WEEK_ZERO_PADDED_FORMATTER = "%02d"
        private const val SEPARATOR: String = "-W"

        /**
         * Obtains an instance of {@code YearWeek} from a text string such as {@code 2007-W13}.
         *
         * <p>The string must represent a valid year and week.
         *
         * @param value the text to parse such as "2007-W13", not null
         * @return the parsed year and week, not null
         * @throws YearAndWeekParseException if the text cannot be parsed
         * @throws DateTimeException if year or week values are invalid
         */

        @JvmStatic
        fun parse(value: String): YearWeek {
            val split = value.trim().split(SEPARATOR)

            if (split.size != 2) throw YearAndWeekParseException("Unable to convert $value to YearWeek")

            return YearWeek(year = split[0].toInt(), week = split[1].toInt())
        }

        @JvmStatic
        fun from(date: LocalDate): YearWeek {
            return YearWeek(year = date.year, week = date.get(weekOfYearField()))
        }

        @JvmStatic
        fun now(): YearWeek {
            return from(TrustedClock.getNowLocalDate())
        }

        @JvmStatic
        fun of(year: Int, week: Int) = YearWeek(year = year, week = week)
    }
}

@Keep
class YearAndWeekParseException(message: String) : Exception(message)

private const val DAYS_IN_WEEK = 7L
