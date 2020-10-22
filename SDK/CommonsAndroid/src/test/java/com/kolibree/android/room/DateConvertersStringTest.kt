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
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class DateConvertersStringTest {

    // From string

    @Test
    fun `convert String Date to LocalDate`() {
        assertEquals(LocalDate.of(2001, 1, 1), DateConvertersString().getLocalDateFromString("2001-01-01"))
    }

    @Test
    fun `convert null String to LocalDate`() {
        assertNull(DateConvertersString().getLocalDateFromString(null))
    }

    @Test
    fun `convert empty String to LocalDate`() {
        assertNull(DateConvertersString().getLocalDateFromString(""))
    }

    @Test
    fun `convert String DateTime to LocalDateTime`() {
        assertEquals(
            LocalDateTime.of(2001, 1, 1, 1, 2, 21),
            DateConvertersString().getLocalDateTimeFromString("2001-01-01T01:02:21")
        )
    }

    @Test
    fun `convert null String to LocalDateTime`() {
        assertNull(
            DateConvertersString().getLocalDateTimeFromString(null)
        )
    }

    @Test
    fun `convert empty String to LocalDateTime`() {
        assertNull(
            DateConvertersString().getLocalDateTimeFromString("")
        )
    }

    @Test
    fun `convert String DateTime to ZonedDateTime`() {
        assertEquals(
            ZonedDateTime.of(2001, 1, 1, 1, 2, 21, 0, ZoneId.of("+2")),
            DateConvertersString().getZonedDateTimeFromString("2001-01-01T01:02:21+0200")
        )
    }

    @Test
    fun `convert null String to ZonedDateTime`() {
        assertNull(
            DateConvertersString().getZonedDateTimeFromString(null)
        )
    }

    @Test
    fun `convert empty String to ZonedDateTime`() {
        assertNull(
            DateConvertersString().getZonedDateTimeFromString("")
        )
    }

    // To String

    @Test
    fun `convert LocalDate to String`() {
        assertEquals("2001-01-01", DateConvertersString().setLocalDateToString(LocalDate.of(2001, 1, 1)))
    }

    @Test
    fun `convert null LocalDate to String`() {
        assertEquals("", DateConvertersString().setLocalDateToString(null))
    }

    @Test
    fun `convert LocalDateTime to String`() {
        assertEquals(
            "2001-01-01T01:02:21",
            DateConvertersString().setLocalDateTimeToString(LocalDateTime.of(2001, 1, 1, 1, 2, 21))
        )
    }

    @Test
    fun `convert null LocalDateTime to String`() {
        assertEquals(
            "",
            DateConvertersString().setLocalDateTimeToString(null)
        )
    }

    @Test
    fun `convert ZonedDateTime to String`() {
        assertEquals(
            "2001-01-01T01:02:21+0200",
            DateConvertersString().setZonedDateTimeToString(ZonedDateTime.of(2001, 1, 1, 1, 2, 21, 0, ZoneId.of("+2")))
        )
    }

    @Test
    fun `convert null ZonedDateTime to String`() {
        assertEquals(
            "",
            DateConvertersString().setZonedDateTimeToString(null)
        )
    }
}
