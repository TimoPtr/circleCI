/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.clock

import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class TrustedClockTest {

    val date = ZonedDateTime.of(1970, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC)

    @Before
    fun setUp() {
        TrustedClock.systemZone = ZoneId.systemDefault()
    }

    @Test
    fun `test get ZonedDateTime`() {
        TrustedClock.utcClock = Clock.fixed(date.toInstant(), date.zone)
        assertEquals(ZonedDateTime.now(TrustedClock.utcClock.withZone(ZoneId.systemDefault())), TrustedClock.getNowZonedDateTime())
    }

    @Test
    fun `test get OffsetDateTime`() {
        TrustedClock.utcClock = Clock.fixed(date.toInstant(), date.zone)
        assertEquals(OffsetDateTime.now(TrustedClock.utcClock.withZone(ZoneId.systemDefault())), TrustedClock.getNowOffsetDateTime())
    }

    @Test
    fun `test get ZonedDateTime UTC`() {
        TrustedClock.utcClock = Clock.fixed(date.toInstant(), date.zone)
        assertEquals(ZonedDateTime.now(TrustedClock.utcClock), TrustedClock.getNowZonedDateTimeUTC())
    }

    @Test
    fun `test get LocalDateTime`() {
        TrustedClock.utcClock = Clock.fixed(date.toInstant(), date.zone)
        assertEquals(LocalDateTime.now(TrustedClock.utcClock.withZone(ZoneId.systemDefault())), TrustedClock.getNowLocalDateTime())
    }

    @Test
    fun `test get LocalTime`() {
        TrustedClock.utcClock = Clock.fixed(date.toInstant(), date.zone)
        assertEquals(LocalTime.now(TrustedClock.utcClock.withZone(ZoneId.systemDefault())), TrustedClock.getNowLocalTime())
    }

    @Test
    fun `test get LocalDate`() {
        TrustedClock.utcClock = Clock.fixed(date.toInstant(), date.zone)
        assertEquals(LocalDate.now(TrustedClock.utcClock.withZone(ZoneId.systemDefault())), TrustedClock.getNowLocalDate())
    }

    @Test
    fun `test get Instant`() {
        TrustedClock.utcClock = Clock.fixed(date.toInstant(), date.zone)
        assertEquals(Instant.now(TrustedClock.utcClock.withZone(ZoneId.systemDefault())), TrustedClock.getNowInstant())
    }

    @Test
    fun `test get Instant UTC`() {
        TrustedClock.utcClock = Clock.fixed(date.toInstant(), date.zone)
        assertEquals(Instant.now(TrustedClock.utcClock), TrustedClock.getNowInstantUTC())
    }
}
