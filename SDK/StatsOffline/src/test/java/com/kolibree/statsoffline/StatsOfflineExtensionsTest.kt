package com.kolibree.statsoffline

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.statsoffline.models.YearWeek
import java.util.Locale
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.Month

internal class StatsOfflineExtensionsTest : BaseUnitTest() {

    /*
    dateRangeBetween
     */

    @Test(expected = IllegalArgumentException::class)
    fun `dateRangeBetween throws IllegalArgumentException if startDate and endDate are on the same day`() {
        val date = TrustedClock.getNowLocalDate()

        dateRangeBetween(date, date)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `dateRangeBetween throws IllegalArgumentException if startDate is after endDate`() {
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.plusDays(1)

        dateRangeBetween(startDate, endDate)
    }

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

        assertEquals(expectedDates, dateRangeBetween(startDate, endDate))
    }

    /*
    toYearWeek
     */
    @Test
    fun `31-12-2017 is week 52 of 2017 for FRANCE`() {
        Locale.setDefault(Locale.FRANCE)

        val year = 2017
        val date = LocalDate.of(year, Month.DECEMBER.value, 31)

        val expectedWeek = YearWeek.of(year, 52)

        assertEquals(expectedWeek, date.toYearWeek())
    }

    @Test
    fun `31-12-2017 is week 53 of 2017 for US`() {
        Locale.setDefault(Locale.US)

        val year = 2017
        val date = LocalDate.of(year, Month.DECEMBER.value, 31)

        val expectedWeek = YearWeek.of(year, 53)

        assertEquals(expectedWeek, date.toYearWeek())
    }

    @Test
    fun `31-12-2018 is week 53 of 2018 in FRANCE`() {
        Locale.setDefault(Locale.FRANCE)

        val date = LocalDate.of(2018, Month.DECEMBER.value, 31)

        val expectedWeek = YearWeek.of(2018, 53)

        assertEquals(expectedWeek, date.toYearWeek())
    }

    @Test
    fun `1-1-2019 is week 1 of 2019 in FRANCE`() {
        Locale.setDefault(Locale.FRANCE)

        val date = LocalDate.of(2019, Month.JANUARY.value, 1)

        val expectedWeek = YearWeek.of(2019, 1)

        assertEquals(expectedWeek, date.toYearWeek())
    }

    @Test
    fun `1-1-2017 is week 0 of 2017 in FRANCE`() {
        Locale.setDefault(Locale.FRANCE)

        val date = LocalDate.of(2017, Month.JANUARY.value, 1)

        val expectedWeek = YearWeek.of(2017, 0)

        assertEquals(expectedWeek, date.toYearWeek())
    }

    @Test
    fun `1-1-2017 is week 1 of 2017 in US`() {
        Locale.setDefault(Locale.US)

        val date = LocalDate.of(2017, Month.JANUARY.value, 1)

        val expectedWeek = YearWeek.of(2017, 1)

        assertEquals(expectedWeek, date.toYearWeek())
    }

    @Test
    fun `Double isNonZero return true if it is non-zero number`() {
        val num = 1.0
        assert(num.isNonZero())
    }

    @Test
    fun `Double isNonZero return false if number is zero`() {
        val num = 0.0
        assert(!num.isNonZero())
    }
}
