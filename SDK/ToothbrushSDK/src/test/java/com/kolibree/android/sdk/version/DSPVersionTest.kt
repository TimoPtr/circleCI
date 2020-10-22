package com.kolibree.android.sdk.version

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DSPVersionTest {

    @Test
    fun `construct version with major, minor and revision`() {
        val v = DspVersion(4, 10, 15)

        assertEquals("Value mismatch", "4.10.15", v.toString())
        assertEquals("Binary value mismatch", 0x040A000F, v.toBinary())
        assertEquals("Wrong major value", 4, v.major.toLong())
        assertEquals("Wrong minor value", 10, v.minor.toLong())
        assertEquals("Wrong revision value", 15, v.algorithm.toLong())
    }

    @Test
    fun `two versions with same version should be equals`() {
        val v1 = DspVersion(4, 10, 15)
        val v2 = DspVersion(4, 10, 15)

        assertEquals("Binary Equality test failed", v1.toBinary(), v2.toBinary())
        assertEquals("Equality test failed", v1, v2)
    }

    @Test
    fun `isNewer should return true if version is newer than the current one and false otherwise`() {
        val version = DspVersion(0, 1, 1)
        val equalVersion = DspVersion(0, 1, 1)
        val newerVersion = DspVersion(0, 17, 65535)
        val olderVersion = DspVersion(0, 0, 65536)

        assertFalse(equalVersion.isNewer(version))
        assertFalse(olderVersion.isNewer(version))
        assertTrue(newerVersion.isNewer(version))
    }

    @Test
    fun `isNewerOrSame should return true if version is newer or the same than the current one and false otherwise`() {
        val version = DspVersion(0, 1, 1)
        val equalVersion = DspVersion(0, 1, 1)
        val newerVersion = DspVersion(0, 17, 65535)
        val olderVersion = DspVersion(0, 0, 65536)

        assertFalse(olderVersion.isNewerOrSame(version))
        assertTrue(equalVersion.isNewerOrSame(version))
        assertTrue(newerVersion.isNewerOrSame(version))
    }

    @Test
    fun `NULL equals to 0 0 0`() {
        assertEquals(0, DspVersion.NULL.major)
        assertEquals(0, DspVersion.NULL.minor)
        assertEquals(0, DspVersion.NULL.algorithm)
    }
}
