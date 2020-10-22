package com.kolibree.statsoffline.models

import androidx.annotation.Keep
import org.threeten.bp.LocalDate

/**
 * Represents a period of days between [startDate] and [endDate]
 *
 * Implementors of the interface are responsible for defining the sanitized state of the range
 */
@Keep
interface DateRange {
    val startDate: LocalDate
    val endDate: LocalDate

    /**
     * Returns a List<[LocalDate]> between startDate and endDate, both included
     */
    fun datesInRange(): List<LocalDate>
}

/**
 * Sanitized implementation of [DateRange]
 *
 * Guarantees that [startDate] is always before [endDate]. Does not support [startDate] equal to [endDate]
 */
internal data class SanitizedDateRange(override val startDate: LocalDate, override val endDate: LocalDate) : DateRange {
    init {
        validate()
    }

    /**
     * @throws [IllegalArgumentException] if startDate isn't before endDate
     */
    private fun validate() {
        if (endDate.isAfter(startDate).not()) throw IllegalArgumentException("startDate should be before endDate")
    }

    /**
     * Returns a List<[LocalDate]> between startDate and endDate, both included
     */
    override fun datesInRange(): List<LocalDate> {
        val dateRange: MutableList<LocalDate> = mutableListOf()
        var date = startDate
        while (date.isAfter(endDate).not()) {
            dateRange.add(date)

            date = date.plusDays(1)
        }

        return dateRange.toList()
    }
}
