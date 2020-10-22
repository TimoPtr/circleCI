package com.kolibree.statsoffline.models

import io.kotlintest.properties.Gen
import io.kotlintest.properties.Gen.Companion.localDateTime
import io.kotlintest.properties.Gen.Companion.localTime
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import junit.framework.TestCase.assertEquals
import kotlin.random.Random
import org.threeten.bp.LocalDate
import org.threeten.bp.Year
import org.threeten.bp.temporal.ChronoUnit

class SanitizedDateRangePropertyTest : StringSpec({
    "if startDate and endDate are the same date" {
        shouldThrow<IllegalArgumentException> {
            assertAll(10_000, threeTenLocalDateGen()) { date ->
                SanitizedDateRange(date, date)
            }
        }
    }

    "if endDate is before startDate" {
        shouldThrow<IllegalArgumentException> {
            assertAll(threeTenLocalDateGen(), threeTenLocalDateGen()) { a: LocalDate, b: LocalDate ->
                if (a != b) {
                    if (a.isBefore(b)) SanitizedDateRange(b, a) else SanitizedDateRange(a, b)
                }
            }
        }
    }

    "if endDate is before startDate, construction should be safe" {
        assertAll(threeTenLocalDateGen(), threeTenLocalDateGen()) { a: LocalDate, b: LocalDate ->
            if (a != b) {
                if (a.isBefore(b)) SanitizedDateRange(a, b) else SanitizedDateRange(b, a)
            }
        }
    }

    "if endDate is before startDate, dates range should return all dates between start and end" {
        assertAll(threeTenLocalDateGen(), threeTenLocalDateGen()) { a: LocalDate, b: LocalDate ->
            if (a != b) {
                val dateRange =
                    if (a.isBefore(b)) SanitizedDateRange(a, b) else SanitizedDateRange(b, a)

                val startDate = dateRange.startDate
                val endDate = dateRange.endDate

                val datesInRange = dateRange.datesInRange()
                assertEquals(startDate, datesInRange.first())
                assertEquals(endDate, datesInRange.last())

                // +1 days otherwise Period doesn't include endDate
                val totalDays = ChronoUnit.DAYS.between(startDate, endDate.plusDays(1)).toInt()

                assertEquals(totalDays, datesInRange.size)

                val expectedLocalDates = (0L until totalDays).map { startDate.plusDays(it) }

                assertEquals(expectedLocalDates, datesInRange)
            }
        }
    }
})

/**
 * Generates a stream of random LocalDates
 *
 * This generator creates randomly generated LocalDates, in the range [[minYear, maxYear]].
 *
 * If any of the years in the range contain a leap year, the date [29/02/YEAR] will always be a constant value of this
 * generator.
 *
 * @see [localDateTime]
 * @see [localTime]
 */
fun threeTenLocalDateGen(minYear: Int = 1970, maxYear: Int = 2030): Gen<LocalDate> =
    object : Gen<LocalDate> {
        override fun constants(): Iterable<LocalDate> {
            val yearRange = (minYear..maxYear)
            val feb28Date = LocalDate.of(yearRange.random(), 2, 28)

            val feb29Year = yearRange.firstOrNull { Year.of(it).isLeap }
            val feb29Date = feb29Year?.let { LocalDate.of(it, 2, 29) }

            return listOfNotNull(
                feb28Date,
                feb29Date,
                LocalDate.of(minYear, 1, 1),
                LocalDate.of(maxYear, 12, 31)
            )
        }

        override fun random(): Sequence<LocalDate> = generateSequence {
            val minDate = LocalDate.of(minYear, 1, 1)
            val maxDate = LocalDate.of(maxYear, 12, 31)

            val days = ChronoUnit.DAYS.between(minDate, maxDate)

            minDate.plusDays(Random.nextLong(days + 1))
        }
    }
