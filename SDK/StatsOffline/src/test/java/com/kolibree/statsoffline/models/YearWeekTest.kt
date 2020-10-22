/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.models

import com.kolibree.android.app.test.BaseUnitTest
import java.util.Locale
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.Month.DECEMBER
import org.threeten.bp.Month.JANUARY

class YearWeekTest : BaseUnitTest() {
    @Test(expected = DateTimeException::class)
    fun `throws DateTimeException if week is over 54`() {
        YearWeek(year = 2018, week = 55)
    }

    @Test(expected = DateTimeException::class)
    fun `throws DateTimeException if week is below 0`() {
        YearWeek(year = 2017, week = -1)
    }

    @Test
    fun `create below 55`() {
        YearWeek(year = 2018, week = 53)
        YearWeek(year = 2018, week = 54)
    }

    @Test
    fun `create 0`() {
        YearWeek(year = 2018, week = 0)
    }

    /*
    from LocalDate
     */
    @Test
    fun `from creates expected YearWeek for 2018 and Locale FRANCE`() {
        Locale.setDefault(Locale.FRANCE)

        val year = 2018
        assertEquals(YearWeek(year = year, week = 53), YearWeek.from(LocalDate.of(year, DECEMBER.value, 31)))
        assertEquals(YearWeek(year = year, week = 1), YearWeek.from(LocalDate.of(year, JANUARY.value, 1)))
    }

    @Test
    fun `from creates expected YearWeek for 2018 and Locale US`() {
        Locale.setDefault(Locale.US)

        val year = 2018
        assertEquals(YearWeek(year = year, week = 53), YearWeek.from(LocalDate.of(year, DECEMBER.value, 31)))
        assertEquals(YearWeek(year = year, week = 1), YearWeek.from(LocalDate.of(year, JANUARY.value, 1)))
    }

    @Test
    fun `from creates expected YearWeek for 2017 and Locale FRANCE`() {
        Locale.setDefault(Locale.FRANCE)

        val year = 2017

        assertEquals(YearWeek(year = year, week = 52), YearWeek.from(LocalDate.of(year, DECEMBER.value, 31)))
        assertEquals(YearWeek(year = year, week = 0), YearWeek.from(LocalDate.of(year, JANUARY.value, 1)))
        assertEquals(YearWeek(year = year, week = 1), YearWeek.from(LocalDate.of(year, JANUARY.value, 2)))
        assertEquals(YearWeek(year = year, week = 1), YearWeek.from(LocalDate.of(year, JANUARY.value, 8)))
    }

    @Test
    fun `from creates expected YearWeek for USA`() {
        Locale.setDefault(Locale.US)

        val year2017 = 2017
        val year2016 = 2016

        assertEquals(YearWeek(year = year2017, week = 53), YearWeek.from(LocalDate.of(year2017, DECEMBER.value, 31)))
        assertEquals(YearWeek(year = year2017, week = 1), YearWeek.from(LocalDate.of(year2017, JANUARY.value, 1)))
        assertEquals(YearWeek(year = year2017, week = 1), YearWeek.from(LocalDate.of(year2017, JANUARY.value, 2)))
        assertEquals(YearWeek(year = year2017, week = 2), YearWeek.from(LocalDate.of(year2017, JANUARY.value, 8)))

        // January 1st is Friday, but there's no Week 0 in USA since minimalDaysForFirstWeek=1
        assertEquals(YearWeek(year = year2016, week = 1), YearWeek.from(LocalDate.of(year2016, JANUARY.value, 1)))
        assertEquals(YearWeek(year = year2016, week = 2), YearWeek.from(LocalDate.of(year2016, JANUARY.value, 3)))
    }

    /*
    toString
     */

    @Test
    fun `toString returns expected values`() {
        assertEquals("2018-W53", YearWeek(year = 2018, week = 53).toString())
        assertEquals("2017-W53", YearWeek(year = 2017, week = 53).toString())
        assertEquals("2018-W54", YearWeek(year = 2018, week = 54).toString())
        assertEquals("2017-W00", YearWeek(year = 2017, week = 0).toString())
        assertEquals("2017-W01", YearWeek(year = 2017, week = 1).toString())
    }

    /*
    parse
     */

    @Test
    fun `parse converts to expected values`() {
        assertEquals(YearWeek(year = 2018, week = 53), YearWeek.parse("2018-W53"))
        assertEquals(YearWeek(year = 2017, week = 53), YearWeek.parse("2017-W53"))
        assertEquals(YearWeek(year = 2018, week = 54), YearWeek.parse("2018-W54"))
        assertEquals(YearWeek(year = 2017, week = 0), YearWeek.parse("2017-W00"))
        assertEquals(YearWeek(year = 2017, week = 1), YearWeek.parse("2017-W01"))
    }

