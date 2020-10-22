/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.binary

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.sdk.math.Axis
import java.nio.charset.Charset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

/** [PayloadReader] unit tests  */
class PayloadReaderTest {

    @Test
    fun testReadInt8() {
        val test = PayloadReader(byteArrayOf(-1, 0xFF.toByte(), 0x00, 128.toByte()))

        assertEquals((-1).toByte().toLong(), test.readInt8().toLong())
        assertEquals(0xFF.toByte().toLong(), test.readInt8().toLong())
        assertEquals(0x00, test.readInt8().toLong())
        assertEquals(128.toByte().toLong(), test.readInt8().toLong())
    }

    @Test
    fun testReadInt16() {
        val test =
            PayloadReader(byteArrayOf(0x34.toByte(), 0x12.toByte(), 0x78.toByte(), 0x56.toByte()))

        assertEquals(test.readInt16().toLong(), 0x1234)
        assertEquals(test.readInt16().toLong(), 0x5678)
    }

    @Test
    fun testReadUnsignedInt32() {
        val test = PayloadReader(byteArrayOf(0x67, 0x45, 0x23, 0x01))

        assertEquals(0x01234567, test.readUnsignedInt32())
    }

    @Test
    fun testReadUnsignedInt16() {
        val test = PayloadReader(byteArrayOf(0x23, 0x01))

        assertEquals(0x0123, test.readUnsignedInt16().toLong())
    }

    @Test
    fun testReadUnsignedInt8_Positive() {
        val test = PayloadReader(byteArrayOf(56))

        assertEquals(56, test.readUnsignedInt8().toLong())
    }

    @Test
    fun testReadUnsignedInt8_Negative() {
        val test = PayloadReader(byteArrayOf(-56))

        assertNotEquals(-56, test.readUnsignedInt8().toLong())
    }

    @Test
    fun testReadBoolean() {
        val test = PayloadReader(byteArrayOf(0x00, 0x01, 0x44))

        assertFalse(test.readBoolean())
        assertTrue(test.readBoolean())
        assertTrue(test.readBoolean())
    }

    @Test
    fun testReadFloat() {
        val test = PayloadReader(byteArrayOf(12, 24, 19, 17))

        assertEquals(1.1715392E-31f, test.readFloat(), 0.0001f)
    }

    @Test
    fun testSkip() {
        val test = PayloadReader(byteArrayOf(0x01, 0x02, 0x03, 0x04))

        assertEquals(0x01, test.readInt8().toLong())
        test.skip(2)
        assertEquals(0x04, test.readInt8().toLong())
    }

    @Test
    fun testReadString() {
        val payload = "Kolibree éàè123".toByteArray(Charset.forName("UTF-8"))
        val test = PayloadReader(payload)

        assertEquals("Kolibree éàè123", test.readString(payload.size))
    }

    @Test
    fun testReadVector() {
        val test = PayloadReader(
            byteArrayOf(
                0x01,
                0x23,
                0x45,
                0x67,
                0x14,
                0x53,
                0x51,
                0x48,
                0x00,
                0x85.toByte(),
                0x74,
                0x12
            )
        )
        val vector = test.readVector()

        assertEquals(9.309519e+23f, vector.get(Axis.X), 0.0001f)
        assertEquals(214348.313f, vector.get(Axis.Y), 0.0001f)
        assertEquals(7.715676e-28f, vector.get(Axis.Z), 0.0001f)
    }

    @Test
    fun testReadHardwareVersion() {
        val test = PayloadReader(byteArrayOf(0x15, 0x00, 0x74, 0x12))
        val hwv = test.readHardwareVersion()

        assertEquals("21.4724", hwv.toString())
    }

    @Test
    fun testReadSoftwareVersion() {
        val test = PayloadReader(byteArrayOf(0x04, 0x02, 0x20, 0x01))
        val swv = test.readSoftwareVersion()

        assertEquals("4.2.288", swv.toString())
    }

    @Test
    fun `readData returns a dateTime in the current TZ`() { // 2017 October 26th 11:26:05 am UTC
        val timeOffset = 4
        TrustedClock.systemZone = ZoneOffset.ofHours(timeOffset)
        val year = 2017
        val month = 10
        val day = 26
        val hours = 11
        val minutes = 26
        val seconds = 5

        val test = PayloadReader(
            byteArrayOf(
                (year - 2000).toByte(),
                month.toByte(),
                day.toByte(),
                hours.toByte(),
                minutes.toByte(),
                seconds.toByte()
            )
        )

        val expectedDateTime =
            OffsetDateTime.of(
                year,
                month,
                day,
                hours + timeOffset,
                minutes,
                seconds,
                0,
                TrustedClock.systemZoneOffset
            )

        assertEquals(expectedDateTime, test.readDate())
    }

    @Test
    fun testRewind() {
        val test = PayloadReader(byteArrayOf(0x01, 0x02, 0x03))
        assertEquals(0x01, test.readInt8().toLong())
        assertEquals(0x02, test.readInt8().toLong())
        test.rewind()
        assertEquals(0x01, test.readInt8().toLong())
    }

    @Test
    fun readDateFromUnixTimeStamp_returnsParsedZonedDateTime() { // 28 August 2019 at 14:39:49 UTC
        // Decimal timestamp: 1566988789
        val test = PayloadReader(
            byteArrayOf(0xF5.toByte(), 0x59, 0x66, 0x5D)
        )

        val expected = OffsetDateTime.of(2019, 8, 28, 14, 39, 49, 0, TrustedClock.systemZoneOffset)

        assertEquals(expected, test.readDateFromUnixTimeStamp())
    }

    /*
    readDspVersionCompat
     */

    @Test
    fun `readDspVersionCompat returns expected DspVersion`() {
        val reader = PayloadReader(byteArrayOf(0x04, 0x00, 0x01, 0x00, 0x00, 0x02, 0x00, 0x00))
        val version = reader.readDspVersionCompat()
        assertEquals(4, version.major)
        assertEquals(1, version.minor)
        assertEquals(512L, version.algorithm)
    }

    /*
    readDspVersion
     */

    @Test
    fun `readDspVersion returns expected DspVersion`() {
        val reader = PayloadReader(byteArrayOf(0x04, 0x00, 0x01, 0x00, 0x00, 0x04, 0x00))
        val version = reader.readDspVersion()
        assertEquals(4, version.major)
        assertEquals(1, version.minor)
        assertEquals(1024L, version.algorithm)
    }
}
