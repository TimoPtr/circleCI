/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.extensions

import com.kolibree.android.KOLIBREE_DAY_START_HOUR
import com.kolibree.android.clock.TrustedClock
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit

class DateExtensionsTest {

    @Test
    fun `ZonedDateTime to UTC milli`() {
        assertEquals(
            1000,
            ZonedDateTime.of(1970, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC).toUTCEpochMilli()
        )
    }

    @Test
    fun `ZonedDateTime to milli`() {
        assertEquals(1000, ZonedDateTime.of(1970, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC).toEpochMilli())
    }

    @Test
    fun `OffsetDateTime to milli`() {
        assertEquals(1000, OffsetDateTime.of(1970, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC).toEpochMilli())
    }

    @Test
    fun `Long to ZonedDateTime`() {
        assertEquals(
            ZonedDateTime.of(1970, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC),
            1000L.toZonedDateTimeFromUTC()
        )
    }

    @Test
    fun `LocalDate atEndOfDay returns same day at 23 59 59`() {
        val localDate = LocalDate.now().minusDays(3)
        val expectedLocalDateTime = localDate.atTime(23, 59, 59)

        assertEquals(expectedLocalDateTime, localDate.atEndOfDay())
    }

    @Test
    fun `LocalDateTime to kolibree day returns same date if time is after 4AM`() {
        val fourThirtyAM = midnight().withHour(4).withMinute(30)
        val elevenPM = midnight().withHour(23)

        val expectedDate = TrustedClock.getNowLocalDate()

        assertEquals(expectedDate, fourThirtyAM.toKolibreeDay())
        assertEquals(expectedDate, elevenPM.toKolibreeDay())
    }

    @Test
    fun `LocalDateTime to kolibree day returns same date if time is 4AM`() {
        val currentTime = midnight().withHour(4)

        val expectedDate = TrustedClock.getNowLocalDate()

        assertEquals(expectedDate, currentTime.toKolibreeDay())
    }

    @Test
    fun `LocalDateTime to kolibree day returns previous date if time is before 4AM`() {
        val threeFiftyNine = midnight()
            .withHour(3)
            .withMinute(59)
            .withSecond(59)
        val oneAM = midnight().withHour(1)

        val expectedDate = TrustedClock.getNowLocalDate().minusDays(1)

        assertEquals(expectedDate, threeFiftyNine.toKolibreeDay())
        assertEquals(expectedDate, oneAM.toKolibreeDay())
        assertEquals(expectedDate, midnight().toKolibreeDay())
    }

    /*
    atStartOfKolibreeDay
     */

    @Test
    fun `atStartOfKolibreeDay returns same date at right time and offset`() {
        val expectedYear = 1986
        val expectedMonth = 6
        val expectedDay = 5

        val offsetDateTime = LocalDate.of(expectedYear, expectedMonth, expectedDay)
            .atStartOfKolibreeDay()

        assertEquals(expectedYear, offsetDateTime.year)
        assertEquals(expectedMonth, offsetDateTime.monthValue)
        assertEquals(expectedDay, offsetDateTime.dayOfMonth)
        assertEquals(KOLIBREE_DAY_START_HOUR, offsetDateTime.hour)
    }

    @Test
    fun `LocalDate correctly checks first day of the month`() {
        assertTrue(LocalDate.of(2019, 3, 1).isFirstDayOfTheMonth())
        assertTrue(LocalDate.of(2019, 12, 1).isFirstDayOfTheMonth())
        assertFalse(LocalDate.of(2019, 3, 2).isFirstDayOfTheMonth())
        assertFalse(LocalDate.of(2019, 3, 31).isFirstDayOfTheMonth())
    }

    @Test
    fun `LocalDate correctly checks last day of the month`() {
        assertTrue(LocalDate.of(2019, 1, 31).isLastDayOfTheMonth())
        assertTrue(LocalDate.of(2019, 2, 28).isLastDayOfTheMonth())
        assertTrue(LocalDate.of(2019, 3, 31).isLastDayOfTheMonth())
        assertTrue(LocalDate.of(2019, 8, 31).isLastDayOfTheMonth())
        assertTrue(LocalDate.of(2019, 12, 31).isLastDayOfTheMonth())
        assertFalse(LocalDate.of(2019, 1, 30).isLastDayOfTheMonth())
        assertFalse(LocalDate.of(2019, 2, 1).isLastDayOfTheMonth())
        assertFalse(LocalDate.of(2019, 3, 1).isLastDayOfTheMonth())
        assertFalse(LocalDate.of(2019, 8, 30).isLastDayOfTheMonth())
    }

    @Test
    fun `LocalDate correctly checks last day of the month for the leap year`() {
        assertFalse(LocalDate.of(2019, 2, 27).isLastDayOfTheMonth())
        assertTrue(LocalDate.of(2019, 2, 28).isLastDayOfTheMonth())
        assertFalse(LocalDate.of(2020, 2, 28).isLastDayOfTheMonth())
        assertTrue(LocalDate.of(2020, 2, 29).isLastDayOfTheMonth())
    }

    @Test
    fun `toCurrentTimeZone change the TZ to the current system one`() {
        val utcOffsetDateTime = OffsetDateTime.now(TrustedClock.utcClock)
        assertEquals(ZoneOffset.UTC, utcOffsetDateTime.offset)

        TrustedClock.systemZone = ZoneOffset.MAX

        val currentDateTime = utcOffsetDateTime.toCurrentTimeZone()
        assertEquals(TrustedClock.systemZoneOffset, currentDateTime.offset)
    }

    private fun midnight() = TrustedClock.getNowLocalDateTime().truncatedTo(ChronoUnit.DAYS)
}
