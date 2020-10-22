package com.kolibree.android.sdk.version

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by aurelien on 28/07/17.
 *
 *
 * [SoftwareVersion] test unit
 */
class SoftwareVersionTest {

    @Test
    fun `construct version from binary`() {
        val value: Long = 0x040A000F

        val v = SoftwareVersion(value)

        assertEquals("Value mismatch", "4.10.15", v.toString())
        assertEquals("Binary value mismatch", value, v.toBinary())
        assertEquals("Wrong major value", 4, v.major.toLong())
        assertEquals("Wrong minor value", 10, v.minor.toLong())
        assertEquals("Wrong revision value", 15, v.revision.toLong())
    }

    @Test
    fun `construct version with major, minor and revision`() {
        val v = SoftwareVersion(4, 10, 15)

        assertEquals("Value mismatch", "4.10.15", v.toString())
        assertEquals("Binary value mismatch", 0x040A000F, v.toBinary())
        assertEquals("Wrong major value", 4, v.major.toLong())
        assertEquals("Wrong minor value", 10, v.minor.toLong())
        assertEquals("Wrong revision value", 15, v.revision.toLong())
    }

    @Test
    fun `construct version from string`() {
        assertEquals(SoftwareVersion(12, 15, 21), SoftwareVersion("12.15.21"))
    }

    @Test
    fun `two versions with same version should be equals`() {
        val v1 = SoftwareVersion(4, 10, 15)
        val v2 = SoftwareVersion(0x040A000F)

        assertEquals("Binary Equality test failed", v1.toBinary(), v2.toBinary())
        assertEquals("Equality test failed", v1, v2)
    }

    @Test
    fun `isNewer should return true if version is newer than the current one and false otherwise`() {
        val version = SoftwareVersion(0, 1, 1)
        val equalVersion = SoftwareVersion(0, 1, 1)
        val newerVersion = SoftwareVersion(0, 17, 65535)
        val olderVersion = SoftwareVersion(0, 0, 65536)

        assertFalse(equalVersion.isNewer(version))
        assertFalse(olderVersion.isNewer(version))
        assertTrue(newerVersion.isNewer(version))
    }

    @Test
    fun `isNewerOrSame should return true if version is newer or the same than the current one and false otherwise`() {
        val version = SoftwareVersion(0, 1, 1)
        val equalVersion = SoftwareVersion(0, 1, 1)
        val newerVersion = SoftwareVersion(0, 17, 65535)
        val olderVersion = SoftwareVersion(0, 0, 65536)

        assertFalse(olderVersion.isNewerOrSame(version))
        assertTrue(equalVersion.isNewerOrSame(version))
        assertTrue(newerVersion.isNewerOrSame(version))
    }

    /*
    isNull
     */

    @Test
    fun `isNull returns true for NULL or 0 0 0`() {
        assertTrue(SoftwareVersion.NULL.isNull())
        assertTrue(SoftwareVersion(0, 0, 0).isNull())
    }

    @Test
    fun `isNull returns false for any other combination`() {
        assertFalse(SoftwareVersion(0, 1, 1).isNull())
        assertFalse(SoftwareVersion(0, 1, 1).isNull())
        assertFalse(SoftwareVersion(0, 17, 65535).isNull())
        assertFalse(SoftwareVersion(0, 0, 65536).isNull())
    }
}
