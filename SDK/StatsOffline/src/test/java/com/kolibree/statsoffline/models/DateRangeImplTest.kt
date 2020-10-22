package com.kolibree.statsoffline.models

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate

class DateRangeImplTest : BaseUnitTest() {
    /*
    validation
     */

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws IllegalArgumentException if startDate and endDate are on the same day`() {
        val date = TrustedClock.getNowLocalDate()

        SanitizedDateRange(date, date)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws IllegalArgumentException if startDate is after endDate`() {
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.plusDays(1)

        SanitizedDateRange(startDate, endDate)
    }

    /*
    datesInRange
     */

    @Test
    fun `dateRangeBetween returns list of LocalDate with all dates in the period`() {
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(5)

        val expectedDates = listOf<LocalDate>(
            startDate,
            startDate.plusDays(1),
            startDate.plusDays(2),
            startDate.plusDays(3),
            startDate.plusDays(4),
            endDate
        )

        assertEquals(expectedDates, SanitizedDateRange(startDate, endDate).datesInRange())
    }
}
