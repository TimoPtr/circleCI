package com.kolibree.android.sdk.version

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by aurelien on 28/07/17.
 *
 *
 * [HardwareVersion] test unit
 */
class HardwareVersionTest {

    @Test
    fun `construct version from binary`() {
        val value: Long = 0x000A000F

        val v = HardwareVersion(value)

        assertEquals("Value mismatch", "10.15", v.toString())
        assertEquals("Binary value mismatch", value, v.toBinary())
        assertEquals("Wrong major value", 10, v.major.toLong())
        assertEquals("Wrong minor value", 15, v.minor.toLong())
    }

    @Test
    fun `construct version with major and minor`() {
        val v = HardwareVersion(10, 15)

        assertEquals("Value mismatch", "10.15", v.toString())
        assertEquals("Binary value mismatch", 0x000A000F, v.toBinary())
        assertEquals("Wrong major value", 10, v.major.toLong())
        assertEquals("Wrong minor value", 15, v.minor.toLong())
    }

    @Test
    fun `construct version from string`() {
        assertEquals(HardwareVersion(12, 15), HardwareVersion("12.15"))
    }

    @Test
    fun `two versions with same version should be equals`() {
        val v1 = HardwareVersion(10, 15)
        val v2 = HardwareVersion(0x000A000F)

        assertEquals("Binary Equality test failed", v1.toBinary(), v2.toBinary())
        assertEquals("Equality test failed", v1, v2)
    }

    @Test
    fun `isNewer should return true if version is newer than the current one and false otherwise`() {
        val version = HardwareVersion(1, 1)
        val equalVersion = HardwareVersion(1, 1)
        val newerVersion = HardwareVersion(17, 65535)
        val olderVersion = HardwareVersion(0, 65536)

        assertFalse(equalVersion.isNewer(version))
        assertFalse(olderVersion.isNewer(version))
        assertTrue(newerVersion.isNewer(version))
    }
}
