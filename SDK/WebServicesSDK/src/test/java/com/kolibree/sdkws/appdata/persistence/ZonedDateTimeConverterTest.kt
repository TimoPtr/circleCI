package com.kolibree.sdkws.appdata.persistence

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

/**
 * [ZonedDateTimeConverter] tests
 */
class ZonedDateTimeConverterTest {

    private val zonedDateTimeConverter = ZonedDateTimeConverter()

    @Test
    fun fromZonedDateTime() {
        val date = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
        val result = zonedDateTimeConverter.fromZonedDateTime(date)
        val expected = 1514764800000L
        assertEquals(expected, result)
    }

    @Test
    fun fromTimestamp() {
        val date = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
        val result = zonedDateTimeConverter.toZonedDateTime(1514764800000L)
        assertTrue(date.isEqual(result))
    }
}