    @Test
    fun `parse and to string work as expected`() {
        val yearAndWeek = YearWeek(year = 2018, week = 53)

        assertEquals(yearAndWeek, YearWeek.parse(yearAndWeek.toString()))
    }

    @Test
    fun `parse can parse value even if week is not zero padded`() {
        YearWeek.parse("2018-W2")
    }

    @Test(expected = YearAndWeekParseException::class)
    fun `parse throws YearAndWeekParseException if string is empty`() {
        YearWeek.parse("")
    }

    @Test(expected = YearAndWeekParseException::class)
    fun `parse throws YearAndWeekParseException if string is random`() {
        YearWeek.parse("dasdsada")
    }

    @Test(expected = YearAndWeekParseException::class)
    fun `parse throws YearAndWeekParseException if string dos not contain week`() {
        YearWeek.parse("2018W-")
    }

    @Test(expected = YearAndWeekParseException::class)
    fun `parse throws YearAndWeekParseException if string does not contain year`() {
        YearWeek.parse("W48")
    }

    @Test(expected = YearAndWeekParseException::class)
    fun `parse throws YearAndWeekParseException if string is unexpected`() {
        YearWeek.parse("2018-W48-W02")
    }

    @Test(expected = DateTimeException::class)
    fun `parse throws DateTimeException if week is 55`() {
        YearWeek.parse("2018-W55")
    }

    @Test(expected = DateTimeException::class)
    fun `parse throws DateTimeException if week is negative`() {
        YearWeek.parse("2018-W-1")
    }

    /*
    dates
     */

    @Test
    fun `dates returns expected dates for week zero of 2016 for FRANCE`() {
        Locale.setDefault(Locale.FRANCE)

        val year = 2016
        val january1st = LocalDate.of(year, Month.JANUARY.value, 1)

        val expectedDates = listOf(
            january1st,
            january1st.plusDays(1),
            january1st.plusDays(2)
        )

        assertEquals(YearWeek.of(year, 0), YearWeek.from(january1st))

        assertEquals(expectedDates, YearWeek.of(year, 0).dates())
    }

    @Test
    fun `dates returns expected dates for week one of 2016 for US`() {
        /*
        USA sets minimalDaysInWeek=1, thus they don't have week zero!!
         */
        Locale.setDefault(Locale.US)

        val year = 2016
        val january1st = LocalDate.of(year, Month.JANUARY.value, 1)

        val expectedDates = listOf(
            january1st,
            january1st.plusDays(1)
        )

        assertEquals(expectedDates, YearWeek.from(january1st).dates())
    }

    @Test
    fun `dates returns expected dates for week two of 2016 for US`() {
        /*
        USA sets minimalDaysInWeek=1, thus they don't have week zero!!
         */
        Locale.setDefault(Locale.US)

        val year = 2016
        val january3rd = LocalDate.of(year, Month.JANUARY.value, 3)

        val expectedDates = listOf(
            january3rd,
            january3rd.plusDays(1),
            january3rd.plusDays(2),
            january3rd.plusDays(3),
            january3rd.plusDays(4),
            january3rd.plusDays(5),
            january3rd.plusDays(6)
        )

        assertEquals(expectedDates, YearWeek.from(january3rd).dates())
    }

    @Test
    fun `dates returns expected dates for second week of 2019 for FRANCE`() {
        Locale.setDefault(Locale.FRANCE)

        val january7th = LocalDate.of(2019, Month.JANUARY.value, 7)

        val expectedDates = listOf(
            january7th,
            january7th.plusDays(1),
            january7th.plusDays(2),
            january7th.plusDays(3),
            january7th.plusDays(4),
            january7th.plusDays(5),
            january7th.plusDays(6)
        )

        assertEquals(expectedDates, YearWeek.of(2019, 2).dates())
    }

    @Test
    fun `dates returns expected dates for second week of 2019 for US`() {
        Locale.setDefault(Locale.US)

        val january7th = LocalDate.of(2019, Month.JANUARY.value, 7)

        val expectedDates = listOf(
            january7th.minusDays(1),
            january7th,
            january7th.plusDays(1),
            january7th.plusDays(2),
            january7th.plusDays(3),
            january7th.plusDays(4),
            january7th.plusDays(5)
        )

        assertEquals(expectedDates, YearWeek.of(2019, 2).dates())
    }

    @Test
    fun `dates does not return past year dates for 1st week of 2019 for FRANCE`() {
        Locale.setDefault(Locale.FRANCE)

        val january1st = LocalDate.of(2019, Month.JANUARY.value, 1)

        val expectedDates = listOf(
            january1st,
            january1st.plusDays(1),
            january1st.plusDays(2),
            january1st.plusDays(3),
            january1st.plusDays(4),
            january1st.plusDays(5)
        )

        assertEquals(expectedDates, YearWeek.of(2019, 1).dates())
    }
}
