/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.room

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class DateConvertersLongTest {

    // From Long

    @Test
    fun `convert null to LocalDate`() {
        assertNull(DateConvertersLong().getLocalDateFromLong(null))
    }

    @Test
    fun `convert Long to LocalDate`() {
        assertEquals(LocalDate.of(1970, 1, 1), DateConvertersLong().getLocalDateFromLong(0))
    }

    @Test
    fun `convert null to LocalDateTime UTC`() {
        assertNull(DateConvertersLong().getLocalDateTimeUTCFromLong(null))
    }

    @Test
    fun `convert Long to LocalDateTime UTC`() {
        assertEquals(LocalDateTime.of(1970, 1, 1, 0, 0, 0), DateConvertersLong().getLocalDateTimeUTCFromLong(0))
    }

    @Test
    fun `convert null to ZonedDateTime UTC`() {
        assertNull(DateConvertersLong().getZonedDateTimeUTCFromLong(null))
    }

    @Test
    fun `convert Long to ZonedDateTime UTC`() {
        assertEquals(
            ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
            DateConvertersLong().getZonedDateTimeUTCFromLong(0)
        )
    }

    // To Long

    @Test
    fun `convert null LocalDate to long`() {
        assertEquals(0L, DateConvertersLong().setLocalDateToLong(null))
    }

    @Test
    fun `convert LocalDate to long`() {
        assertEquals(0L, DateConvertersLong().setLocalDateToLong(LocalDate.of(1970, 1, 1)))
    }

    @Test
    fun `convert null LocalDateTime UTC to long`() {
        assertEquals(0L, DateConvertersLong().setLocalDateTimeUTCToLong(null))
    }

    @Test
    fun `convert LocalDateTime UTC to long`() {
        assertEquals(0L, DateConvertersLong().setLocalDateTimeUTCToLong(LocalDateTime.of(1970, 1, 1, 0, 0, 0)))
    }

    @Test
    fun `convert null ZonedDateTime UTC to long`() {
        assertEquals(0L, DateConvertersLong().setZonedDateTimeUTCToLong(null))
    }

    @Test
    fun `convert ZonedDateTime UTC to long`() {
        assertEquals(
            0L,
            DateConvertersLong().setZonedDateTimeUTCToLong(ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
        )
    }
}
